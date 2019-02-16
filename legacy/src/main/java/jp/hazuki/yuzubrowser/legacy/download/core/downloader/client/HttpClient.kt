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

package jp.hazuki.yuzubrowser.legacy.download.core.downloader.client

import android.content.Context
import java.io.InputStream

interface HttpClient {
    var connectTimeout: Int

    var instanceFollowRedirects: Boolean

    val responseCode: Int

    val inputStream: InputStream

    val headerFields: Map<String, List<String>>

    fun setCookie(cookie: String?)

    fun setReferrer(referrer: String?)

    fun setUserAgent(context: Context, userAgent: String?)

    fun setRange(range: String)

    fun setHeader(key: String, value: String)

    fun connect()

    fun getHeaderField(name: String): String?

    companion object {
        fun create(url: String, requestMethod: String = "GET"): HttpClient {
            return DefaultHttpClient(url, requestMethod)
        }
    }
}