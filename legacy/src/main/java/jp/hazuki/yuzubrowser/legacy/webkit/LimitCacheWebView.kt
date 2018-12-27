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

package jp.hazuki.yuzubrowser.legacy.webkit

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.LongSparseArray
import android.view.KeyEvent
import android.webkit.WebBackForwardList
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabCache
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabData
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabIndexData
import jp.hazuki.yuzubrowser.legacy.utils.JsonUtils
import jp.hazuki.yuzubrowser.legacy.utils.WebViewUtils
import java.io.IOException
import java.io.StringWriter
import java.util.*

class LimitCacheWebView(context: Context) : AbstractCacheWebView(context), TabCache.OnCacheOverFlowListener<TabData> {
    private val tabIndexList = ArrayList<TabIndexData>()
    private val tabSaveData = LongSparseArray<Bundle>()
    private val tabCache: TabCache<TabData> = TabCache(AppData.fast_back_cache_size.get(), this)
    override var currentTab: TabData = SwipeWebView(context).let { web ->
        val data = TabData(web)
        tabIndexList.add(data.tabIndexData)
        tabCache[data.id] = data
        addView(web)
        data
    }

    override val tabs: Collection<TabData>
        get() = tabCache.values

    override val tabSize: Int
        get() = tabIndexList.size

    override val webChromeClientWrapper = object : CustomWebChromeClientWrapper(this) {
        override fun onProgressChanged(web: CustomWebView, newProgress: Int) {
            val data = webView2data(web)
            data?.onProgressChanged(newProgress)
            if (web != currentTab.mWebView) return
            super.onProgressChanged(web, newProgress)
        }

        override fun onReceivedTitle(web: CustomWebView, title: String) {
            val data = webView2data(web)
            data?.onReceivedTitle(title)
            if (web != currentTab.mWebView) return
            super.onReceivedTitle(web, title)
        }

        override fun onReceivedIcon(web: CustomWebView, icon: Bitmap) {
            if (web != currentTab.mWebView) return
            super.onReceivedIcon(web, icon)
        }
    }

    override val webViewClientWrapper = object : CustomWebViewClientWrapper(this) {
        override fun onScaleChanged(view: CustomWebView, oldScale: Float, newScale: Float) {
            if (view != currentTab.mWebView) return
            super.onScaleChanged(view, oldScale, newScale)
        }

        override fun onUnhandledKeyEvent(view: CustomWebView, event: KeyEvent) {
            if (view != currentTab.mWebView) return
            super.onUnhandledKeyEvent(view, event)
        }

        override fun onPageFinished(web: CustomWebView, url: String) {
            val data = webView2data(web)
            data?.onPageFinished(web, url)
            if (web != currentTab.mWebView) return
            super.onPageFinished(web, url)
        }

        override fun onPageStarted(web: CustomWebView, url: String, favicon: Bitmap?) {
            val data = webView2data(web)
            data?.onPageStarted(url, favicon)
            if (web != currentTab.mWebView) return
            super.onPageStarted(web, url, favicon)
        }

        override fun shouldOverrideUrlLoading(web: CustomWebView, url: String, uri: Uri): Boolean {
            if (WebViewUtils.shouldLoadSameTabAuto(url)) return false
            if (super.shouldOverrideUrlLoading(web, url, uri)) {
                return true
            } else {
                if (WebViewUtils.isRedirect(web)) return false
                if (web.url == null) return false
                newTab(url)
                return true
            }
        }
    }

    override fun removeCurrentTab(tab: TabData) {
        tabIndexList.removeAt(current)
        moveTo(currentTab, false)
    }

    private fun webView2data(web: CustomWebView): TabData? = tabCache[web.identityId]

    override fun newTab(url: String?, additionalHttpHeaders: MutableMap<String, String>) {
        val from = currentTab
        val to = makeWebView()
        val currentUrl = from.url
        if (currentUrl != null) {
            additionalHttpHeaders["Referer"] = currentUrl
        }
        to.mWebView.loadUrl(url, sHeaderMap)

        for (i in tabIndexList.size - 1 downTo current + 1) {
            removeWebView(i)
        }

        tabIndexList.add(to.tabIndexData)
        tabCache[to.id] = to
        moveTo(from, true)
    }

    private fun makeWebView(): TabData {
        val to = TabData(SwipeWebView(context))
        settingWebView(currentTab.mWebView, to.mWebView)
        return to
    }

    private fun removeWebView(index: Int) {
        val now = tabIndexList.removeAt(index)
        tabCache.remove(now.id)
        tabSaveData.remove(now.id)
    }

    private fun getWebView(index: Int): TabData {
        val now = tabIndexList[index]
        var data: TabData? = tabCache[now.id]
        if (data == null) {
            data = now.getTabData(SwipeWebView(context))
            settingWebView(currentTab.mWebView, data!!.mWebView)
            val state = tabSaveData.get(now.id)
            if (state != null) {
                data.mWebView.restoreState(state)
            } else {
                if (now.url != null)
                    data.mWebView.loadUrl(now.url)
            }
            tabCache[now.id] = data
        }
        return data
    }

    @Synchronized override fun clearHistory() {
        val data = currentTab
        data.mWebView.clearHistory()
        tabIndexList.clear()
        tabCache.clear()
        tabIndexList.add(data.tabIndexData)
        tabCache[data.id] = data
        current = 0
    }

