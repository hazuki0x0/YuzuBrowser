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

package jp.hazuki.yuzubrowser.legacy.utils.fastmatch

import android.net.Uri

import java.util.regex.Pattern

internal abstract class RegexHost : SimpleCountMatcher() {
    protected abstract val regex: Pattern

    override val type: Int
        get() = FastMatcher.TYPE_REGEX_HOST
    override val pattern: String
        get() = regex.pattern()

    override fun matchItem(uri: Uri): Boolean {
        val host = uri.host
        return if (host != null)
            regex.matcher(host).find()
        else
            regex.matcher(uri.toString()).find()
    }
}
