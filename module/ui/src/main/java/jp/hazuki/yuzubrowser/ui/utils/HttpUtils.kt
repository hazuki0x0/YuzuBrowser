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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

fun OkHttpClient.getImage(url: String, userAgent: String?, referrer: String?, cookie: String? = null): Bitmap? {
    val requestBuilder = Request.Builder()
        .url(url)
        .get()

    if (!userAgent.isNullOrEmpty()) requestBuilder.addHeader("User-Agent", userAgent)
    if (!referrer.isNullOrEmpty()) requestBuilder.addHeader("Referer", referrer)
    if (!cookie.isNullOrEmpty()) requestBuilder.addHeader("Cookie", cookie)

    try {
        newCall(requestBuilder.build()).execute().use { response ->
            response.body()?.let {
                return BitmapFactory.decodeStream(it.byteStream())
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}
