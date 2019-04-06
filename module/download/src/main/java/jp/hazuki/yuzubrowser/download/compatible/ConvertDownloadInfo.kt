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

package jp.hazuki.yuzubrowser.download.compatible

import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import java.io.File

class ConvertDownloadInfo(val url: String, path: String, val time: Long, state: Int) {
    val root: String
    val name: String
    val state = if (state == 100) DownloadFileInfo.STATE_UNKNOWN_ERROR else state

    init {
        val file = File(path)
        root = "file://${file.parent}"
        name = file.name
    }
}