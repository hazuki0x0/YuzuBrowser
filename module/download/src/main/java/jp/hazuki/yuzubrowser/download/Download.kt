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

package jp.hazuki.yuzubrowser.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.download.core.data.DownloadFile
import jp.hazuki.yuzubrowser.download.core.data.MetaData
import jp.hazuki.yuzubrowser.download.core.utils.guessDownloadFileName
import jp.hazuki.yuzubrowser.download.core.utils.toDocumentFile
import jp.hazuki.yuzubrowser.download.service.DownloadService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.File
import java.io.IOException

fun Context.download(root: Uri, file: DownloadFile, meta: MetaData?) {
    if (file.url.length > INTENT_LIMIT) {
        ui {
            val tmp = File(cacheDir, DOWNLOAD_TMP_FILENAME)
            try {
                val save = async(Dispatchers.IO) { tmp.outputStream().use { it.write(file.url.toByteArray()) } }
                var name: Deferred<String>? = null
                if (file.name == null) {
                    name = async(Dispatchers.IO) { guessDownloadFileName(root.toDocumentFile(this@download), file.url, null, null, null) }
                }
                save.await()
                val intent = Intent(this@download, DownloadService::class.java).apply {
                    action = INTENT_ACTION_START_DOWNLOAD
                    putExtra(INTENT_EXTRA_DOWNLOAD_ROOT_URI, root)
                    putExtra(INTENT_EXTRA_DOWNLOAD_REQUEST, DownloadFile(file.url.convertToTmpDownloadUrl(), name?.await()
                            ?: file.name, file.request))
                    putExtra(INTENT_EXTRA_DOWNLOAD_METADATA, meta)
                }
                startService(intent)
            } catch (e: IOException) {
            }
        }
        return
    }
    val intent = Intent(this, DownloadService::class.java).apply {
        action = INTENT_ACTION_START_DOWNLOAD
        putExtra(INTENT_EXTRA_DOWNLOAD_ROOT_URI, root)
        putExtra(INTENT_EXTRA_DOWNLOAD_REQUEST, file)
        putExtra(INTENT_EXTRA_DOWNLOAD_METADATA, meta)
    }
    startService(intent)
}

fun Context.reDownload(id: Long) {
    val intent = Intent(this, DownloadService::class.java).apply {
        action = INTENT_ACTION_RESTART_DOWNLOAD
        putExtra(INTENT_EXTRA_DOWNLOAD_ID, id)
    }
    startService(intent)
}

fun String.convertToTmpDownloadUrl(): String {
    var last = indexOf(';')
    if (last < 0) last = indexOf(',')
    return substring(0, last) + DOWNLOAD_TMP_TYPE
}

private const val HALF_MB = 512 * 1024

private const val INTENT_LIMIT = HALF_MB - 1024 - 1

internal const val DOWNLOAD_TMP_TYPE = ";yuzu_tmp_download"

internal const val DOWNLOAD_TMP_FILENAME = "tmp_download"
