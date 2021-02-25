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
import android.webkit.WebResourceResponse
import jp.hazuki.yuzubrowser.adblock.core.*
import jp.hazuki.yuzubrowser.adblock.filter.ContentFilter
import jp.hazuki.yuzubrowser.adblock.filter.abp.ABP_PREFIX_ALLOW
import jp.hazuki.yuzubrowser.adblock.filter.abp.ABP_PREFIX_DENY
import jp.hazuki.yuzubrowser.adblock.filter.abp.ABP_PREFIX_DISABLE_ELEMENT_PAGE
import jp.hazuki.yuzubrowser.adblock.filter.unified.element.ElementContainer
import jp.hazuki.yuzubrowser.adblock.filter.unified.getFilterDir
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpDao
import jp.hazuki.yuzubrowser.adblock.repository.original.AdBlockManager
import jp.hazuki.yuzubrowser.core.utility.extensions.getNoCacheResponse
import jp.hazuki.yuzubrowser.core.utility.utils.IOUtils
import jp.hazuki.yuzubrowser.core.utility.utils.getMimeType
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.util.concurrent.CountDownLatch

class AdBlockController(private val context: Context, private val abpDao: AbpDao) {
    private val dummyImage: ByteArray = IOUtils.readByte(context.resources.assets.open("blank.png"))
    private val dummy = WebResourceResponse("text/plain", "UTF-8", EmptyInputStream())

    private val manager: AdBlockManager = AdBlockManager(context)
    private var adBlocker: Blocker? = null
    private var elementBlocker: CosmeticFiltering? = null
    private var isAbpIgnoreGenericElement = false
    private var waitForLoading: CountDownLatch? = null

    init {
        update()
    }

    fun update() {
        waitForLoading = CountDownLatch(1)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val abpLoader = AbpLoader(context.getFilterDir(), abpDao.getAll())
                val deny = async {
                    FilterContainer().also {
                        abpLoader.loadAll(ABP_PREFIX_DENY).forEach(it::plusAssign)
                    }
                }
                val allow = async {
                    FilterContainer().also {
                        abpLoader.loadAll(ABP_PREFIX_ALLOW).forEach(it::plusAssign)
                    }
                }
                val allowPage = async {
                    FilterContainer().also {
                        manager.getCachedMatcherList(AdBlockManager.ALLOW_PAGE_TABLE_NAME).forEach(it::plusAssign)
                    }
                }

                if (AppPrefs.isAbpUseElementHide.get()) {
                    val disableCosmetic = async {
                        FilterContainer().also {
                            abpLoader.loadAll(ABP_PREFIX_DISABLE_ELEMENT_PAGE).forEach(it::plusAssign)
                        }
                    }
                    val elementFilter = async {
                        ElementContainer().also {
                            abpLoader.loadAllElementFilter().forEach(it::plusAssign)
                        }
                    }

                    elementBlocker = CosmeticFiltering(disableCosmetic.await(), elementFilter.await())
                }

                adBlocker = Blocker(allowPage.await(), allow.await(), deny.await())

                isAbpIgnoreGenericElement = AppPrefs.isAbpIgnoreGenericElement.get()
            } finally {
                waitForLoading?.countDown()
                waitForLoading = null
            }
        }
    }

    fun isWhitePage(pageUrl: Uri): Boolean {
        waitForLoading?.await()
        return adBlocker?.isWhitePage(pageUrl) ?: false
    }

    fun isBlock(contentRequest: ContentRequest): ContentFilter? {
        waitForLoading?.await()
        return adBlocker?.isBlock(contentRequest)
    }

    fun createDummy(uri: Uri): WebResourceResponse {
        val mimeType = getMimeType(uri.toString())
        return if (mimeType.startsWith("image/")) {
            WebResourceResponse("image/png", null, ByteArrayInputStream(dummyImage))
        } else {
            dummy
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

    fun loadScript(url: Uri): String? {
        val cosmetic = elementBlocker ?: return null

        return cosmetic.loadScript(url)
    }
}
