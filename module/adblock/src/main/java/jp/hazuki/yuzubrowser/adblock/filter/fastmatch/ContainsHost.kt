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

package jp.hazuki.yuzubrowser.adblock.filter.fastmatch

import android.net.Uri

internal class ContainsHost(private val host: String) : SimpleCountMatcher() {
    override val type: Int
        get() = FastMatcher.TYPE_CONTAINS_HOST
    override val pattern: String
        get() = host

    override fun matchItem(uri: Uri): Boolean {
        val req = uri.host
        return req?.contains(host) ?: uri.toString().contains(host)
    }
}