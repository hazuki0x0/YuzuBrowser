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

package jp.hazuki.yuzubrowser.legacy.userjs

import jp.hazuki.yuzubrowser.core.utility.extensions.replace
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.legacy.utils.WebUtils.maybeContainsUrlScheme
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

private val TLD_REGEX = "^([^:]+://[^/]+)\\\\.tld(/.*)?\$".toRegex()

fun makeUrlPattern(patternUrl: String?): Pattern? {
    if (patternUrl == null) return null
    try {
        val builder = StringBuilder(patternUrl)
        builder.replace("?", "\\?").replace(".", "\\.").replace("*", ".*?").replace("+", ".+?")
        var converted = builder.toString()

        if (converted.contains(".tld", true)) {
            converted = TLD_REGEX.replaceFirst(converted, "$1(.[a-z]{1,6}){1,3}$2")
        }

        return if (maybeContainsUrlScheme(converted))
            Pattern.compile("^$converted")
        else
            Pattern.compile("^\\w+://$converted")
    } catch (e: PatternSyntaxException) {
        ErrorReport.printAndWriteLog(e)
    }

    return null
}

fun makeUrlPatternParsed(patternUrl: String): Pattern? {
    try {
        val converted = if (patternUrl.contains(".tld", true)) {
            TLD_REGEX.replaceFirst(patternUrl, "$1(.[a-z]{1,6}){1,3}$2")
        } else {
            patternUrl
        }
        return Pattern.compile(converted)
    } catch (e: PatternSyntaxException) {
        ErrorReport.printAndWriteLog(e)
    }

    return null
}