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

import jp.hazuki.yuzubrowser.legacy.utils.ErrorReport
import jp.hazuki.yuzubrowser.legacy.utils.fastmatch.regex.NormalRegexUrl
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class ItemDecoder {

    private val factory = FastMatcherFactory()

    fun singleDecode(line: String, id: Int, count: Int, time: Long): FastMatcher? {
        val matcher = singleDecode(line)
        if (matcher != null) {
            matcher.id = id
            matcher.count = count
            matcher.time = time
        }
        return matcher
    }

    private fun singleDecode(line: String): SimpleCountMatcher? {
        if (line.length > 2) {
            if (line[0] == '[' && line[line.length - 1] == ']') {
                return try {
                    NormalRegexUrl(line.substring(1, line.length - 1))
                } catch (e: PatternSyntaxException) {
                    ErrorReport.printAndWriteLog(e)
                    null
                }

            }
            val space = line.indexOf(' ')
            if (space > 0) {
                val ip = line.substring(0, space)
                if (IP_ADDRESS.matcher(ip).matches() && line.length > space + 1
                        || ip == "h" || ip == "host") {
                    return factory.compileHost(line.substring(space + 1))
                } else if (ip == "c") {
                    return ContainsHost(line.substring(space + 1))
                }
            }
            val matcher = HOST.matcher(line)
            if (matcher.matches()) {
                val host = matcher.group()
                return factory.compileHost(host)
            }

            return factory.compileUrl(line)
        }
        return null
    }

    fun release() {
        factory.release()
    }

    companion object {
        private val IP_ADDRESS = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+$")
        private val HOST = Pattern.compile("^https?://([0-9a-z.\\-]+)/?$")
    }
}