    override fun copyMyBackForwardList(): CustomWebBackForwardList {
        val list = CustomWebBackForwardList(current, tabIndexList.size)
        tabIndexList.forEach {
            val data = tabCache[it.id]
            val item: CustomWebHistoryItem
            item = if (data == null) {
                CustomWebHistoryItem(it.url, it.url, it.title, null)
            } else {
                CustomWebHistoryItem(data.url
                        ?: "", data.originalUrl, data.title, data.mWebView.favicon)
            }
            list.add(item)
        }
        return list
    }

    override fun resetCurrentTab(): TabData {
        currentTab = getWebView(current)
        return currentTab
    }

    @Synchronized override fun restoreState(inState: Bundle): WebBackForwardList? {
        isFirst = false

        val from = currentTab
        tabCache.clear()
        tabSaveData.clear()
        removeAllViews()

        current = inState.getInt(BUNDLE_CURRENT)
        val data = inState.getString(BUNDLE_TAB_DATA)
        loadIndexData(data)

        for (i in tabIndexList.indices) {
            val indexData = tabIndexList[i]
            val state = inState.getBundle(BUNDLE_WEB_NO + i)
            checkNotNull(state)
            tabSaveData.put(indexData.id, state)

            if (inState.getBoolean(BUNDLE_LOADED + indexData.id, false)) {
                val web = indexData.getTabData(SwipeWebView(context))
                web.mWebView.onPause()
                tabCache[id] = web
                if (i == current) {
                    addView(web.mWebView.view)
                    currentTab = web
                }

                web.mWebView.restoreState(state)
                settingWebView(from.mWebView, web.mWebView)
            }
        }


        move(from, currentTab)
        return null
    }

    @Synchronized override fun saveState(outState: Bundle): WebBackForwardList? {
        outState.putBoolean(BUNDLE_IS_FAST_BACK, true)
        outState.putInt(BUNDLE_CURRENT, current)
        outState.putString(BUNDLE_TAB_DATA, saveIndexData())

        for (tabData in tabCache.values) {
            val state = Bundle()
            tabData.mWebView.saveState(state)
            tabSaveData.put(tabData.id, state)
            outState.putBoolean(BUNDLE_LOADED + tabData.id, true)
        }

        var i = 0
        while (tabIndexList.size > i) {
            val state = tabSaveData.get(tabIndexList[i].id)
            outState.putBundle(BUNDLE_WEB_NO + i, state)
            i++
        }

        return null
    }

    private fun saveIndexData(): String {
        val writer = StringWriter()
        try {
            JsonUtils.getFactory().createGenerator(writer).use { generator ->
                generator.writeStartArray()
                for (data in tabIndexList) {
                    generator.writeStartObject()
                    generator.writeNumberField(JSON_NAME_ID, data.id)
                    generator.writeStringField(JSON_NAME_URL, data.url)
                    generator.writeStringField(JSON_NAME_TITLE, data.title)
                    generator.writeEndObject()
                }
                generator.writeEndArray()
                generator.flush()
                return writer.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ""
    }

    private fun loadIndexData(data: String?) {
        tabIndexList.clear()
        try {
            JsonUtils.getFactory().createParser(data!!).use { parser ->
                // 配列の処理
                if (parser.nextToken() == JsonToken.START_ARRAY) {
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        // 各オブジェクトの処理
                        if (parser.currentToken == JsonToken.START_OBJECT) {
                            var id: Long = -1
                            var url: String? = null
                            var title: String? = null
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                val name = parser.currentName
                                parser.nextToken()
                                if (name != null) {
                                    when (name) {
                                        JSON_NAME_ID -> id = parser.longValue
                                        JSON_NAME_URL -> url = parser.text
                                        JSON_NAME_TITLE -> {
                                            title = parser.text
                                            parser.skipChildren()
                                        }
                                        else -> parser.skipChildren()
                                    }
                                }
                            }
                            tabIndexList.add(TabIndexData(url, title, 0, id, 0))
                        } else {
                            parser.skipChildren()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override val isBackForwardListEmpty: Boolean
        get() = isFirst || current == 0 && tabIndexList.size == 1 && tabIndexList[0].url == null

    override fun onPreferenceReset() {
        tabCache.setSize(AppData.fast_back_cache_size.get())
    }

    override fun onCacheOverflow(tabData: TabData) {
        val bundle = Bundle()
        tabData.mWebView.saveState(bundle)
        tabSaveData.put(tabData.id, bundle)
        tabData.mWebView.setEmbeddedTitleBarMethod(null)
        tabData.mWebView.destroy()
    }

    companion object {
        private const val BUNDLE_TAB_DATA = "FastBack.TAB_DATA"
        private const val BUNDLE_WEB_NO = "FastBack.WEB_NO"
        private const val BUNDLE_LOADED = "FastBack.LOADED_"
        private const val BUNDLE_IS_FAST_BACK = "FastBack.IsFastBack"
        private const val BUNDLE_CURRENT = "FastBack.WEB_CURRENT_COUNT"

        @JvmStatic
        fun isBundleFastBackWebView(state: Bundle): Boolean =
                state.getBoolean(BUNDLE_IS_FAST_BACK, false)

        private const val JSON_NAME_ID = "id"
        private const val JSON_NAME_URL = "url"
        private const val JSON_NAME_TITLE = "t"
    }
}
