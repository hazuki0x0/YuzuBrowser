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

package jp.hazuki.yuzubrowser.download

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import jp.hazuki.yuzubrowser.download.core.utils.setCookie
import jp.hazuki.yuzubrowser.download.core.utils.setReferrer
import jp.hazuki.yuzubrowser.download.core.utils.setUserAgent
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

fun OkHttpClient.getImage(url: String, userAgent: String?, referrer: String?, cookie: String? = null): Bitmap? {
    val request = Request.Builder()
        .url(url)
        .get()
        .setUserAgent(userAgent)
        .setReferrer(referrer)
        .setCookie(cookie)
        .build()

    try {
        newCall(request).execute().use { response ->
            response.body?.let {
                return BitmapFactory.decodeStream(it.byteStream())
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}
