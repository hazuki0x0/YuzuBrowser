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

package jp.hazuki.yuzubrowser.ui.utils

import android.util.Patterns
import android.webkit.URLUtil
import java.util.*
import java.util.regex.Pattern

private val URI_SCHEMA = Pattern.compile("((?:http|https|file|market)://|(?:inline|data|about|content|javascript|mailto|view-source|yuzu|blob):)(.*)", Pattern.CASE_INSENSITIVE)

fun String.isUrl(): Boolean {
    val query = trim()
    val hasSpace = query.indexOf(' ') != -1
    val matcher = URI_SCHEMA.matcher(query)
    return if (matcher.matches()) true else !hasSpace && Patterns.WEB_URL.matcher(query).matches()
}


fun String.makeUrlFromQuery(search_url: String, search_place_holder: String): String {
    var query = trim()
    val hasSpace = query.indexOf(' ') != -1

    val matcher = URI_SCHEMA.matcher(query)
    if (matcher.matches()) {
        val scheme = matcher.group(1)
        val lcScheme = scheme.toLowerCase(Locale.US)
        if (lcScheme != scheme) {
            query = lcScheme + matcher.group(2)
        }
        return query
    }
    return if (!hasSpace && Patterns.WEB_URL.matcher(query).matches()) {
        URLUtil.guessUrl(query)
    } else URLUtil.composeSearchUrl(query, search_url, search_place_holder)
}


fun String.makeUrl(): String {
    var query = trim()
    val hasSpace = query.indexOf(' ') != -1

    val matcher = URI_SCHEMA.matcher(query)
    if (matcher.matches()) {
        val scheme = matcher.group(1)
        val lcScheme = scheme.toLowerCase(Locale.US)
        if (lcScheme != scheme) {
            query = lcScheme + matcher.group(2)
        }
        if (hasSpace && Patterns.WEB_URL.matcher(query).matches()) {
            query = query.replace(" ", "%20")
        }
        return query
    }
    return URLUtil.guessUrl(query)
}
