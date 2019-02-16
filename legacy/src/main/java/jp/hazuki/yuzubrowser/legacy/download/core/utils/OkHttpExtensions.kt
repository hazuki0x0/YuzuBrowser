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

package jp.hazuki.yuzubrowser.legacy.download.core.utils

import android.content.Context
import android.webkit.WebSettings
import okhttp3.Request
import okhttp3.Response

fun Request.Builder.setCookie(cookie: String?): Request.Builder {
    if (!cookie.isNullOrEmpty())
        header("Cookie", cookie)
    return this
}

fun Request.Builder.setReferrer(referrer: String?): Request.Builder {
    if (!referrer.isNullOrEmpty())
        header("Referer", referrer)
    return this
}

fun Request.Builder.setUserAgent(context: Context, userAgent: String?): Request.Builder {
    if (userAgent.isNullOrEmpty()) {
        header("User-Agent", WebSettings.getDefaultUserAgent(context))
    } else {
        header("User-Agent", userAgent)
    }
    return this
}

val Response.contentLength: Long
    get() {
        val str = header("Content-Length")
        try {
            return str?.toLong() ?: -1
        } catch (e: NumberFormatException) {
        }
        return -1
    }

val Response.isResumable: Boolean
    get() {
        val range = header("Accept-Ranges")
        return range == "bytes"
    }