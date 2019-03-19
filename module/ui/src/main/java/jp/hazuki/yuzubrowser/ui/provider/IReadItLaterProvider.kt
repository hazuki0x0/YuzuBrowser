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

package jp.hazuki.yuzubrowser.ui.provider

import android.net.Uri

interface IReadItLaterProvider {
    val editUri: Uri

    fun getReadUri(time: Long): Uri

    fun getEditUri(time: Long): Uri

    fun convertToEdit(uri: Uri): Uri

    companion object {
        const val TIME = "time"
        const val URL = "url"
        const val TITLE = "title"
        const val PATH = "path"

        const val COL_TIME = 0
        const val COL_URL = 1
        const val COL_TITLE = 2
    }
}