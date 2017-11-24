/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.utils.fastmatch

import android.net.Uri

interface FastMatcher {

    val type: Int

    val id: Int

    val frequency: Int

    val pattern: String

    val isUpdate: Boolean

    val time: Long

    fun match(uri: Uri): Boolean

    fun saved()

    companion object {
        const val TYPE_SIMPLE_HOST = 1
        const val TYPE_SIMPLE_URL = 2
        const val TYPE_REGEX_HOST = 3
        const val TYPE_REGEX_URL = 4
        const val TYPE_CONTAINS_HOST = 5
    }
}
