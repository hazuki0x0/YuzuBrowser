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

package jp.hazuki.yuzubrowser.webkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Build
import android.os.Message
import android.print.PrintDocumentAdapter
import android.view.ContextMenu
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import jp.hazuki.yuzubrowser.browser.BrowserManager
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.tab.manager.TabData
import jp.hazuki.yuzubrowser.toolbar.OnWebViewScrollChangeListener
import jp.hazuki.yuzubrowser.utils.WebViewUtils
import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector
import jp.hazuki.yuzubrowser.webkit.listener.OnScrollChangedListener
import jp.hazuki.yuzubrowser.webkit.listener.OnWebStateChangeListener
import java.util.*

abstract class AbstractCacheWebView(context: Context) : FrameLayout(context), CustomWebView {
    protected var id = System.currentTimeMillis()

    protected abstract val currentTab: TabData
    protected abstract val tabs: Collection<TabData>
    protected abstract val tabSize: Int

    protected var current = 0
    protected var isFirst = true
    private var titleBar: View? = null
    private var webLayerType: Int = 0
    private var webLayerPaint: Paint? = null
    private var acceptThirdPartyCookies: Boolean = false
    private var verticalScrollBarEnabled: Boolean = false
    private var stateChangeListener: OnWebStateChangeListener? = null
    private var onScrollChangedListener: OnScrollChangedListener? = null
    private var scrollBarListener: OnScrollChangedListener? = null
    private var downloadListener: DownloadListener? = null
    private var scrollableHeight: (() -> Int)? = null
    private var gestureDetector: MultiTouchGestureDetector? = null
    protected abstract val webChromeClientWrapper: CustomWebChromeClientWrapper
    protected abstract val webViewClientWrapper: CustomWebViewClientWrapper
    private val downloadListenerWrapper = DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
        synchronized(this@AbstractCacheWebView) {
            if (current >= 1) {
                val from = currentTab
                if (from.url == null || from.url == url) {
                    removeCurrentTab(from)
                    from.mWebView.destroy()
                }
            }
        }
        downloadListener?.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength)
    }
    override var scrollableChangeListener: OnScrollableChangeListener? = null
        set(value) {
            field = value
            tabs.forEach { it.mWebView.scrollableChangeListener = value }
        }

    protected abstract fun removeCurrentTab(tab: TabData)
    protected abstract fun resetCurrentTab(): TabData
    protected abstract fun newTab(url: String?, additionalHttpHeaders: MutableMap<String, String> = sHeaderMap)

    override fun clearCache(includeDiskFiles: Boolean) {
        for (web in tabs) {
            web.mWebView.clearCache(true)
        }
    }

    override fun canGoBack() = canGoBackType() != CAN_NOT_MOVE

    private fun canGoBackType(): Int {
        if (currentTab.mWebView.canGoBack()) return CAN_INTERNAL_MOVE
        return if (current >= 1) CAN_EXTERNAL_MOVE else CAN_NOT_MOVE
    }

    override fun canGoForward() = canGoForwardType() != CAN_NOT_MOVE

    private fun canGoForwardType(): Int {
        if (currentTab.mWebView.canGoForward()) return CAN_INTERNAL_MOVE
        return if (current + 1 < tabSize) CAN_EXTERNAL_MOVE else CAN_NOT_MOVE
    }

    @Synchronized
    override fun canGoBackOrForward(steps: Int): Boolean {
        if (steps == 0) return true
        return if (steps < 0) {
            current >= -steps
        } else {
            current + steps < tabSize
        }
    }

    fun moveTo(from: TabData, next: Boolean) {
        current += if (next) 1 else -1
        val to = resetCurrentTab()
        removeAllViews()
        addView(to.mWebView.view)
        move(from, to)
    }

    @Synchronized
    override fun goBack() {
        val from = currentTab
        when (canGoBackType()) {
            CAN_EXTERNAL_MOVE -> moveTo(from, false)
            CAN_INTERNAL_MOVE -> from.mWebView.goBack()
        }
    }

    @Synchronized
    override fun goForward() {
        val from = currentTab
        when (canGoForwardType()) {
            CAN_EXTERNAL_MOVE -> moveTo(from, true)
            CAN_INTERNAL_MOVE -> from.mWebView.goForward()
        }
    }

    @Synchronized
    override fun goBackOrForward(steps: Int) {
        if (!canGoBackOrForward(steps)) return

        removeAllViews()
        val from = currentTab
        current += steps
        val to = resetCurrentTab()
        addView(to.mWebView.view)
        move(from, to)
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

    override fun clearFormData() {
        for (web in tabs) {
            web.mWebView.clearFormData()
        }
    }

    override fun clearMatches() {
        for (web in tabs) {
            web.mWebView.clearMatches()
        }
    }

    override fun destroy() {
        titleBar = null
        for (web in tabs) {
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

    override val hitTestResult: WebView.HitTestResult?
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

    override fun evaluateJavascript(js: String?, callback: ValueCallback<String>?) {
        currentTab.mWebView.evaluateJavascript(js, callback)
    }

    override fun onPause() {
        for (web in tabs) {
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

    override fun resumeTimers() {
        currentTab.mWebView.resumeTimers()
    }

    override fun setDownloadListener(listener: DownloadListener?) {
        downloadListener = listener
        for (web in tabs) {
            web.mWebView.setDownloadListener(downloadListenerWrapper)
        }
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun setHttpAuthUsernamePassword(host: String, realm: String, username: String, password: String) {
        for (web in tabs) {
            web.mWebView.setHttpAuthUsernamePassword(host, realm, username, password)
        }
    }

    override fun setNetworkAvailable(networkUp: Boolean) {
        for (web in tabs) {
            web.mWebView.setNetworkAvailable(networkUp)
        }
    }

    override fun setMyWebChromeClient(client: CustomWebChromeClient?) {
        webChromeClientWrapper.setWebChromeClient(client)
        for (web in tabs) {
            web.mWebView.setMyWebChromeClient(webChromeClientWrapper)
        }
    }

    override fun setMyWebViewClient(client: CustomWebViewClient?) {
        webViewClientWrapper.setWebViewClient(client)
        for (web in tabs) {
            web.mWebView.setMyWebViewClient(webViewClientWrapper)
        }
    }

    override fun stopLoading() {
        currentTab.mWebView.stopLoading()
    }

    override fun zoomIn(): Boolean = currentTab.mWebView.zoomIn()

    override fun zoomOut(): Boolean = currentTab.mWebView.zoomOut()

    private var mCreateContextMenuListener: CustomOnCreateContextMenuListener? = null
    private val mCreateContextMenuListenerWrapper = object : CustomOnCreateContextMenuListener() {
        override fun onCreateContextMenu(menu: ContextMenu, webView: CustomWebView, menuInfo: ContextMenu.ContextMenuInfo?) {
            mCreateContextMenuListener?.onCreateContextMenu(menu, this@AbstractCacheWebView as CustomWebView, menuInfo)
        }
    }

    override fun setOnMyCreateContextMenuListener(webContextMenuListener: CustomOnCreateContextMenuListener?) {
        mCreateContextMenuListener = webContextMenuListener
        for (web in tabs) {
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
            for (web in tabs) {
                web.mWebView.swipeEnable = value
            }
        }

    override fun setGestureDetector(d: MultiTouchGestureDetector?) {
        gestureDetector = d
        for (web in tabs) {
            web.mWebView.setGestureDetector(d)
        }
    }

    @Synchronized
    override fun setEmbeddedTitleBarMethod(view: View?): Boolean {
        for (web in tabs) {
            web.mWebView.setEmbeddedTitleBarMethod(null)
        }
        titleBar = view
        return currentTab.mWebView.setEmbeddedTitleBarMethod(view)
    }

    override fun notifyFindDialogDismissedMethod(): Boolean {
        for (web in tabs) {
            web.mWebView.notifyFindDialogDismissedMethod()
        }
        return true
    }

    override fun setOverScrollModeMethod(arg: Int): Boolean {
        for (web in tabs) {
            web.mWebView.setOverScrollModeMethod(arg)
        }
        return true
    }

    override val overScrollModeMethod: Int
        get() = currentTab.mWebView.overScrollModeMethod

    override fun setOnCustomWebViewStateChangeListener(l: OnWebStateChangeListener?) {
        stateChangeListener = l
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
        for (web in tabs) {
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

    protected fun move(fromData: TabData, toData: TabData) {
        val from = fromData.mWebView
        val to = toData.mWebView

        from.onPause()
        to.onResume()
        from.setEmbeddedTitleBarMethod(null)
        to.setEmbeddedTitleBarMethod(titleBar)
        from.setOnMyCreateContextMenuListener(null)
        to.setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper)
        from.setGestureDetector(null)
        to.setGestureDetector(gestureDetector)
        from.setDownloadListener(null)
        to.setDownloadListener(downloadListenerWrapper)
        from.setMyOnScrollChangedListener(null)
        to.setMyOnScrollChangedListener(onScrollChangedListener)
        from.setScrollBarListener(null)
        to.setScrollBarListener(scrollBarListener)
        to.paddingScrollChangedListener = from.paddingScrollChangedListener
        from.paddingScrollChangedListener = null

        stateChangeListener?.invoke(this, toData)
        to.requestWebFocus()
        if (from.isScrollable != to.isScrollable) {
            scrollableChangeListener?.onScrollableChanged(to.isScrollable)
        }
    }

    protected fun settingWebView(from: CustomWebView, to: CustomWebView) {
        to.setMyWebChromeClient(webChromeClientWrapper)
        to.setMyWebViewClient(webViewClientWrapper)

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

    override fun setMyOnScrollChangedListener(l: OnScrollChangedListener?) {
        onScrollChangedListener = l
        currentTab.mWebView.setMyOnScrollChangedListener(l)
    }

    override fun setScrollBarListener(l: OnScrollChangedListener?) {
        scrollBarListener = l
        currentTab.mWebView.setScrollBarListener(l)
    }

    override fun setLayerType(layerType: Int, paint: Paint?) {
        this.webLayerType = layerType
        webLayerPaint = paint
        for (web in tabs) {
            web.mWebView.setLayerType(layerType, paint)
        }
    }

    override fun setAcceptThirdPartyCookies(manager: CookieManager, accept: Boolean) {
        acceptThirdPartyCookies = accept
        for (web in tabs) {
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

    override var paddingScrollChangedListener: OnWebViewScrollChangeListener?
        get() = currentTab.mWebView.paddingScrollChangedListener
        set(value) {
            currentTab.mWebView.paddingScrollChangedListener = value
        }

    override fun setVerticalScrollBarEnabled(enabled: Boolean) {
        verticalScrollBarEnabled = enabled
        for (web in tabs) {
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

    override fun onPreferenceReset() {}

    companion object {
        internal val sHeaderMap = TreeMap<String, String>()

        internal const val CAN_NOT_MOVE = 0
        internal const val CAN_EXTERNAL_MOVE = 1
        internal const val CAN_INTERNAL_MOVE = 2
    }
}