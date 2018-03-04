/*
 * Copyright (C) 2017-2018 Hazuki
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
import jp.hazuki.yuzubrowser.Constants
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import jp.hazuki.yuzubrowser.download.core.downloader.client.HttpClient
import jp.hazuki.yuzubrowser.download.core.utils.checkFlag
import jp.hazuki.yuzubrowser.download.core.utils.contentLength
import jp.hazuki.yuzubrowser.download.core.utils.isResumable
import jp.hazuki.yuzubrowser.utils.ErrorReport
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException

class HttpDownloader(private val context: Context, private val info: DownloadFileInfo, private val request: DownloadRequest) : Downloader {
    override var downloadListener: Downloader.DownloadListener? = null

    private var abort = false

    override fun download(): Boolean {
        val httpClient: HttpClient
        try {
            httpClient = HttpClient.create(info.url)
        } catch (e: MalformedURLException) {
            downloadListener?.onFileDownloadFailed(info, "unknown url:${info.url}")
            return false
        }

        httpClient.instanceFollowRedirects = true

        httpClient.setHeader("Connection", "close")

        httpClient.setCookie(CookieManager.getInstance().getCookie(info.url))
        httpClient.setReferrer(request.referrer)
        httpClient.setUserAgent(context, request.userAgent)

        val existTmp = info.root.findFile("${info.name}${Constants.download.TMP_FILE_SUFFIX}")

        if (info.resumable && info.checkFlag(DownloadFileInfo.STATE_PAUSED) && existTmp != null) {
            info.currentSize = existTmp.length()
            httpClient.setRange("bytes=${info.currentSize}-${info.size}")
        }

        val tmp = existTmp ?: info.root.createFile(info.mimeType, "${info.name}${Constants.download.TMP_FILE_SUFFIX}")
        ?: throw IllegalStateException("Can not create file. mimetype:${info.mimeType}, filename:${info.name}${Constants.download.TMP_FILE_SUFFIX}")

        try {
            httpClient.connect()
            val mode = when (httpClient.responseCode) {
                HttpURLConnection.HTTP_OK -> "w"
                HttpURLConnection.HTTP_PARTIAL -> "wa"
                else -> {
                    info.state = DownloadFileInfo.STATE_UNKNOWN_ERROR
                    downloadListener?.onFileDownloadFailed(info, "failed connect response:${httpClient.responseCode}\nat:${info.url}")
                    return false
                }
            }

            if (info.size < 0) {
                info.size = httpClient.contentLength
            }
            if (!info.resumable) {
                info.resumable = httpClient.isResumable
            }

            context.contentResolver.openOutputStream(tmp.uri, mode).use { output ->
                httpClient.inputStream.use { input ->
                    downloadListener?.onStartDownload(info)

                    var len = -1
                    var progress = info.currentSize
                    val buffer = ByteArray(BUFFER_SIZE)
                    var oldSec = System.currentTimeMillis()

                    while (input.read(buffer, 0, BUFFER_SIZE).also { len = it } >= 0) {
                        if (abort) break

                        output.write(buffer, 0, len)
                        progress += len

                        if (System.currentTimeMillis() > oldSec + NOTIFICATION_INTERVAL) {
                            info.currentSize = progress
                            downloadListener?.onFileDownloading(info, progress)
                            oldSec = System.currentTimeMillis()
                        }
                    }
                }
            }

            if (abort) {
                deleteTempIfNeed()
                downloadListener?.onFileDownloadAbort(info)
                return false
            } else {
                tmp.renameTo(info.name)
            }

            info.state = DownloadFileInfo.STATE_DOWNLOADED
            downloadListener?.onFileDownloaded(info)
            return true
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
            if (info.resumable) {
                info.state = DownloadFileInfo.STATE_UNKNOWN_ERROR or DownloadFileInfo.STATE_PAUSED
            } else {
                tmp.delete()
                info.state = DownloadFileInfo.STATE_UNKNOWN_ERROR
            }
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
        if (info.resumable) {
            info.state = DownloadFileInfo.STATE_UNKNOWN_ERROR or DownloadFileInfo.STATE_PAUSED
        } else {
            info.state = DownloadFileInfo.STATE_UNKNOWN_ERROR
        }
        abort = true
    }

    private fun deleteTempIfNeed() {
        if (info.state == DownloadFileInfo.STATE_UNKNOWN_ERROR || info.state == DownloadFileInfo.STATE_CANCELED) {
            info.root.findFile("${info.name}${Constants.download.TMP_FILE_SUFFIX}")?.delete()
        }
    }

    companion object {
        private const val BUFFER_SIZE = 1024 * 2
        private const val NOTIFICATION_INTERVAL = 1000
    }
}