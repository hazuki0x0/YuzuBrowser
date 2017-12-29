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

package jp.hazuki.yuzubrowser.download2.core.downloader.client

import android.content.Context
import android.webkit.WebSettings
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DefaultHttpClient(url: String, requestMethod: String) : HttpClient {

    private val connection = URL(url).openConnection() as HttpURLConnection

    init {
        connection.requestMethod = requestMethod
    }

    override var connectTimeout: Int
        get() = connection.connectTimeout
        set(value) {
            connection.connectTimeout = value
        }

    override var instanceFollowRedirects: Boolean
        get() = connection.instanceFollowRedirects
        set(value) {
            connection.instanceFollowRedirects = value
        }

    override val responseCode: Int
        get() = connection.responseCode

    override val inputStream: InputStream
        get() = connection.inputStream

    override val headerFields: Map<String, List<String>>
        get() = connection.headerFields

    override fun setCookie(cookie: String?) {
        if (!cookie.isNullOrEmpty()) {
            connection.setRequestProperty("Cookie", cookie)
        }
    }

    override fun setReferrer(referrer: String?) {
        if (!referrer.isNullOrEmpty()) {
            connection.setRequestProperty("Referer", referrer)
        }
    }

    override fun setUserAgent(context: Context, userAgent: String?) {
        if (userAgent.isNullOrEmpty()) {
            connection.setRequestProperty("User-Agent", WebSettings.getDefaultUserAgent(context))
        } else {
            connection.setRequestProperty("User-Agent", userAgent)
        }
    }

    override fun setRange(range: String) {
        connection.setRequestProperty("Range", range)
    }

    override fun setHeader(key: String, value: String) {
        connection.setRequestProperty(key, value)
    }

    override fun connect() {
        connection.connect()
    }

    override fun getHeaderField(name: String): String? {
        return connection.getHeaderField(name)
    }
}