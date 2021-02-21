/*
 * Copyright (C) 2017-2021 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.download.core.downloader

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.documentfile.provider.DocumentFile
import jp.hazuki.yuzubrowser.core.MIME_TYPE_UNKNOWN
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.storage.toDocumentFile
import jp.hazuki.yuzubrowser.download.TMP_FILE_SUFFIX
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection

class UniversalDownloader(private val context: Context, private val info: DownloadFileInfo, private val request: DownloadRequest) : Downloader {
    override var downloadListener: Downloader.DownloadListener? = null

    private var abort = false

    override fun download(): Boolean {
        val conn: URLConnection
        try {
            conn = URL(info.url).openConnection()
        } catch (e: MalformedURLException) {
            downloadListener?.onFileDownloadFailed(info, "unknown url:${info.url}")
            return false
        }

        conn.setRequestProperty("Connection", "close")

        val cookie = CookieManager.getInstance().getCookie(info.url)
        if (!cookie.isNullOrEmpty()) {
            conn.setRequestProperty("Cookie", cookie)
        }

        if (!request.referrer.isNullOrEmpty()) {
            conn.setRequestProperty("Referer", request.referrer)
        }
        if (request.userAgent.isNullOrEmpty()) {
            conn.setRequestProperty("User-Agent", WebSettings.getDefaultUserAgent(context))
        } else {
            conn.setRequestProperty("User-Agent", request.userAgent)
        }

        val rootFile = info.root.toDocumentFile(context)
        val downloadFile = if (request.isScopedStorageMode) {
            rootFile
        } else {
            val existTmp = rootFile.findFile("${info.name}$TMP_FILE_SUFFIX")

            if (info.resumable) {
                info.resumable = false
            }

            val mimeType = if (info.mimeType.isNotEmpty()) info.mimeType else MIME_TYPE_UNKNOWN

            val tmpFile = existTmp ?: rootFile.createFile(mimeType, "${info.name}$TMP_FILE_SUFFIX")

            if (tmpFile == null) {
                info.state = DownloadFileInfo.STATE_UNKNOWN_ERROR
                downloadListener?.onFileDownloadFailed(info, "Unable to create file, file name:${info.name}")
                return false
            }

            tmpFile
        }


        try {
            conn.connect()

            if (info.size < 0) {
                info.size = conn.contentLength.toLong()
            }

            context.contentResolver.openOutputStream(downloadFile.uri, "w").use { output ->
                if (output == null) throw IllegalStateException()
                conn.inputStream.use { input ->
                    downloadListener?.onStartDownload(info)

                    var len: Int
                    var progress = info.currentSize
                    val buffer = ByteArray(BUFFER_SIZE)
                    var oldSize: Long
                    var oldSec = System.currentTimeMillis()

                    while (input.read(buffer, 0, BUFFER_SIZE).also { len = it } >= 0) {
                        if (abort) break

                        output.write(buffer, 0, len)
                        progress += len

                        if (System.currentTimeMillis() > oldSec + NOTIFICATION_INTERVAL) {
                            oldSize = info.currentSize
                            info.currentSize = progress

                            val time = System.currentTimeMillis()
                            info.transferSpeed = ((progress - oldSize) * 1000.0 / (time - oldSec)).toLong()
                            downloadListener?.onFileDownloading(info, progress)

                            oldSec = time
                        }
                    }
                }
            }

            var downloadedFile: DocumentFile? = null

            if (abort) {
                deleteTempIfNeed(rootFile)
                downloadListener?.onFileDownloadAbort(info)
                return false
            } else {
                if (request.isScopedStorageMode) {
                    downloadedFile = rootFile
                } else {
                    if (!downloadFile.renameTo(info.name)) {
                        downloadedFile = rootFile.findFile(info.name)
                        if (downloadedFile == null)
                            throw DownloadException("Rename is failed. name:\"${info.name}\", download path:${rootFile.uri}, mimetype:${info.mimeType}, exists:${rootFile.findFile(info.name) != null}")
                    }

                    downloadedFile = downloadedFile ?: rootFile.findFile(info.name)
                    if (downloadedFile == null) {
                        throw DownloadException("File not found. name:\"${info.name}\", download path:${rootFile.uri}")
                    }
                }
            }

            info.state = DownloadFileInfo.STATE_DOWNLOADED
            downloadListener?.onFileDownloaded(info, downloadedFile)
            return true
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
            if (downloadFile.exists()) {
                downloadFile.delete()
            }
            info.state = DownloadFileInfo.STATE_UNKNOWN_ERROR
            downloadListener?.onFileDownloadFailed(info, null)
        }
        return false
    }

    override fun cancel() {
        info.state = DownloadFileInfo.STATE_CANCELED
        abort = true
    }

    override fun pause() {
        info.state = DownloadFileInfo.STATE_PAUSED
        abort = true
    }

    override fun abort() {
        info.state = DownloadFileInfo.STATE_UNKNOWN_ERROR
        abort = true
    }

    private fun deleteTempIfNeed(rootFile: DocumentFile) {
        if (info.state == DownloadFileInfo.STATE_UNKNOWN_ERROR || info.state == DownloadFileInfo.STATE_CANCELED) {
            rootFile.findFile("${info.name}$TMP_FILE_SUFFIX")?.delete()
        }
    }

    private class DownloadException(message: String) : IOException(message)

    companion object {
        private const val BUFFER_SIZE = 1024 * 2
        private const val NOTIFICATION_INTERVAL = 1000
    }
}
