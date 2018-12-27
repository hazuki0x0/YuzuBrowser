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

package jp.hazuki.yuzubrowser.legacy.utils.fastmatch

import android.net.Uri
import java.util.*

class FastMatcherList(
        private val matcherList: ArrayList<FastMatcher> = arrayListOf(),
        var dbTime: Long = -1
) : Iterable<FastMatcher> {

    override fun iterator(): Iterator<FastMatcher> {
        return matcherList.iterator()
    }

    @Synchronized
    fun match(uri: Uri): Boolean = matcherList.any { it.match(uri) }

    @Synchronized
    fun sort() {
        Collections.sort(matcherList, FastMatcherSorter())
    }
}
