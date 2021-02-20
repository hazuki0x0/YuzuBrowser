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
import jp.hazuki.yuzubrowser.core.utility.storage.toDocumentFile
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import jp.hazuki.yuzubrowser.download.core.utils.decodeBase64Image
import jp.hazuki.yuzubrowser.download.core.utils.saveBase64Image

class Base64Downloader(
    private val context: Context,
    private val info: DownloadFileInfo,
    private val request: DownloadRequest,
) : Downloader {
    override var downloadListener: Downloader.DownloadListener? = null

    override fun download(): Boolean {
        downloadListener?.onStartDownload(info)
        val image = decodeBase64Image(info.url)
        val rootFile = info.root.toDocumentFile(context)
        val file = if (request.isScopedStorageMode) {
            rootFile
        } else {
            rootFile.createFile(image.mimeType, info.name)
        }
        return if (file != null && context.contentResolver.saveBase64Image(image, file)) {
            info.state = DownloadFileInfo.STATE_DOWNLOADED
            info.size = file.length()
            downloadListener?.onFileDownloaded(info, file)
            true
        } else {
            info.state = DownloadFileInfo.STATE_UNKNOWN_ERROR
            downloadListener?.onFileDownloadFailed(info)
            false
        }
    }
}
