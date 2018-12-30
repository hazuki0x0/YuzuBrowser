/*
 * Copyright (C) 2017-2018 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.adblock

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import jp.hazuki.utility.extensions.getNoCacheResponse
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.adblock.faster.Filter
import jp.hazuki.yuzubrowser.legacy.adblock.faster.core.AdBlocker
import jp.hazuki.yuzubrowser.legacy.adblock.faster.core.FilterMatcher
import jp.hazuki.yuzubrowser.legacy.utils.IOUtils
import jp.hazuki.yuzubrowser.legacy.utils.fastmatch.FastMatcherList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class AdBlockController(context: Context) {
    private val dummyImage: ByteArray = IOUtils.readByte(context.resources.assets.open("blank.png"))
    private val dummy = WebResourceResponse("text/plain", "UTF-8", EmptyInputStream())

    private val manager: AdBlockManager = AdBlockManager(context)
    private var whitePageList: FastMatcherList? = null
    private var adBlocker: AdBlocker? = null
    private var updating = false

    init {
        update()
    }

    fun update() {
        updating = true
        GlobalScope.launch {
            lateinit var blackFilter: FilterMatcher
            lateinit var whiteFilter: FilterMatcher
            manager.getFastMatcherCachedList(AdBlockManager.BLACK_TABLE_NAME).let {
                blackFilter = FilterMatcher(it.iterator())
            }

            manager.getFastMatcherCachedList(AdBlockManager.WHITE_TABLE_NAME).let {
                whiteFilter = FilterMatcher(it.iterator())
            }

            manager.getFastMatcherCachedList(AdBlockManager.WHITE_PAGE_TABLE_NAME).let {
                whitePageList = it
                adBlocker = AdBlocker(blackFilter, whiteFilter, it)
            }
            updating = false
        }
    }

    fun isBlock(pageUri: Uri, uri: Uri): Filter? {
        return adBlocker?.isBlock(pageUri, uri)
    }

    fun onResume() {
        if (updating) return

        GlobalScope.launch {
            manager.updateMatcher(AdBlockManager.BLACK_TABLE_NAME, adBlocker?.blackList)
            manager.updateMatcher(AdBlockManager.WHITE_TABLE_NAME, adBlocker?.whiteList)
            manager.updateOrder(AdBlockManager.WHITE_PAGE_TABLE_NAME, whitePageList)
        }
    }

    fun createDummy(uri: Uri): WebResourceResponse {
        val last = uri.lastPathSegment
        return if (last != null && (!last.contains(".") || last.endsWith(".js") || last.endsWith(".css")
                || last.endsWith(".html") || last.endsWith(".htm"))) {
            dummy
        } else {
            WebResourceResponse("image/png", null, ByteArrayInputStream(dummyImage))
        }
    }

    fun createMainFrameDummy(context: Context, uri: Uri, pattern: String): WebResourceResponse {
        val builder = StringBuilder("<meta charset=utf-8>" +
                "<meta content=\"width=device-width,initial-scale=1,minimum-scale=1\"name=viewport>" +
                "<style>body{padding:5px 15px;background:#fafafa}body,p{text-align:center}p{margin:20px 0 0}" +
                "pre{margin:5px 0;padding:5px;background:#ddd}</style><title>")
                .append(context.getText(R.string.pref_ad_block))
                .append("</title><p>")
                .append(context.getText(R.string.ad_block_blocked_page))
                .append("<pre>")
                .append(uri)
                .append("</pre><p>")
                .append(context.getText(R.string.ad_block_blocked_filter))
                .append("<pre>")
                .append(pattern)
                .append("</pre>")

        return getNoCacheResponse("text/html", builder)
    }

    private class EmptyInputStream : InputStream() {
        @Throws(IOException::class)
        override fun read(): Int = -1
    }
}
