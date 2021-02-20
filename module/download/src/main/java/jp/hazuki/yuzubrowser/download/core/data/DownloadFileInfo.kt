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

package jp.hazuki.yuzubrowser.download.core.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadFileInfo(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    val url: String,
    val mimeType: String,
    var root: Uri,
    val name: String,
    var size: Long,
    var resumable: Boolean = false,
    var startTime: Long = System.currentTimeMillis(),
    var state: Int
) {

    constructor (
        url: String,
        mimeType: String,
        root: Uri,
        name: String,
        size: Long,
        resumable: Boolean = false,
        startTime: Long = System.currentTimeMillis()
    ) : this(0, url, mimeType, root, name, size, resumable, startTime, STATE_DOWNLOADING)

    constructor(
        root: Uri,
        file: DownloadFile,
        meta: MetaData
    ) : this(file.url, meta.mineType, root, file.name ?: meta.name, meta.size, meta.resumable)

    @Ignore
    var currentSize = 0L

    @Ignore
    var transferSpeed = 0L

    companion object {
        const val STATE_DOWNLOADING = 0
        const val STATE_DOWNLOADED = 1
        const val STATE_CANCELED = 2
        const val STATE_PAUSED = 4
        const val STATE_UNKNOWN_ERROR = 512
    }
}
