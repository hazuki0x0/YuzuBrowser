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
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import jp.hazuki.yuzubrowser.browser.BrowserManager
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.tab.manager.TabData
import jp.hazuki.yuzubrowser.utils.WebViewUtils
import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector
import jp.hazuki.yuzubrowser.webkit.listener.OnScrollChangedListener
import jp.hazuki.yuzubrowser.webkit.listener.OnWebStateChangeListener
import java.util.*

class CacheWebView(context: Context) : FrameLayout(context), CustomWebView {
    private val mList = ArrayList<TabData>()
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
    private val mDownloadListenerWrapper = DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
        synchronized(this@CacheWebView) {
            if (mCurrent >= 1) {
                val from = mList[mCurrent]
                if (from.url == null || from.url == url) {
                    mList.removeAt(mCurrent)

                    val to = mList[--mCurrent]
                    removeAllViews()
                    addView(to.mWebView.view)
                    move(from, to)

                    from.mWebView.destroy()
                }
            }
        }
        mDownloadListener?.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength)
    }
    private val mWebChromeClientWrapper = object : CustomWebChromeClientWrapper(this) {
        override fun onProgressChanged(web: CustomWebView, newProgress: Int) {
            val data = webView2Data(web)
            data?.onProgressChanged(newProgress)
            if (web != mList[mCurrent].mWebView) return
            super.onProgressChanged(web, newProgress)
        }

        override fun onReceivedTitle(web: CustomWebView, title: String) {
            val data = webView2Data(web)
            data?.onReceivedTitle(title)
            if (web != mList[mCurrent].mWebView) return
            super.onReceivedTitle(web, title)
        }

        override fun onReceivedIcon(web: CustomWebView, icon: Bitmap) {
            if (web != mList[mCurrent].mWebView) return
            super.onReceivedIcon(web, icon)
        }
    }

    private val mWebViewClientWrapper = object : CustomWebViewClientWrapper(this) {
        override fun onScaleChanged(view: CustomWebView, oldScale: Float, newScale: Float) {
            if (view != mList[mCurrent].mWebView) return
            super.onScaleChanged(view, oldScale, newScale)
        }

        override fun onUnhandledKeyEvent(view: CustomWebView, event: KeyEvent) {
            if (view != mList[mCurrent].mWebView) return
            super.onUnhandledKeyEvent(view, event)
        }

        override fun onPageFinished(web: CustomWebView, url: String) {
            val data = webView2Data(web)
            data?.onPageFinished(web, url)
            if (web != mList[mCurrent].mWebView) return
            super.onPageFinished(web, url)
        }

        override fun onPageStarted(web: CustomWebView, url: String, favicon: Bitmap?) {
            val data = webView2Data(web)
            data?.onPageStarted(url, favicon)
            if (web != mList[mCurrent].mWebView) return
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
            mCreateContextMenuListener?.onCreateContextMenu(menu, this@CacheWebView as CustomWebView, menuInfo)
        }
    }
    private var mGestureDetector: MultiTouchGestureDetector? = null

    init {
        val web = SwipeWebView(context)
        mList.add(TabData(web))
        addView(web)
    }

    private fun webView2Data(web: CustomWebView): TabData? = mList.firstOrNull { it.mWebView == web }

    private fun newTab(url: String?, additionalHttpHeaders: MutableMap<String, String> = sHeaderMap) {
        val from = mList[mCurrent]
        val to = TabData(SwipeWebView(context))
        for (i in mList.size - 1 downTo mCurrent + 1) {
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
        ++mCurrent
        move(from, to)
    }

    private fun canGoBackType(): Int {
        if (mList[mCurrent].mWebView.canGoBack()) return CAN_INTERNAL_MOVE
        return if (mCurrent >= 1) CAN_EXTERNAL_MOVE else CAN_NOT_MOVE
    }

    override fun canGoBack() = canGoBackType() != CAN_NOT_MOVE

    @Synchronized override fun canGoBackOrForward(steps: Int): Boolean {
        if (steps == 0) return true
        return if (steps < 0) {
            mCurrent >= -steps
        } else {
            mCurrent + steps < mList.size
        }
    }

    private fun canGoForwardType(): Int {
        if (mList[mCurrent].mWebView.canGoForward()) return CAN_INTERNAL_MOVE
        return if (mCurrent + 1 < mList.size) CAN_EXTERNAL_MOVE else CAN_NOT_MOVE
    }

    override fun canGoForward() = canGoForwardType() != CAN_NOT_MOVE

    override fun clearCache(includeDiskFiles: Boolean) {
        for (web in mList) {
            web.mWebView.clearCache(true)
        }
    }

    override fun clearFormData() {
        for (web in mList) {
            web.mWebView.clearFormData()
        }
    }

    @Synchronized override fun clearHistory() {
        val data = mList[mCurrent]
        data.mWebView.clearHistory()
        mList.clear()
        mList.add(data)
        mCurrent = 0
    }

    override fun clearMatches() {
        for (web in mList) {
            web.mWebView.clearMatches()
        }
    }

    override fun copyMyBackForwardList(): CustomWebBackForwardList {
        val list = CustomWebBackForwardList(mCurrent, mList.size)
        mList.asSequence()
                .map { it.mWebView }
                .mapTo(list) { CustomWebHistoryItem(it.url!!, it.originalUrl, it.title, it.favicon) }
        return list
    }

    override fun destroy() {
        mTitleBar = null
        for (web in mList) {
            web.mWebView.destroy()
        }
    }

    override fun findAllAsync(find: String) {
        mList[mCurrent].mWebView.findAllAsync(find)
    }

    override fun setFindListener(listener: WebView.FindListener) {
        mList[mCurrent].mWebView.setFindListener(listener)
    }

    override fun findNext(forward: Boolean) {
        mList[mCurrent].mWebView.findNext(forward)
    }

    override fun flingScroll(vx: Int, vy: Int) {
        mList[mCurrent].mWebView.flingScroll(vx, vy)
    }

    override val favicon
        get() = mList[mCurrent].mWebView.favicon

    override val hitTestResult
        get() = mList[mCurrent].mWebView.hitTestResult

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun getHttpAuthUsernamePassword(host: String, realm: String): Array<String>? =
            mList[mCurrent].mWebView.getHttpAuthUsernamePassword(host, realm)

    override val originalUrl: String?
        get() = mList[mCurrent].mWebView.originalUrl

    override val progress: Int
        get() = mList[mCurrent].mWebView.progress

    override val settings: WebSettings
        get() = mList[mCurrent].mWebView.settings

    override val title: String?
        get() = mList[mCurrent].mWebView.title

    override val url: String?
        get() = mList[mCurrent].mWebView.url

    @Synchronized override fun goBack() {
        val from = mList[mCurrent]
        when (canGoBackType()) {
            CAN_EXTERNAL_MOVE -> {
                val to = mList[--mCurrent]
                removeAllViews()
                addView(to.mWebView.view)
                move(from, to)
            }
            CAN_INTERNAL_MOVE -> from.mWebView.goBack()
            else -> {
            }
        }
    }

    @Synchronized override fun goBackOrForward(steps: Int) {
        if (!canGoBackOrForward(steps)) return

        removeAllViews()
        val from = mList[mCurrent]
        mCurrent += steps
        val to = mList[mCurrent]
        addView(to.mWebView.view)
        move(from, to)
    }

    @Synchronized override fun goForward() {
        val from = mList[mCurrent]
        when (canGoForwardType()) {
            CAN_EXTERNAL_MOVE -> {
                val to = mList[++mCurrent]
                removeAllViews()
                addView(to.mWebView.view)
                move(from, to)
            }
            CAN_INTERNAL_MOVE -> from.mWebView.goForward()
            else -> {
            }
        }
    }

    override fun loadUrl(url: String?) {
        when {
            isFirst -> {
                isFirst = false
                mList[0].mWebView.loadUrl(url)
            }
            WebViewUtils.shouldLoadSameTabUser(url) -> mList[mCurrent].mWebView.loadUrl(url)
            url != null -> newTab(url)
        }
    }

    override fun loadUrl(url: String?, additionalHttpHeaders: MutableMap<String, String>?) {
        when {
            isFirst -> {
                isFirst = false
                mList[0].mWebView.loadUrl(url, additionalHttpHeaders)
            }
            WebViewUtils.shouldLoadSameTabUser(url) -> mList[mCurrent].mWebView.loadUrl(url, additionalHttpHeaders)
            url != null && additionalHttpHeaders != null -> newTab(url, additionalHttpHeaders)
            url != null -> newTab(url)
        }
    }

    override fun evaluateJavascript(js: String?, callback: ValueCallback<String>?) {
        mList[mCurrent].mWebView.evaluateJavascript(js, callback)
    }

    override fun onPause() {
        for (web in mList) {
            web.mWebView.onPause()
        }
    }

    override fun onResume() {
        /*for(NormalWebView web:mList){
            web.onResume();
		}*/
        mList[mCurrent].mWebView.onResume()
    }

    override fun pageDown(bottom: Boolean): Boolean = mList[mCurrent].mWebView.pageDown(bottom)

    override fun pageUp(top: Boolean): Boolean = mList[mCurrent].mWebView.pageUp(top)

    override fun pauseTimers() {
        mList[mCurrent].mWebView.pauseTimers()
    }

    override fun reload() {
        mList[mCurrent].mWebView.reload()
    }

    override fun requestWebFocus(): Boolean = mList[mCurrent].mWebView.requestWebFocus()

    override fun requestFocusNodeHref(hrefMsg: Message) {
        mList[mCurrent].mWebView.requestFocusNodeHref(hrefMsg)
    }

    override fun requestImageRef(msg: Message) {
        mList[mCurrent].mWebView.requestImageRef(msg)
    }

    @Synchronized override fun restoreState(inState: Bundle): WebBackForwardList? {
        isFirst = false

        val from = mList[mCurrent]
        mList.clear()
        removeAllViews()

        val all = inState.getInt("CacheWebView.WEB_ALL_COUNT")
        mCurrent = inState.getInt("CacheWebView.WEB_CURRENT_COUNT")

        for (i in 0 until all) {
            val web = TabData(SwipeWebView(context))
            web.mWebView.onPause()
            mList.add(web)
            if (i == mCurrent)
                addView(web.mWebView.view)
            web.mWebView.restoreState(inState.getBundle("CacheWebView.WEB_NO" + i))
            settingWebView(from.mWebView, web.mWebView)
        }
        move(from, mList[mCurrent])
        return null
    }

    override fun resumeTimers() {
        mList[mCurrent].mWebView.resumeTimers()
    }

    @Synchronized override fun saveState(outState: Bundle): WebBackForwardList? {
        outState.putBoolean("CacheWebView.IsCacheWebView", true)
        outState.putInt("CacheWebView.WEB_ALL_COUNT", mList.size)
        outState.putInt("CacheWebView.WEB_CURRENT_COUNT", mCurrent)
        for ((i, web) in mList.withIndex()) {
            val state = Bundle()
            web.mWebView.saveState(state)
            outState.putBundle("CacheWebView.WEB_NO" + i, state)
        }
        return null
    }

    override fun setDownloadListener(listener: DownloadListener?) {
        mDownloadListener = listener
        for (web in mList) {
            web.mWebView.setDownloadListener(mDownloadListenerWrapper)
        }
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun setHttpAuthUsernamePassword(host: String, realm: String, username: String, password: String) {
        for (web in mList) {
            web.mWebView.setHttpAuthUsernamePassword(host, realm, username, password)
        }
    }

    override fun setNetworkAvailable(networkUp: Boolean) {
        for (web in mList) {
            web.mWebView.setNetworkAvailable(networkUp)
        }
    }

    override fun setMyWebChromeClient(client: CustomWebChromeClient?) {
        mWebChromeClientWrapper.setWebChromeClient(client)
        /*mList.get(mCurrent).setMyWebChromeClient(mWebChromeClientWrapper);*/
        for (web in mList) {
            web.mWebView.setMyWebChromeClient(mWebChromeClientWrapper)
        }
    }

    override fun setMyWebViewClient(client: CustomWebViewClient?) {
        mWebViewClientWrapper.setWebViewClient(client)
        /*mList.get(mCurrent).setMyWebViewClient(mWebViewClientWrapper);*/
        for (web in mList) {
            web.mWebView.setMyWebViewClient(mWebViewClientWrapper)
        }
    }

    override fun stopLoading() {
        mList[mCurrent].mWebView.stopLoading()
    }

    override fun zoomIn(): Boolean = mList[mCurrent].mWebView.zoomIn()

    override fun zoomOut(): Boolean = mList[mCurrent].mWebView.zoomOut()

    override fun setOnMyCreateContextMenuListener(webContextMenuListener: CustomOnCreateContextMenuListener?) {
        mCreateContextMenuListener = webContextMenuListener
        /*mList.get(mCurrent).setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper);*/
        for (web in mList) {
            web.mWebView.setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper)
        }
    }

    override val webScrollX: Int
        get() = mList[mCurrent].mWebView.webScrollX

    override val webScrollY: Int
        get() = mList[mCurrent].mWebView.webScrollY

    override val view: View
        get() = this

    override val webView: WebView
        get() = mList[mCurrent].mWebView.webView

    override var swipeEnable: Boolean
        get() = mList[mCurrent].mWebView.swipeEnable
        set(value) {
            for (web in mList) {
                web.mWebView.swipeEnable = value
            }
        }

    override fun setGestureDetector(d: MultiTouchGestureDetector?) {
        mGestureDetector = d
        for (web in mList) {
            web.mWebView.setGestureDetector(d)
        }
    }

    @Synchronized override fun setEmbeddedTitleBarMethod(view: View?): Boolean {
        for (web in mList) {
            web.mWebView.setEmbeddedTitleBarMethod(null)
        }
        mTitleBar = view
        return mList[mCurrent].mWebView.setEmbeddedTitleBarMethod(view)
    }

    override fun notifyFindDialogDismissedMethod(): Boolean {
        for (web in mList) {
            web.mWebView.notifyFindDialogDismissedMethod()
        }
        return true
    }

    override fun setOverScrollModeMethod(arg: Int): Boolean {
        for (web in mList) {
            web.mWebView.setOverScrollModeMethod(arg)
        }
        return true
    }

    override val overScrollModeMethod: Int
        get() = mList[mCurrent].mWebView.overScrollModeMethod

    override fun setOnCustomWebViewStateChangeListener(l: OnWebStateChangeListener?) {
        mStateChangeListener = l
    }

    override fun computeVerticalScrollRangeMethod(): Int =
            mList[mCurrent].mWebView.computeVerticalScrollRangeMethod()

    override fun computeVerticalScrollOffsetMethod(): Int =
            mList[mCurrent].mWebView.computeVerticalScrollOffsetMethod()

    override fun computeVerticalScrollExtentMethod(): Int =
            mList[mCurrent].mWebView.computeVerticalScrollExtentMethod()

    override fun computeHorizontalScrollRangeMethod(): Int =
            mList[mCurrent].mWebView.computeHorizontalScrollRangeMethod()

    override fun computeHorizontalScrollOffsetMethod(): Int =
            mList[mCurrent].mWebView.computeHorizontalScrollOffsetMethod()

    override fun computeHorizontalScrollExtentMethod(): Int =
            mList[mCurrent].mWebView.computeHorizontalScrollExtentMethod()

    override fun createPrintDocumentAdapter(documentName: String?): PrintDocumentAdapter? =
            mList[mCurrent].mWebView.createPrintDocumentAdapter(documentName)

    override fun loadDataWithBaseURL(baseUrl: String, data: String, mimeType: String, encoding: String, historyUrl: String) {
        mList[mCurrent].mWebView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override var identityId: Long
        get() = id
        set(value) {
            if (id > value) {
                id = value
            }
        }

    override fun resetTheme() {
        for (web in mList) {
            web.mWebView.resetTheme()
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        mList[mCurrent].mWebView.scrollTo(x, y)
    }

    override fun scrollBy(x: Int, y: Int) {
        mList[mCurrent].mWebView.scrollBy(x, y)
    }

    override fun saveWebArchiveMethod(filename: String): Boolean =
            mList[mCurrent].mWebView.saveWebArchiveMethod(filename)

    private fun move(fromdata: TabData, todata: TabData) {
        val from = fromdata.mWebView
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
        get() = isFirst || mCurrent == 0 && mList.size == 1 && mList[0].mWebView.url == null

    override fun setMyOnScrollChangedListener(l: OnScrollChangedListener?) {
        mOnScrollChangedListener = l
        mList[mCurrent].mWebView.setMyOnScrollChangedListener(l)
    }

    override fun setScrollBarListener(l: OnScrollChangedListener?) {
        mScrollBarListener = l
        mList[mCurrent].mWebView.setScrollBarListener(l)
    }

    override fun setLayerType(layerType: Int, paint: Paint?) {
        this.webLayerType = layerType
        webLayerPaint = paint
        for (web in mList) {
            web.mWebView.setLayerType(layerType, paint)
        }
    }

    override fun onPreferenceReset() {}

    override fun setAcceptThirdPartyCookies(manager: CookieManager, accept: Boolean) {
        acceptThirdPartyCookies = accept
        for (web in mList) {
            web.mWebView.setAcceptThirdPartyCookies(manager, accept)
        }
    }

    override fun setDoubleTapFling(fling: Boolean) {
        mList[mCurrent].mWebView.setDoubleTapFling(fling)
    }

    override val isTouching: Boolean
        get() = mList[mCurrent].mWebView.isTouching

    override val isScrollable: Boolean
        get() = mList[mCurrent].mWebView.isScrollable

    override var isToolbarShowing: Boolean
        get() = mList[mCurrent].mWebView.isToolbarShowing
        set(value) {
            mList[mCurrent].mWebView.isToolbarShowing = value
        }

    override var isNestedScrollingEnabledMethod: Boolean
        get() = mList[mCurrent].mWebView.isNestedScrollingEnabledMethod
        set(value) {
            mList[mCurrent].mWebView.isNestedScrollingEnabledMethod = value
        }

    override fun setVerticalScrollBarEnabled(enabled: Boolean) {
        verticalScrollBarEnabled = enabled
        for (web in mList) {
            web.mWebView.setVerticalScrollBarEnabled(enabled)
        }
    }

    override fun setSwipeable(swipeable: Boolean) {
        mList[mCurrent].mWebView.setSwipeable(swipeable)
    }

    companion object {

        private val sHeaderMap = TreeMap<String, String>()

        private const val CAN_NOT_MOVE = 0
        private const val CAN_EXTERNAL_MOVE = 1
        private const val CAN_INTERNAL_MOVE = 2

        @JvmStatic
        fun isBundleCacheWebView(state: Bundle): Boolean =
                state.getBoolean("CacheWebView.IsCacheWebView", false)
    }
}
