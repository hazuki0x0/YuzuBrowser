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

package jp.hazuki.yuzubrowser.adblock

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import jp.hazuki.yuzubrowser.adblock.core.AbpLoader
import jp.hazuki.yuzubrowser.adblock.core.AdBlocker
import jp.hazuki.yuzubrowser.adblock.core.FilterMatcher
import jp.hazuki.yuzubrowser.adblock.core.FilterMatcherList
import jp.hazuki.yuzubrowser.adblock.filter.Filter
import jp.hazuki.yuzubrowser.adblock.filter.abp.ABP_PREFIX_BLACK
import jp.hazuki.yuzubrowser.adblock.filter.abp.ABP_PREFIX_WHITE
import jp.hazuki.yuzubrowser.adblock.filter.abp.ABP_PREFIX_WHITE_PAGE
import jp.hazuki.yuzubrowser.adblock.filter.unified.getFilterDir
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpDao
import jp.hazuki.yuzubrowser.adblock.repository.original.AdBlockManager
import jp.hazuki.yuzubrowser.core.utility.extensions.getNoCacheResponse
import jp.hazuki.yuzubrowser.core.utility.utils.IOUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class AdBlockController(private val context: Context, private val abpDao: AbpDao) {
    private val dummyImage: ByteArray = IOUtils.readByte(context.resources.assets.open("blank.png"))
    private val dummy = WebResourceResponse("text/plain", "UTF-8", EmptyInputStream())

    private val manager: AdBlockManager = AdBlockManager(context)
    private var adBlocker: AdBlocker? = null
    private var updating = false

    init {
        update()
    }

    fun update() {
        updating = true
        GlobalScope.launch {
            val abpLoader = AbpLoader(context.getFilterDir(), abpDao.getAll())
            val black = async {
                FilterMatcher(manager.getCachedMatcherList(AdBlockManager.BLACK_TABLE_NAME).iterator()).also {
                    abpLoader.loadAll(ABP_PREFIX_BLACK, it)
                }
            }
            val white = async {
                FilterMatcher(manager.getCachedMatcherList(AdBlockManager.WHITE_TABLE_NAME).iterator()).also {
                    abpLoader.loadAll(ABP_PREFIX_WHITE, it)
                }
            }
            val whitePage = async {
                val list = mutableListOf<Filter>()
                list.addAll(manager.getCachedMatcherList(AdBlockManager.WHITE_PAGE_TABLE_NAME))
                list.addAll(abpLoader.loadAllList(ABP_PREFIX_WHITE_PAGE))
                FilterMatcherList(list)
            }
            adBlocker = AdBlocker(black.await(), white.await(), whitePage.await())

            updating = false
        }
    }

    fun isBlock(pageUri: Uri, request: WebResourceRequest): Filter? {
        return adBlocker?.isBlock(pageUri, request)
    }

    fun onResume() {
        if (updating) return
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
