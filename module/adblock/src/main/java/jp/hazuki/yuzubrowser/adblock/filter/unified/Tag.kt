/*
 * Copyright 2020 Hazuki
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

package jp.hazuki.yuzubrowser.adblock.filter.unified

import com.google.re2j.Pattern

object Tag {
    private val pattern = Pattern.compile("[a-z0-9%]{3,}")

    fun create(url: String) = sequence {
        val m = pattern.matcher(url)
        while (m.find()) {
            val tag = url.substring(m.start(), m.end())
            if (isPrevent(tag)) continue
            yield(tag)
        }
        yield("")
    }

    fun createBest(pattern: String): String {
        var maxLength = 0
        var tag = ""

        val m = Tag.pattern.matcher(pattern)
        while (m.find()) {
            val candidate = pattern.substring(m.start(), m.end())
            if (isPrevent(candidate)) continue

            if (candidate.length > maxLength) {
                maxLength = candidate.length
                tag = candidate
            }
        }

        return tag
    }

    private fun isPrevent(tag: String): Boolean {
        return when (tag) {
            "http", "https", "html", "jpg", "png" -> true
            else -> false
        }
    }
}
