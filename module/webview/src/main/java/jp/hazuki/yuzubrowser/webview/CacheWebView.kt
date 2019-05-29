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

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebBackForwardList
import jp.hazuki.yuzubrowser.webview.page.WebViewPage
import jp.hazuki.yuzubrowser.webview.utility.WebViewUtility
import java.util.*

internal class CacheWebView(context: Context) : AbstractCacheWebView(context), WebViewUtility {

    private val mList = ArrayList<WebViewPage>()

    override val currentPage: WebViewPage
        get() = mList[current]

    override val tabs: Collection<WebViewPage>
        get() = mList

    override val tabSize: Int
        get() = mList.size

    override fun removeCurrentTab(tab: WebViewPage) {
        mList.removeAt(current)

        val to = mList[--current]
        removeAllViews()
        addView(to.webView.view)
        move(tab, to)
    }

    override val webChromeClientWrapper = object : CustomWebChromeClientWrapper(this) {
        override fun onProgressChanged(web: CustomWebView, newProgress: Int) {
            if (web != mList[current].webView) return
            super.onProgressChanged(web, newProgress)
        }

        override fun onReceivedTitle(web: CustomWebView, title: String) {
            val data = webView2Data(web)
            data?.title = title
            if (web != mList[current].webView) return
            super.onReceivedTitle(web, title)
        }

        override fun onReceivedIcon(web: CustomWebView, icon: Bitmap) {
            if (web != mList[current].webView) return
            super.onReceivedIcon(web, icon)
        }
    }

    override val webViewClientWrapper = object : CustomWebViewClientWrapper(this) {
        override fun onScaleChanged(view: CustomWebView, oldScale: Float, newScale: Float) {
            if (view != mList[current].webView) return
            super.onScaleChanged(view, oldScale, newScale)
        }

        override fun onUnhandledKeyEvent(view: CustomWebView, event: KeyEvent) {
            if (view != mList[current].webView) return
            super.onUnhandledKeyEvent(view, event)
        }

        override fun onPageFinished(web: CustomWebView, url: String) {
            val data = webView2Data(web)
            data?.onPageFinished()
            if (web != mList[current].webView) return
            super.onPageFinished(web, url)
        }

        override fun onPageStarted(web: CustomWebView, url: String, favicon: Bitmap?) {
            val data = webView2Data(web)
            data?.onPageStarted(url)
            if (web != mList[current].webView) return
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
            if (web != mList[current].webView) return
            super.onPageCommitVisible(web, url)
        }
    }

    init {
        val web = SwipeWebView(context)
        mList.add(WebViewPage(web))
        addView(web)
    }

    private fun webView2Data(web: CustomWebView): WebViewPage? = mList.firstOrNull { it.webView == web }

    override fun newTab(url: String, additionalHttpHeaders: Map<String, String>) {
        val from = mList[current]
        val to = WebViewPage(SwipeWebView(context))
        for (i in mList.size - 1 downTo current + 1) {
            mList[i].webView.destroy()
            mList.removeAt(i)
        }

        removeAllViews()
        mList.add(to)
        addView(to.webView.view)
        from.webView.copySettingsTo(to.webView)
        val currentUrl = from.url
        to.url = url
        ++current
        move(from, to)

        if (emptyMap == additionalHttpHeaders || additionalHttpHeaders.isEmpty()) {
            to.webView.loadUrl(url, getReferrerMap(currentUrl))
        } else {
            to.webView.loadUrl(url, additionalHttpHeaders.getHeaderMap(currentUrl))
        }
    }

    @Synchronized override fun clearHistory() {
        val data = mList[current]
        data.webView.clearHistory()
        mList.clear()
        mList.add(data)
        current = 0
    }

    override fun copyMyBackForwardList(): CustomWebBackForwardList {
        val list = CustomWebBackForwardList(current, mList.size)
        mList.asSequence()
                .map { it.webView }
                .mapTo(list) {
                    CustomWebHistoryItem(it.url
                            ?: "", it.originalUrl, it.title, it.favicon)
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
            val web = WebViewPage(SwipeWebView(context))
            web.webView.onPause()
            mList.add(web)
            if (i == current)
                addView(web.webView.view)
            web.webView.restoreState(inState.getBundle("CacheWebView.WEB_NO$i"))
            from.webView.copySettingsTo(web.webView)
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
            web.webView.saveState(state)
            outState.putBundle("CacheWebView.WEB_NO$i", state)
        }
        return null
    }

    override val isBackForwardListEmpty: Boolean
        get() = isFirst || current == 0 && mList.size == 1 && mList[0].webView.url == null

    companion object {
        @JvmStatic
        fun isBundleCacheWebView(state: Bundle): Boolean =
                state.getBoolean("CacheWebView.IsCacheWebView", false)
    }
}
