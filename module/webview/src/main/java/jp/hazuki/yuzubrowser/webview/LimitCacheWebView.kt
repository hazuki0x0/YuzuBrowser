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

package jp.hazuki.yuzubrowser.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.LongSparseArray
import android.view.KeyEvent
import android.webkit.WebBackForwardList
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jp.hazuki.yuzubrowser.core.cache.LRUCache
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.webview.page.Page
import jp.hazuki.yuzubrowser.webview.page.WebViewPage
import jp.hazuki.yuzubrowser.webview.utility.WebViewUtility
import java.util.*

@SuppressLint("ViewConstructor")
internal class LimitCacheWebView(context: Context, private val moshi: Moshi) : AbstractCacheWebView(context), LRUCache.OnCacheOverFlowListener<WebViewPage>, WebViewUtility {
    private val tabIndexList = ArrayList<Page>()
    private val tabSaveData = LongSparseArray<Bundle>()
    private val tabCache: LRUCache<Long, WebViewPage> = LRUCache(AppPrefs.fast_back_cache_size.get(), this)
    override var currentPage: WebViewPage = SwipeWebView(context).let { web ->
        val data = WebViewPage(web)
        tabIndexList.add(data.page)
        tabCache[data.id] = data
        addView(web)
        data
    }

    override val tabs: Collection<WebViewPage>
        get() = tabCache.values

    override val tabSize: Int
        get() = tabIndexList.size

    override val webChromeClientWrapper = object : CustomWebChromeClientWrapper(this) {
        override fun onProgressChanged(web: CustomWebView, newProgress: Int) {
            if (web != currentPage.webView) return
            super.onProgressChanged(web, newProgress)
        }

        override fun onReceivedTitle(web: CustomWebView, title: String) {
            val data = webView2data(web)
            data?.title = title
            if (web != currentPage.webView) return
            super.onReceivedTitle(web, title)
        }

        override fun onReceivedIcon(web: CustomWebView, icon: Bitmap) {
            if (web != currentPage.webView) return
            super.onReceivedIcon(web, icon)
        }
    }

    override val webViewClientWrapper = object : CustomWebViewClientWrapper(this) {
        override fun onScaleChanged(view: CustomWebView, oldScale: Float, newScale: Float) {
            if (view != currentPage.webView) return
            super.onScaleChanged(view, oldScale, newScale)
        }

        override fun onUnhandledKeyEvent(view: CustomWebView, event: KeyEvent) {
            if (view != currentPage.webView) return
            super.onUnhandledKeyEvent(view, event)
        }

        override fun onPageFinished(web: CustomWebView, url: String) {
            val data = webView2data(web)
            data?.onPageFinished()
            if (web != currentPage.webView) return
            super.onPageFinished(web, url)
        }

        override fun onPageStarted(web: CustomWebView, url: String, favicon: Bitmap?) {
            val data = webView2data(web)
            data?.onPageStarted(url)
            if (web != currentPage.webView) return
            super.onPageStarted(web, url, favicon)
        }

        override fun shouldOverrideUrlLoading(web: CustomWebView, url: String, uri: Uri): Boolean {
            if (url.shouldLoadSameTabAuto()) return false
            if (super.shouldOverrideUrlLoading(web, url, uri)) {
                return true
            } else {
                if (web.isRedirect) return false
                if (web.url == null) return false
                newTab(url)
                return true
            }
        }

        override fun onPageCommitVisible(web: CustomWebView, url: String) {
            if (web != currentPage.webView) return
            super.onPageCommitVisible(web, url)
        }
    }

    override fun removeCurrentTab(tab: WebViewPage) {
        tabIndexList.removeAt(current)
        moveTo(currentPage, false)
    }

    private fun webView2data(web: CustomWebView): WebViewPage? = tabCache[web.identityId]

    override fun newTab(url: String, additionalHttpHeaders: Map<String, String>) {
        val from = currentPage
        val to = makeWebView()
        val currentUrl = from.url
        to.url = url

        for (i in tabIndexList.size - 1 downTo current + 1) {
            removeWebView(i)
        }

        tabIndexList.add(to.page)
        tabCache[to.id] = to
        moveTo(from, true)

        if (emptyMap == additionalHttpHeaders || additionalHttpHeaders.isEmpty()) {
            to.webView.loadUrl(url, getReferrerMap(currentUrl))
        } else {
            to.webView.loadUrl(url, additionalHttpHeaders.getHeaderMap(currentUrl))
        }
    }

