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

package jp.hazuki.yuzubrowser.legacy.adblock.faster.utils

import android.net.Uri

private val ipv4 = "^((0x[\\da-f]+|\\d+)(\\.|\$))*\$".toRegex(RegexOption.IGNORE_CASE)

fun getDomain(host: String): String {
    val index = host.lastIndexOf('.', host.lastIndexOf('.') - 1)

    return if (index < 0) host else host.substring(index + 1)
}

fun isDomain(host: String): Boolean {
    if (!host.contains('.')) return false
    if (ipv4.matches(host)) return false

    return host.contains('.')
}

fun isThirdParty(pageUrl: Uri, requestUri: Uri): Boolean {
    val pageHost = pageUrl.host ?: ""
    val requestHost = requestUri.host ?: ""

    if (pageHost == requestHost) return false

    if (!isDomain(pageHost) || !isDomain(requestHost)) return true

    return getDomain(pageHost) != getDomain(requestHost)
}