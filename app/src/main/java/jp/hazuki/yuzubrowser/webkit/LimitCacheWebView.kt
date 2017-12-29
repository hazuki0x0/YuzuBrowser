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

package jp.hazuki.yuzubrowser.webkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.print.PrintDocumentAdapter
import android.util.LongSparseArray
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.webkit.WebView.HitTestResult
import android.widget.FrameLayout
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.browser.BrowserManager
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.tab.manager.TabCache
import jp.hazuki.yuzubrowser.tab.manager.TabData
import jp.hazuki.yuzubrowser.tab.manager.TabIndexData
import jp.hazuki.yuzubrowser.utils.JsonUtils
import jp.hazuki.yuzubrowser.utils.WebViewUtils
import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector
import jp.hazuki.yuzubrowser.webkit.listener.OnScrollChangedListener
import jp.hazuki.yuzubrowser.webkit.listener.OnWebStateChangeListener
import java.io.IOException
import java.io.StringWriter
import java.util.*

class LimitCacheWebView(context: Context) : FrameLayout(context), CustomWebView, TabCache.OnCacheOverFlowListener<TabData> {
    private val tabIndexList = ArrayList<TabIndexData>()
    private val tabSaveData = LongSparseArray<Bundle>()
    private val tabCache: TabCache<TabData> = TabCache(AppData.fast_back_cache_size.get(), this)
    private lateinit var currentTab: TabData
    private var id = System.currentTimeMillis()
    private var mCurrent = 0
    private var isFirst = true
    private var mTitleBar: View? = null
    private var webLayerType: Int = 0
    private var webLayerPaint: Paint? = null
    private var acceptThirdPartyCookies: Boolean = false
    private var verticalScrollBarEnabled: Boolean = false
    private var mStateChangeListener: OnWebStateChangeListener? = null
    private var mOnScrollChangedListener: OnScrollChangedListener? = null
    private var mScrollBarListener: OnScrollChangedListener? = null
    private var mDownloadListener: DownloadListener? = null
    private var scrollableHeight: (() -> Int)? = null
    private val mDownloadListenerWrapper = DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
        synchronized(this@LimitCacheWebView) {
            if (mCurrent >= 1) {
                val from = currentTab
                if (from.url == null || from.url == url) {
                    tabIndexList.removeAt(mCurrent)
                    moveTo(false)

                    from.mWebView.destroy()
                }
            }
        }
        mDownloadListener?.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength)
    }

    private val mWebChromeClientWrapper = object : CustomWebChromeClientWrapper(this) {
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

    private val mWebViewClientWrapper = object : CustomWebViewClientWrapper(this) {
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

    private var mCreateContextMenuListener: CustomOnCreateContextMenuListener? = null
    private val mCreateContextMenuListenerWrapper = object : CustomOnCreateContextMenuListener() {
        override fun onCreateContextMenu(menu: ContextMenu, webView: CustomWebView, menuInfo: ContextMenuInfo?) {
            mCreateContextMenuListener?.onCreateContextMenu(menu, this@LimitCacheWebView as CustomWebView, menuInfo)
        }
    }
    private var mGestureDetector: MultiTouchGestureDetector? = null

    init {
        val web = SwipeWebView(context)
        val data = TabData(web)
        tabIndexList.add(data.tabIndexData)
        tabCache.put(data.id, data)
        currentTab = data

        addView(web)
    }

    private fun webView2data(web: CustomWebView): TabData? = tabCache[web.identityId]

    private fun newTab(url: String?, additionalHttpHeaders: MutableMap<String, String> = sHeaderMap) {
        val to = makeWebView()
        val currentTab = currentTab
        val currentUrl = currentTab.url
        if (currentUrl != null) {
            additionalHttpHeaders.put("Referer", currentUrl)
        }
        to.mWebView.loadUrl(url, sHeaderMap)

        for (i in tabIndexList.size - 1 downTo mCurrent + 1) {
            removeWebView(i)
        }

        tabIndexList.add(to.tabIndexData)
        tabCache.put(to.id, to)
        moveTo(true)
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
            tabCache.put(now.id, data)
        }
        return data
    }

    private fun moveTo(next: Boolean): TabData {
        mCurrent += if (next) 1 else -1

        val from = currentTab
        currentTab = getWebView(mCurrent)
        removeAllViews()
        addView(currentTab.mWebView.view)
        move(from, currentTab)
        return currentTab
    }

    private fun canGoBackType(): Int {
        if (currentTab.mWebView.canGoBack()) return CAN_INTERNAL_MOVE
        return if (mCurrent >= 1) CAN_EXTERNAL_MOVE else CAN_NOT_MOVE
    }

    override fun canGoBack(): Boolean = canGoBackType() != CAN_NOT_MOVE

    @Synchronized override fun canGoBackOrForward(steps: Int): Boolean {
        if (steps == 0) return true
        return if (steps < 0) {
            mCurrent >= -steps
        } else {
            mCurrent + steps < tabIndexList.size
        }
    }

    private fun canGoForwardType(): Int {
        if (currentTab.mWebView.canGoForward()) return CAN_INTERNAL_MOVE
        return if (mCurrent + 1 < tabIndexList.size) CAN_EXTERNAL_MOVE else CAN_NOT_MOVE
    }

    override fun canGoForward(): Boolean = canGoForwardType() != CAN_NOT_MOVE

    override fun clearCache(includeDiskFiles: Boolean) {
        for (web in tabCache.values) {
            web.mWebView.clearCache(true)
        }
    }

    override fun clearFormData() {
        for (web in tabCache.values) {
            web.mWebView.clearFormData()
        }
    }

    @Synchronized override fun clearHistory() {
        val data = currentTab
        data.mWebView.clearHistory()
        tabIndexList.clear()
        tabCache.clear()
        tabIndexList.add(data.tabIndexData)
        tabCache.put(data.id, data)
        mCurrent = 0
    }

    override fun clearMatches() {
        for (web in tabCache.values) {
            web.mWebView.clearMatches()
        }
    }

    override fun copyMyBackForwardList(): CustomWebBackForwardList {
        val list = CustomWebBackForwardList(mCurrent, tabIndexList.size)
        tabIndexList.forEach {
            val data = tabCache[it.id]
            val item: CustomWebHistoryItem
            item = if (data == null) {
                CustomWebHistoryItem(it.url, it.url, it.title, null)
            } else {
                CustomWebHistoryItem(data.url ?: "", data.originalUrl, data.title, data.mWebView.favicon)
            }
            list.add(item)
        }
        return list
    }

    override fun destroy() {
        mTitleBar = null
        for (web in tabCache.values) {
            web.mWebView.destroy()
        }
    }

    override fun findAllAsync(find: String) {
        currentTab.mWebView.findAllAsync(find)
    }

    override fun setFindListener(listener: WebView.FindListener) {
        currentTab.mWebView.setFindListener(listener)
    }

    override fun findNext(forward: Boolean) {
        currentTab.mWebView.findNext(forward)
    }

    override fun flingScroll(vx: Int, vy: Int) {
        currentTab.mWebView.flingScroll(vx, vy)
    }

    override val favicon: Bitmap?
        get() = currentTab.mWebView.favicon

    override val hitTestResult: HitTestResult?
        get() = currentTab.mWebView.hitTestResult

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun getHttpAuthUsernamePassword(host: String, realm: String): Array<String>? =
            currentTab.mWebView.getHttpAuthUsernamePassword(host, realm)

    override val originalUrl: String?
        get() = currentTab.mWebView.originalUrl

    override val progress: Int
        get() = currentTab.mWebView.progress

    override val settings: WebSettings
        get() = currentTab.mWebView.settings

    override val title: String?
        get() = currentTab.mWebView.title

    override val url: String?
        get() = currentTab.mWebView.url

    @Synchronized override fun goBack() {
        val from = currentTab
        when (canGoBackType()) {
            CAN_EXTERNAL_MOVE -> moveTo(false)
            CAN_INTERNAL_MOVE -> from.mWebView.goBack()
            else -> {
            }
        }
    }

    @Synchronized override fun goBackOrForward(steps: Int) {
        if (!canGoBackOrForward(steps)) return

        removeAllViews()
        val from = currentTab
        mCurrent += steps
        currentTab = getWebView(mCurrent)
        addView(currentTab.mWebView.view)
        move(from, currentTab)
    }

    @Synchronized override fun goForward() {
        val from = currentTab
        when (canGoForwardType()) {
            CAN_EXTERNAL_MOVE -> moveTo(true)
            CAN_INTERNAL_MOVE -> from.mWebView.goForward()
            else -> {
            }
        }
    }

    override fun loadUrl(url: String?) {
        when {
            isFirst -> {
                isFirst = false
                currentTab.mWebView.loadUrl(url)
            }
            WebViewUtils.shouldLoadSameTabUser(url) -> currentTab.mWebView.loadUrl(url)
            url != null -> newTab(url)
        }
    }

    override fun loadUrl(url: String?, additionalHttpHeaders: MutableMap<String, String>?) {
        when {
            isFirst -> {
                isFirst = false
                currentTab.mWebView.loadUrl(url, additionalHttpHeaders)
            }
            WebViewUtils.shouldLoadSameTabUser(url) -> currentTab.mWebView.loadUrl(url, additionalHttpHeaders)
            url != null && additionalHttpHeaders != null -> newTab(url, additionalHttpHeaders)
            url != null -> newTab(url)
        }
    }

    override fun evaluateJavascript(js: String?, callback: ValueCallback<String>?) {
        currentTab.mWebView.evaluateJavascript(js, callback)
    }

    override fun onPause() {
        for (web in tabCache.values) {
            web.mWebView.onPause()
        }
    }

    override fun onResume() {
        /*for(NormalWebView web:mList){
            web.onResume();
		}*/
        currentTab.mWebView.onResume()
    }

    override fun pageDown(bottom: Boolean): Boolean = currentTab.mWebView.pageDown(bottom)

    override fun pageUp(top: Boolean): Boolean = currentTab.mWebView.pageUp(top)

    override fun pauseTimers() {
        currentTab.mWebView.pauseTimers()
    }

    override fun reload() {
        currentTab.mWebView.reload()
    }

    override fun requestWebFocus(): Boolean = currentTab.mWebView.requestWebFocus()

    override fun requestFocusNodeHref(hrefMsg: Message) {
        currentTab.mWebView.requestFocusNodeHref(hrefMsg)
    }

    override fun requestImageRef(msg: Message) {
        currentTab.mWebView.requestImageRef(msg)
    }

    @Synchronized override fun restoreState(inState: Bundle): WebBackForwardList? {
        isFirst = false

        val from = currentTab
        tabCache.clear()
        tabSaveData.clear()
        removeAllViews()

        mCurrent = inState.getInt(BUNDLE_CURRENT)
        val data = inState.getString(BUNDLE_TAB_DATA)
        loadIndexData(data)

        for (i in tabIndexList.indices) {
            val indexData = tabIndexList[i]
            val state = inState.getBundle(BUNDLE_WEB_NO + i)
            tabSaveData.put(indexData.id, state)

            if (inState.getBoolean(BUNDLE_LOADED + indexData.id, false)) {
                val web = indexData.getTabData(SwipeWebView(context))
                web.mWebView.onPause()
                tabCache.put(id, web)
                if (i == mCurrent) {
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

    override fun resumeTimers() {
        currentTab.mWebView.resumeTimers()
    }

    @Synchronized override fun saveState(outState: Bundle): WebBackForwardList? {
        outState.putBoolean(BUNDLE_IS_FAST_BACK, true)
        outState.putInt(BUNDLE_CURRENT, mCurrent)
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

    override fun setDownloadListener(listener: DownloadListener?) {
        mDownloadListener = listener
        for (web in tabCache.values) {
            web.mWebView.setDownloadListener(mDownloadListenerWrapper)
        }
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun setHttpAuthUsernamePassword(host: String, realm: String, username: String, password: String) {
        for (web in tabCache.values) {
            web.mWebView.setHttpAuthUsernamePassword(host, realm, username, password)
        }
    }

    override fun setNetworkAvailable(networkUp: Boolean) {
        for (web in tabCache.values) {
            web.mWebView.setNetworkAvailable(networkUp)
        }
    }

    override fun setMyWebChromeClient(client: CustomWebChromeClient?) {
        mWebChromeClientWrapper.setWebChromeClient(client)
        for (web in tabCache.values) {
            web.mWebView.setMyWebChromeClient(mWebChromeClientWrapper)
        }
    }

    override fun setMyWebViewClient(client: CustomWebViewClient?) {
        mWebViewClientWrapper.setWebViewClient(client)
        for (web in tabCache.values) {
            web.mWebView.setMyWebViewClient(mWebViewClientWrapper)
        }
    }

    override fun stopLoading() {
        currentTab.mWebView.stopLoading()
    }

    override fun zoomIn(): Boolean = currentTab.mWebView.zoomIn()

    override fun zoomOut(): Boolean = currentTab.mWebView.zoomOut()

    override fun setOnMyCreateContextMenuListener(webContextMenuListener: CustomOnCreateContextMenuListener?) {
        mCreateContextMenuListener = webContextMenuListener
        for (web in tabCache.values) {
            web.mWebView.setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper)
        }
    }

    override val webScrollX: Int
        get() = currentTab.mWebView.webScrollX

    override val webScrollY: Int
        get() = currentTab.mWebView.webScrollY

    override val view: View
        get() = this

    override val webView: WebView
        get() = currentTab.mWebView.webView

    override var swipeEnable: Boolean
        get() = currentTab.mWebView.swipeEnable
        set(value) {
            for (web in tabCache.values) {
                web.mWebView.swipeEnable = value
            }
        }

    override fun setGestureDetector(d: MultiTouchGestureDetector?) {
        mGestureDetector = d
        for (web in tabCache.values) {
            web.mWebView.setGestureDetector(d)
        }
    }

    @Synchronized override fun setEmbeddedTitleBarMethod(view: View?): Boolean {
        for (web in tabCache.values) {
            web.mWebView.setEmbeddedTitleBarMethod(null)
        }
        mTitleBar = view
        return currentTab.mWebView.setEmbeddedTitleBarMethod(view)
    }

    override fun notifyFindDialogDismissedMethod(): Boolean {
        for (web in tabCache.values) {
            web.mWebView.notifyFindDialogDismissedMethod()
        }
        return true
    }

    override fun setOverScrollModeMethod(arg: Int): Boolean {
        for (web in tabCache.values) {
            web.mWebView.setOverScrollModeMethod(arg)
        }
        return true
    }

    override val overScrollModeMethod: Int
        get() = currentTab.mWebView.overScrollModeMethod

    override fun setOnCustomWebViewStateChangeListener(l: OnWebStateChangeListener?) {
        mStateChangeListener = l
    }

    override fun computeVerticalScrollRangeMethod(): Int =
            currentTab.mWebView.computeVerticalScrollRangeMethod()

    override fun computeVerticalScrollOffsetMethod(): Int =
            currentTab.mWebView.computeVerticalScrollOffsetMethod()

    override fun computeVerticalScrollExtentMethod(): Int =
            currentTab.mWebView.computeVerticalScrollExtentMethod()

    override fun computeHorizontalScrollRangeMethod(): Int =
            currentTab.mWebView.computeHorizontalScrollRangeMethod()

    override fun computeHorizontalScrollOffsetMethod(): Int =
            currentTab.mWebView.computeHorizontalScrollOffsetMethod()

    override fun computeHorizontalScrollExtentMethod(): Int =
            currentTab.mWebView.computeHorizontalScrollExtentMethod()

    override fun createPrintDocumentAdapter(documentName: String?): PrintDocumentAdapter? =
            currentTab.mWebView.createPrintDocumentAdapter(documentName)

    override fun loadDataWithBaseURL(baseUrl: String, data: String, mimeType: String, encoding: String, historyUrl: String) {
        currentTab.mWebView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override var identityId: Long
        get() = id
        set(value) {
            if (id > value) {
                id = value
            }
        }

    override fun resetTheme() {
        for (web in tabCache.values) {
            web.mWebView.resetTheme()
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        currentTab.mWebView.scrollTo(x, y)
    }

    override fun scrollBy(x: Int, y: Int) {
        currentTab.mWebView.scrollBy(x, y)
    }

    override fun saveWebArchiveMethod(filename: String): Boolean =
            currentTab.mWebView.saveWebArchiveMethod(filename)

    private fun move(fromdata: TabData?, todata: TabData) {
        val from = fromdata!!.mWebView
        val to = todata.mWebView

        from.onPause()
        to.onResume()
        from.setEmbeddedTitleBarMethod(null)
        to.setEmbeddedTitleBarMethod(mTitleBar)
        from.setOnMyCreateContextMenuListener(null)
        to.setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper)
        from.setGestureDetector(null)
        to.setGestureDetector(mGestureDetector)
        from.setDownloadListener(null)
        to.setDownloadListener(mDownloadListenerWrapper)
        from.setMyOnScrollChangedListener(null)
        to.setMyOnScrollChangedListener(mOnScrollChangedListener)
        from.setScrollBarListener(null)
        to.setScrollBarListener(mScrollBarListener)

        mStateChangeListener?.invoke(this, todata)
        to.requestWebFocus()
    }

    private fun settingWebView(from: CustomWebView, to: CustomWebView) {
        to.setMyWebChromeClient(mWebChromeClientWrapper)
        to.setMyWebViewClient(mWebViewClientWrapper)

        to.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY)
        to.setOverScrollModeMethod(from.overScrollModeMethod)

        to.resetTheme()
        to.swipeEnable = from.swipeEnable
        to.setVerticalScrollBarEnabled(verticalScrollBarEnabled)

        to.setLayerType(webLayerType, webLayerPaint)
        to.setAcceptThirdPartyCookies(CookieManager.getInstance(), acceptThirdPartyCookies)

        from.setScrollableHeight(null)
        to.setScrollableHeight(scrollableHeight)

        val fromSetting = from.settings
        val toSetting = to.settings

        toSetting.minimumFontSize = fromSetting.minimumFontSize
        toSetting.minimumLogicalFontSize = fromSetting.minimumLogicalFontSize

        toSetting.setNeedInitialFocus(false)
        toSetting.setSupportMultipleWindows(fromSetting.supportMultipleWindows())
        toSetting.defaultFontSize = fromSetting.defaultFontSize
        toSetting.defaultFixedFontSize = fromSetting.defaultFixedFontSize
        WebViewUtils.setTextSize(fromSetting, toSetting)
        toSetting.javaScriptEnabled = fromSetting.javaScriptEnabled
        toSetting.loadsImagesAutomatically = fromSetting.loadsImagesAutomatically
        toSetting.databaseEnabled = fromSetting.databaseEnabled
        toSetting.domStorageEnabled = fromSetting.domStorageEnabled

        toSetting.allowContentAccess = fromSetting.allowContentAccess
        toSetting.allowFileAccess = fromSetting.allowFileAccess
        toSetting.mixedContentMode = fromSetting.mixedContentMode
        toSetting.defaultTextEncodingName = fromSetting.defaultTextEncodingName
        toSetting.userAgentString = fromSetting.userAgentString
        toSetting.loadWithOverviewMode = fromSetting.loadWithOverviewMode
        toSetting.useWideViewPort = fromSetting.useWideViewPort
        WebViewUtils.setDisplayZoomButtons(fromSetting, toSetting)
        toSetting.cacheMode = fromSetting.cacheMode
        toSetting.javaScriptCanOpenWindowsAutomatically = fromSetting.javaScriptCanOpenWindowsAutomatically
        toSetting.layoutAlgorithm = fromSetting.layoutAlgorithm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            toSetting.safeBrowsingEnabled = fromSetting.safeBrowsingEnabled
        } else {
            @Suppress("DEPRECATION")
            toSetting.saveFormData = fromSetting.saveFormData
        }

        //Unknown get
        toSetting.setAppCacheEnabled(AppData.web_app_cache.get())
        toSetting.setAppCachePath(BrowserManager.getAppCacheFilePath(context))
        toSetting.setGeolocationEnabled(AppData.web_geolocation.get())
    }

    override val isBackForwardListEmpty: Boolean
        get() = isFirst || mCurrent == 0 && tabIndexList.size == 1 && tabIndexList[0].url == null

    override fun setMyOnScrollChangedListener(l: OnScrollChangedListener?) {
        mOnScrollChangedListener = l
        currentTab.mWebView.setMyOnScrollChangedListener(l)
    }

    override fun setScrollBarListener(l: OnScrollChangedListener?) {
        mScrollBarListener = l
        currentTab.mWebView.setScrollBarListener(l)
    }

    override fun setLayerType(layerType: Int, paint: Paint?) {
        this.webLayerType = layerType
        webLayerPaint = paint
        for (web in tabCache.values) {
            web.mWebView.setLayerType(layerType, paint)
        }
    }

    override fun onPreferenceReset() {
        tabCache.setSize(AppData.fast_back_cache_size.get())
    }

    override fun setAcceptThirdPartyCookies(manager: CookieManager, accept: Boolean) {
        acceptThirdPartyCookies = accept
        for (web in tabCache.values) {
            web.mWebView.setAcceptThirdPartyCookies(manager, accept)
        }
    }

    override fun setDoubleTapFling(fling: Boolean) {
        currentTab.mWebView.setDoubleTapFling(fling)
    }

    override val isTouching: Boolean
        get() = currentTab.mWebView.isTouching

    override val isScrollable: Boolean
        get() = currentTab.mWebView.isScrollable

    override var isToolbarShowing: Boolean
        get() = currentTab.mWebView.isToolbarShowing
        set(value) {
            currentTab.mWebView.isToolbarShowing = value
        }

    override var isNestedScrollingEnabledMethod: Boolean
        get() = currentTab.mWebView.isNestedScrollingEnabledMethod
        set(value) {
            currentTab.mWebView.isNestedScrollingEnabledMethod = value
        }

    override fun setVerticalScrollBarEnabled(enabled: Boolean) {
        verticalScrollBarEnabled = enabled
        for (web in tabCache.values) {
            web.mWebView.setVerticalScrollBarEnabled(enabled)
        }
    }

    override fun setSwipeable(swipeable: Boolean) {
        currentTab.mWebView.setSwipeable(swipeable)
    }

    override fun setScrollableHeight(listener: (() -> Int)?) {
        scrollableHeight = listener
        currentTab.mWebView.setScrollableHeight(listener)
    }

    override fun onCacheOverflow(tabData: TabData) {
        val bundle = Bundle()
        tabData.mWebView.saveState(bundle)
        tabSaveData.put(tabData.id, bundle)
        tabData.mWebView.setEmbeddedTitleBarMethod(null)
        tabData.mWebView.destroy()
    }

    companion object {

        private val sHeaderMap = TreeMap<String, String>()

        private val CAN_NOT_MOVE = 0
        private val CAN_EXTERNAL_MOVE = 1
        private val CAN_INTERNAL_MOVE = 2

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
