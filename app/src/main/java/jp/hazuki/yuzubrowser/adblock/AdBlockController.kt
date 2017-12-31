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

package jp.hazuki.yuzubrowser.adblock

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import jp.hazuki.yuzubrowser.adblock.faster.core.AdBlocker
import jp.hazuki.yuzubrowser.adblock.faster.core.FilterMatcher
import jp.hazuki.yuzubrowser.utils.IOUtils
import jp.hazuki.yuzubrowser.utils.fastmatch.FastMatcherList
import kotlinx.coroutines.experimental.launch
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class AdBlockController(context: Context) {
    private val dummyImage: ByteArray = IOUtils.readByte(context.resources.assets.open("blank.png"))
    private val dummy = WebResourceResponse("text/plain", "UTF-8", EmptyInputStream())

    private val manager: AdBlockManager = AdBlockManager(context)
    private var blackList: FastMatcherList? = null
    private var whiteList: FastMatcherList? = null
    private var whitePageList: FastMatcherList? = null
    private var adBlocker: AdBlocker? = null

    init {
        update()
    }

    fun update() {
        launch {
            lateinit var blackFilter: FilterMatcher
            lateinit var whiteFilter: FilterMatcher
            manager.getFastMatcherCachedList(AdBlockManager.BLACK_TABLE_NAME).let {
                blackList = it
                blackFilter = FilterMatcher(it.matcherList)
            }

            manager.getFastMatcherCachedList(AdBlockManager.WHITE_TABLE_NAME).let {
                whiteList = it
                whiteFilter = FilterMatcher(it.matcherList)
            }

            manager.getFastMatcherCachedList(AdBlockManager.WHITE_PAGE_TABLE_NAME).let {
                whitePageList = it
                adBlocker = AdBlocker(blackFilter, whiteFilter, it)
            }
        }
    }

    fun isBlock(pageUri: Uri, uri: Uri): Boolean {
        return adBlocker?.isBlock(pageUri, uri) ?: false
    }

    fun onResume() {
        launch {
            manager.updateOrder(AdBlockManager.BLACK_TABLE_NAME, blackList)
            manager.updateOrder(AdBlockManager.WHITE_TABLE_NAME, whiteList)
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

    private class EmptyInputStream : InputStream() {
        @Throws(IOException::class)
        override fun read(): Int = -1
    }
}
