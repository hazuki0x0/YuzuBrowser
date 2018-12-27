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
import android.view.KeyEvent
import android.webkit.WebBackForwardList
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabData
import jp.hazuki.yuzubrowser.legacy.utils.WebViewUtils
import java.util.*

class CacheWebView(context: Context) : AbstractCacheWebView(context) {

    private val mList = ArrayList<TabData>()

    override val currentTab: TabData
        get() = mList[current]

    override val tabs: Collection<TabData>
        get() = mList

    override val tabSize: Int
        get() = mList.size

    override fun removeCurrentTab(tab: TabData) {
        mList.removeAt(current)

        val to = mList[--current]
        removeAllViews()
        addView(to.mWebView.view)
        move(tab, to)
    }

    override val webChromeClientWrapper = object : CustomWebChromeClientWrapper(this) {
        override fun onProgressChanged(web: CustomWebView, newProgress: Int) {
            val data = webView2Data(web)
            data?.onProgressChanged(newProgress)
            if (web != mList[current].mWebView) return
            super.onProgressChanged(web, newProgress)
        }

        override fun onReceivedTitle(web: CustomWebView, title: String) {
            val data = webView2Data(web)
            data?.onReceivedTitle(title)
            if (web != mList[current].mWebView) return
            super.onReceivedTitle(web, title)
        }

        override fun onReceivedIcon(web: CustomWebView, icon: Bitmap) {
            if (web != mList[current].mWebView) return
            super.onReceivedIcon(web, icon)
        }
    }

    override val webViewClientWrapper = object : CustomWebViewClientWrapper(this) {
        override fun onScaleChanged(view: CustomWebView, oldScale: Float, newScale: Float) {
            if (view != mList[current].mWebView) return
            super.onScaleChanged(view, oldScale, newScale)
        }

        override fun onUnhandledKeyEvent(view: CustomWebView, event: KeyEvent) {
            if (view != mList[current].mWebView) return
            super.onUnhandledKeyEvent(view, event)
        }

        override fun onPageFinished(web: CustomWebView, url: String) {
            val data = webView2Data(web)
            data?.onPageFinished(web, url)
            if (web != mList[current].mWebView) return
            super.onPageFinished(web, url)
        }

        override fun onPageStarted(web: CustomWebView, url: String, favicon: Bitmap?) {
            val data = webView2Data(web)
            data?.onPageStarted(url, favicon)
            if (web != mList[current].mWebView) return
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

    init {
        val web = SwipeWebView(context)
        mList.add(TabData(web))
        addView(web)
    }

    private fun webView2Data(web: CustomWebView): TabData? = mList.firstOrNull { it.mWebView == web }

    override fun newTab(url: String?, additionalHttpHeaders: MutableMap<String, String>) {
        val from = mList[current]
        val to = TabData(SwipeWebView(context))
        for (i in mList.size - 1 downTo current + 1) {
            mList[i].mWebView.destroy()
            mList.removeAt(i)
        }

        removeAllViews()
        mList.add(to)
        addView(to.mWebView.view)
        settingWebView(from.mWebView, to.mWebView)
        val currentUrl = from.url
        if (currentUrl != null) {
            additionalHttpHeaders.put("Referer", currentUrl)
        }
        to.mWebView.loadUrl(url, sHeaderMap)
        ++current
        move(from, to)
    }

    @Synchronized override fun clearHistory() {
        val data = mList[current]
        data.mWebView.clearHistory()
        mList.clear()
        mList.add(data)
        current = 0
    }

    override fun copyMyBackForwardList(): CustomWebBackForwardList {
        val list = CustomWebBackForwardList(current, mList.size)
        mList.asSequence()
                .map { it.mWebView }
                .mapTo(list) {
                    CustomWebHistoryItem(it.url ?: "", it.originalUrl, it.title, it.favicon)
                }
        return list
    }

    override fun resetCurrentTab() = mList[current]

    @Synchronized override fun restoreState(inState: Bundle): WebBackForwardList? {
        isFirst = false

        val from = mList[current]
        mList.clear()
        removeAllViews()

        val all = inState.getInt("CacheWebView.WEB_ALL_COUNT")
        current = inState.getInt("CacheWebView.WEB_CURRENT_COUNT")

        for (i in 0 until all) {
            val web = TabData(SwipeWebView(context))
            web.mWebView.onPause()
            mList.add(web)
            if (i == current)
                addView(web.mWebView.view)
            web.mWebView.restoreState(inState.getBundle("CacheWebView.WEB_NO" + i))
            settingWebView(from.mWebView, web.mWebView)
        }
        move(from, mList[current])
        return null
    }

    @Synchronized override fun saveState(outState: Bundle): WebBackForwardList? {
        outState.putBoolean("CacheWebView.IsCacheWebView", true)
        outState.putInt("CacheWebView.WEB_ALL_COUNT", mList.size)
        outState.putInt("CacheWebView.WEB_CURRENT_COUNT", current)
        for ((i, web) in mList.withIndex()) {
            val state = Bundle()
            web.mWebView.saveState(state)
            outState.putBundle("CacheWebView.WEB_NO" + i, state)
        }
        return null
    }

    override val isBackForwardListEmpty: Boolean
        get() = isFirst || current == 0 && mList.size == 1 && mList[0].mWebView.url == null

    companion object {
        @JvmStatic
        fun isBundleCacheWebView(state: Bundle): Boolean =
                state.getBoolean("CacheWebView.IsCacheWebView", false)
    }
}
