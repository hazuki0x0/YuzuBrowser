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
import jp.hazuki.yuzubrowser.download.core.data.DownloadFile
import jp.hazuki.yuzubrowser.download.core.data.MetaData
import jp.hazuki.yuzubrowser.download.service.DownloadService

fun Context.download(root: Uri, file: DownloadFile, meta: MetaData?) {
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