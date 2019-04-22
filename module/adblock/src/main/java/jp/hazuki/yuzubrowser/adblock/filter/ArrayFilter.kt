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

package jp.hazuki.yuzubrowser.adblock.filter

import android.net.Uri

class ArrayFilter : ArrayList<Filter>(2), Filter {
    override val pattern: String
        get() = throw IllegalStateException()

    override fun match(url: Uri, pageUrl: Uri, contentType: Int, isThirdParty: Boolean): Boolean {
        return find(url, pageUrl, contentType, isThirdParty) != null
    }

    override fun find(url: Uri, pageUrl: Uri, contentType: Int, isThirdParty: Boolean): Filter? {
        return firstOrNull { it.find(url, pageUrl, contentType, isThirdParty) != null }
    }
}
