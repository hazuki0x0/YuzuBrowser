/*
 * Copyright (C) 2017-2019 Hazuki
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
import jp.hazuki.yuzubrowser.download.DOWNLOAD_TMP_TYPE
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import okhttp3.OkHttpClient

interface Downloader {
    var downloadListener: DownloadListener?

    fun download(): Boolean

    fun cancel() = Unit

    fun pause() = Unit

    fun abort() = Unit

    companion object {
        fun getDownloader(context: Context, okHttpClient: OkHttpClient, info: DownloadFileInfo, request: DownloadRequest): Downloader {
            return if (info.url.startsWith("data:")) {
                val semicolon = info.url.indexOf(';')
                if (info.url.startsWith(DOWNLOAD_TMP_TYPE, semicolon)) {
                    Base64TmpDownloader(context, info)
                } else {
                    Base64Downloader(context.contentResolver, info)
                }

            } else if (info.url.startsWith("http:", true) || info.url.startsWith("https:", true)) {
                OkHttpDownloader(context, okHttpClient, info, request)
            } else {
                UniversalDownloader(context, info, request)
            }
        }
    }

    interface DownloadListener {

        fun onStartDownload(info: DownloadFileInfo)

        fun onFileDownloaded(info: DownloadFileInfo, downloadedFile: androidx.documentfile.provider.DocumentFile)

        fun onFileDownloadAbort(info: DownloadFileInfo)

        fun onFileDownloadFailed(info: DownloadFileInfo, cause: String? = null)

        fun onFileDownloading(info: DownloadFileInfo, progress: Long)
    }
}