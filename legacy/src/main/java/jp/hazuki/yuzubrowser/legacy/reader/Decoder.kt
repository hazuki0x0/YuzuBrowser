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

package jp.hazuki.yuzubrowser.legacy.reader

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.webkit.WebSettings
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.core.utility.utils.ImageUtils
import jp.hazuki.yuzubrowser.download.getImage
import jp.hazuki.yuzubrowser.legacy.reader.snacktory.HtmlFetcher
import okhttp3.OkHttpClient
import java.util.*

fun OkHttpClient.decodeToReaderData(context: Context, url: String, userAgent: String?): ReaderData? {
    val fetcher = HtmlFetcher()
    if (userAgent.isNullOrEmpty()) {
        fetcher.userAgent = WebSettings.getDefaultUserAgent(context)
    } else {
        fetcher.userAgent = userAgent
    }

    fetcher.referrer = url
    val locale = Locale.getDefault()
    val language = locale.language + "-" + locale.country
    if (language.length >= 5) {
        fetcher.language = language
    }

    try {
        val result = fetcher.fetchAndExtract(this, url, 2500, true)
        if (!TextUtils.isEmpty(result.text)) {
            val html: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(result.text, Html.FROM_HTML_MODE_LEGACY, Html.ImageGetter { getImage(context, it, url, userAgent) }, null)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(result.text, Html.ImageGetter { getImage(context, it, url, userAgent) }, null)
            }
            return ReaderData(result.title, html)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } catch (e: OutOfMemoryError) {
        System.gc()
        Logger.w("reader", e, "Out of memory")
    }

    return null
}

private fun OkHttpClient.getImage(context: Context, imageUrl: String, url: String, userAgent: String?): Drawable {
    val drawable = ImageUtils.getDrawable(context, getImage(imageUrl, userAgent, url))
    return drawable ?: ColorDrawable(0)
}