    private fun makeWebView(): WebViewPage {
        val to = WebViewPage(SwipeWebView(context))
        currentPage.webView.copySettingsTo(to.webView)
        return to
    }

    private fun removeWebView(index: Int) {
        val now = tabIndexList.removeAt(index)
        tabCache.remove(now.id)
        tabSaveData.remove(now.id)
    }

    private fun getWebView(index: Int): WebViewPage {
        val now = tabIndexList[index]
        var data: WebViewPage? = tabCache[now.id]
        if (data == null) {
            data = WebViewPage(SwipeWebView(context, now.id), now)
            currentPage.webView.copySettingsTo(data.webView)
            val state = tabSaveData.get(now.id)
            if (state != null) {
                data.webView.restoreState(state)
            } else {
                if (now.url != null)
                    data.webView.loadUrl(now.url)
            }
            tabCache[now.id] = data
        }
        return data
    }

    @Synchronized override fun clearHistory() {
        val data = currentPage
        data.webView.clearHistory()
        tabIndexList.clear()
        tabCache.clear()
        tabIndexList.add(data.page)
        tabCache[data.id] = data
        current = 0
    }

    override fun copyMyBackForwardList(): CustomWebBackForwardList {
        val list = CustomWebBackForwardList(current, tabIndexList.size)
        tabIndexList.forEach {
            val data = tabCache[it.id]
            val item: CustomWebHistoryItem
            item = if (data == null) {
                CustomWebHistoryItem(it.url ?: "", it.url, it.title, null)
            } else {
                CustomWebHistoryItem(data.url
                        ?: "", data.originalUrl, data.title, data.webView.favicon)
            }
            list.add(item)
        }
        return list
    }

    override fun resetCurrentTab(): WebViewPage {
        currentPage = getWebView(current)
        return currentPage
    }

    @Synchronized override fun restoreState(inState: Bundle): WebBackForwardList? {
        isFirst = false

        val from = currentPage
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
                val web = WebViewPage(SwipeWebView(context, indexData.id), indexData)
                web.webView.onPause()
                tabCache[id] = web
                if (i == current) {
                    addView(web.webView.view)
                    currentPage = web
                }

                web.webView.restoreState(state)
                from.webView.copySettingsTo(web.webView)
            }
        }


        move(from, currentPage)
        return null
    }

    @Synchronized override fun saveState(outState: Bundle): WebBackForwardList? {
        outState.putBoolean(BUNDLE_IS_FAST_BACK, true)
        outState.putInt(BUNDLE_CURRENT, current)
        outState.putString(BUNDLE_TAB_DATA, saveIndexData())

        for (tabData in tabCache.values) {
            val state = Bundle()
            tabData.webView.saveState(state)
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
        val type = Types.newParameterizedType(List::class.java, Page::class.java)
        val adapter = moshi.adapter<List<Page>>(type)
        return adapter.toJson(tabIndexList)
    }

    private fun loadIndexData(data: String?) {
        tabIndexList.clear()
        if (data != null) {
            val type = Types.newParameterizedType(List::class.java, Page::class.java)
            val adapter = moshi.adapter<List<Page>>(type)
            val list = adapter.fromJson(data)
            if (list != null) {
                tabIndexList.addAll(list)
            }
        }
    }

    override val isBackForwardListEmpty: Boolean
        get() = isFirst || current == 0 && tabIndexList.size == 1 && tabIndexList[0].url == null

    override fun onPreferenceReset() {
        tabCache.cacheSize = AppPrefs.fast_back_cache_size.get()
    }

    override fun onCacheOverflow(tabData: WebViewPage) {
        val bundle = Bundle()
        tabData.webView.saveState(bundle)
        tabSaveData.put(tabData.id, bundle)
        tabData.webView.setEmbeddedTitleBarMethod(null)
        tabData.webView.destroy()
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
