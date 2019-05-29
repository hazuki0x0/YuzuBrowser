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
import android.graphics.Paint
import android.os.Build
import android.os.Message
import android.print.PrintDocumentAdapter
import android.view.ContextMenu
import android.view.View
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.collection.ArrayMap
import androidx.collection.SimpleArrayMap
import jp.hazuki.yuzubrowser.core.utility.common.listener.OnTouchEventListener
import jp.hazuki.yuzubrowser.core.utility.extensions.appCacheFilePath
import jp.hazuki.yuzubrowser.webview.listener.OnScrollChangedListener
import jp.hazuki.yuzubrowser.webview.listener.OnScrollableChangeListener
import jp.hazuki.yuzubrowser.webview.page.WebViewPage
import jp.hazuki.yuzubrowser.webview.utility.WebViewUtility

internal abstract class AbstractCacheWebView(context: Context) : FrameLayout(context), CustomWebView, WebViewUtility {
    protected var id = System.currentTimeMillis()

    internal abstract val currentPage: WebViewPage
    internal abstract val tabs: Collection<WebViewPage>
    protected abstract val tabSize: Int

    protected var current = 0
    protected var isFirst = true
    private var titleBar: View? = null
    private var webLayerType: Int = 0
    private var webLayerPaint: Paint? = null
    override var renderingMode = 0
    private var acceptThirdPartyCookies: Boolean = false
    private var verticalScrollBarEnabled: Boolean = false
    private var onScrollChangedListener: OnScrollChangedListener? = null
    private var downloadListener: DownloadListener? = null
    private var scrollableHeight: (() -> Int)? = null
    private var touchDetector: OnTouchEventListener? = null
    protected abstract val webChromeClientWrapper: CustomWebChromeClientWrapper
    protected abstract val webViewClientWrapper: CustomWebViewClientWrapper
    private val downloadListenerWrapper = DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
        synchronized(this@AbstractCacheWebView) {
            if (current >= 1) {
                val from = currentPage
                if (from.url == null || from.url == url) {
                    removeCurrentTab(from)
                    from.webView.destroy()
                }
            }
        }
        downloadListener?.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength)
    }
    override var scrollableChangeListener: OnScrollableChangeListener? = null
        set(value) {
            field = value
            tabs.forEach { it.webView.scrollableChangeListener = value }
        }

    internal abstract fun removeCurrentTab(tab: WebViewPage)
    internal abstract fun resetCurrentTab(): WebViewPage
    protected abstract fun newTab(url: String, additionalHttpHeaders: Map<String, String> = emptyMap)

    override fun clearCache(includeDiskFiles: Boolean) {
        for (web in tabs) {
            web.webView.clearCache(true)
        }
    }

    override fun canGoBack() = canGoBackType() != CAN_NOT_MOVE

    private fun canGoBackType(): Int {
        if (currentPage.webView.canGoBack()) return CAN_INTERNAL_MOVE
        return if (current >= 1) CAN_EXTERNAL_MOVE else CAN_NOT_MOVE
    }

    override fun canGoForward() = canGoForwardType() != CAN_NOT_MOVE

    private fun canGoForwardType(): Int {
        if (currentPage.webView.canGoForward()) return CAN_INTERNAL_MOVE
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

    internal fun moveTo(from: WebViewPage, next: Boolean) {
        current += if (next) 1 else -1
        val to = resetCurrentTab()
        removeAllViews()
        addView(to.webView.view)
        move(from, to)
    }

    @Synchronized
    override fun goBack() {
        val from = currentPage
        when (canGoBackType()) {
            CAN_EXTERNAL_MOVE -> moveTo(from, false)
            CAN_INTERNAL_MOVE -> from.webView.goBack()
        }
    }

    @Synchronized
    override fun goForward() {
        val from = currentPage
        when (canGoForwardType()) {
            CAN_EXTERNAL_MOVE -> moveTo(from, true)
            CAN_INTERNAL_MOVE -> from.webView.goForward()
        }
    }

    @Synchronized
    override fun goBackOrForward(steps: Int) {
        if (!canGoBackOrForward(steps)) return

        removeAllViews()
        val from = currentPage
        current += steps
        val to = resetCurrentTab()
        addView(to.webView.view)
        move(from, to)
    }

    override fun loadUrl(url: String?) {
        when {
            isFirst -> {
                isFirst = false
                currentPage.webView.loadUrl(url)
            }
            url != null && url.shouldLoadSameTabUser() -> currentPage.webView.loadUrl(url)
            url != null -> newTab(url)
        }
    }

    override fun loadUrl(url: String?, additionalHttpHeaders: MutableMap<String, String>?) {
        when {
            isFirst -> {
                isFirst = false
                currentPage.webView.loadUrl(url, additionalHttpHeaders)
            }
            url != null && url.shouldLoadSameTabUser() -> currentPage.webView.loadUrl(url, additionalHttpHeaders)
            url != null && additionalHttpHeaders != null -> newTab(url, additionalHttpHeaders)
            url != null -> newTab(url, additionalHttpHeaders ?: emptyMap)
        }
    }

    override fun clearFormData() {
        for (web in tabs) {
            web.webView.clearFormData()
        }
    }

    override fun clearMatches() {
        for (web in tabs) {
            web.webView.clearMatches()
        }
    }

    override fun destroy() {
        titleBar = null
        for (web in tabs) {
            web.webView.destroy()
        }
    }

    override fun findAllAsync(find: String) {
        currentPage.webView.findAllAsync(find)
    }

    override fun setFindListener(listener: WebView.FindListener) {
        currentPage.webView.setFindListener(listener)
    }

    override fun findNext(forward: Boolean) {
        currentPage.webView.findNext(forward)
    }

    override fun flingScroll(vx: Int, vy: Int) {
        currentPage.webView.flingScroll(vx, vy)
    }

    override val favicon: Bitmap?
        get() = currentPage.webView.favicon

    override val hitTestResult: WebView.HitTestResult?
        get() = currentPage.webView.hitTestResult

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun getHttpAuthUsernamePassword(host: String, realm: String): Array<String>? =
            currentPage.webView.getHttpAuthUsernamePassword(host, realm)

    override val originalUrl: String?
        get() = currentPage.webView.originalUrl

    override val progress: Int
        get() = currentPage.webView.progress

    override val webSettings: YuzuWebSettings
        get() = currentPage.webView.webSettings

    override val title: String?
        get() = currentPage.webView.title

    override val url: String?
        get() = currentPage.webView.url

    override fun evaluateJavascript(js: String?, callback: ValueCallback<String>?) {
        currentPage.webView.evaluateJavascript(js, callback)
    }

    override fun onPause() {
        for (web in tabs) {
            web.webView.onPause()
        }
    }

    override fun onResume() {
        /*for(NormalWebView web:mList){
            web.onResume();
		}*/
        currentPage.webView.onResume()
    }

    override fun pageDown(bottom: Boolean): Boolean = currentPage.webView.pageDown(bottom)

    override fun pageUp(top: Boolean): Boolean = currentPage.webView.pageUp(top)

    override fun pauseTimers() {
        currentPage.webView.pauseTimers()
    }

    override fun reload() {
        currentPage.webView.reload()
    }

    override fun requestWebFocus(): Boolean = currentPage.webView.requestWebFocus()

    override fun requestFocusNodeHref(hrefMsg: Message) {
        currentPage.webView.requestFocusNodeHref(hrefMsg)
    }

    override fun requestImageRef(msg: Message) {
        currentPage.webView.requestImageRef(msg)
    }

    override fun resumeTimers() {
        currentPage.webView.resumeTimers()
    }

    override fun setDownloadListener(listener: DownloadListener?) {
        downloadListener = listener
        for (web in tabs) {
            web.webView.setDownloadListener(downloadListenerWrapper)
        }
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun setHttpAuthUsernamePassword(host: String, realm: String, username: String, password: String) {
        for (web in tabs) {
            web.webView.setHttpAuthUsernamePassword(host, realm, username, password)
        }
    }

    override fun setNetworkAvailable(networkUp: Boolean) {
        for (web in tabs) {
            web.webView.setNetworkAvailable(networkUp)
        }
    }

    override fun setMyWebChromeClient(client: CustomWebChromeClient?) {
        webChromeClientWrapper.setWebChromeClient(client)
        for (web in tabs) {
            web.webView.setMyWebChromeClient(webChromeClientWrapper)
        }
    }

    override fun setMyWebViewClient(client: CustomWebViewClient?) {
        webViewClientWrapper.setWebViewClient(client)
        for (web in tabs) {
            web.webView.setMyWebViewClient(webViewClientWrapper)
        }
    }

    override fun stopLoading() {
        currentPage.webView.stopLoading()
    }

    override fun zoomIn(): Boolean = currentPage.webView.zoomIn()

    override fun zoomOut(): Boolean = currentPage.webView.zoomOut()

    private var mCreateContextMenuListener: CustomOnCreateContextMenuListener? = null
    private val mCreateContextMenuListenerWrapper = object : CustomOnCreateContextMenuListener() {
        override fun onCreateContextMenu(menu: ContextMenu, webView: CustomWebView, menuInfo: ContextMenu.ContextMenuInfo?) {
            mCreateContextMenuListener?.onCreateContextMenu(menu, this@AbstractCacheWebView as CustomWebView, menuInfo)
        }
    }

    override fun setOnMyCreateContextMenuListener(webContextMenuListener: CustomOnCreateContextMenuListener?) {
        mCreateContextMenuListener = webContextMenuListener
        for (web in tabs) {
            web.webView.setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper)
        }
    }

    override val webScrollX: Int
        get() = currentPage.webView.webScrollX

    override val webScrollY: Int
        get() = currentPage.webView.webScrollY

    override val view: View
        get() = this

    override val theme
        get() = currentPage.webView.theme

    override val webView: WebView
        get() = currentPage.webView.webView

    override var swipeEnable: Boolean
        get() = currentPage.webView.swipeEnable
        set(value) {
            for (web in tabs) {
                web.webView.swipeEnable = value
            }
        }

    override fun setWebViewTouchDetector(d: OnTouchEventListener?) {
        touchDetector = d
        for (web in tabs) {
            web.webView.setWebViewTouchDetector(d)
        }
    }

    @Synchronized
    override fun setEmbeddedTitleBarMethod(view: View?): Boolean {
        for (web in tabs) {
            web.webView.setEmbeddedTitleBarMethod(null)
        }
        titleBar = view
        return currentPage.webView.setEmbeddedTitleBarMethod(view)
    }

    override fun notifyFindDialogDismissedMethod(): Boolean {
        for (web in tabs) {
            web.webView.notifyFindDialogDismissedMethod()
        }
        return true
    }

    override fun setOverScrollModeMethod(arg: Int): Boolean {
        for (web in tabs) {
            web.webView.setOverScrollModeMethod(arg)
        }
        return true
    }

    override val overScrollModeMethod: Int
        get() = currentPage.webView.overScrollModeMethod

    override fun computeVerticalScrollRangeMethod(): Int =
            currentPage.webView.computeVerticalScrollRangeMethod()

    override fun computeVerticalScrollOffsetMethod(): Int =
            currentPage.webView.computeVerticalScrollOffsetMethod()

    override fun computeVerticalScrollExtentMethod(): Int =
            currentPage.webView.computeVerticalScrollExtentMethod()

    override fun computeHorizontalScrollRangeMethod(): Int =
            currentPage.webView.computeHorizontalScrollRangeMethod()

    override fun computeHorizontalScrollOffsetMethod(): Int =
            currentPage.webView.computeHorizontalScrollOffsetMethod()

    override fun computeHorizontalScrollExtentMethod(): Int =
            currentPage.webView.computeHorizontalScrollExtentMethod()

    override fun createPrintDocumentAdapter(documentName: String?): PrintDocumentAdapter? =
            currentPage.webView.createPrintDocumentAdapter(documentName)

    override fun loadDataWithBaseURL(baseUrl: String, data: String, mimeType: String, encoding: String, historyUrl: String) {
        currentPage.webView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override var identityId: Long
        get() = id
        set(value) {
            if (id > value) {
                id = value
            }
        }

    override fun setWebViewTheme(theme: CustomWebView.WebViewTheme?) {
        tabs.forEach { it.webView.setWebViewTheme(theme) }
    }

    override fun scrollTo(x: Int, y: Int) {
        currentPage.webView.scrollTo(x, y)
    }

    override fun scrollBy(x: Int, y: Int) {
        currentPage.webView.scrollBy(x, y)
    }

    override fun saveWebArchiveMethod(filename: String): Boolean =
            currentPage.webView.saveWebArchiveMethod(filename)

    internal fun move(fromData: WebViewPage, toData: WebViewPage) {
        val from = fromData.webView
        val to = toData.webView

        from.onPause()
        to.onResume()
        from.setEmbeddedTitleBarMethod(null)
        to.setEmbeddedTitleBarMethod(titleBar)
        from.setOnMyCreateContextMenuListener(null)
        to.setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper)
        from.setWebViewTouchDetector(null)
        to.setWebViewTouchDetector(touchDetector)
        from.setDownloadListener(null)
        to.setDownloadListener(downloadListenerWrapper)
        from.setMyOnScrollChangedListener(null)
        to.setMyOnScrollChangedListener(onScrollChangedListener)

        val progress = to.progress
        webViewClientWrapper.onPageChanged(this, toData.url ?: "", toData.originalUrl
                ?: "", progress, progress != 100)
        to.requestWebFocus()
        if (from.isScrollable != to.isScrollable) {
            scrollableChangeListener?.onScrollableChanged(to.isScrollable)
        }
    }

    protected fun CustomWebView.copySettingsTo(to: CustomWebView) {
        to.setMyWebChromeClient(webChromeClientWrapper)
        to.setMyWebViewClient(webViewClientWrapper)

        to.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY)
        to.setOverScrollModeMethod(overScrollModeMethod)

        to.setWebViewTheme(theme)
        to.swipeEnable = swipeEnable
        to.setVerticalScrollBarEnabled(verticalScrollBarEnabled)

        to.setLayerType(webLayerType, webLayerPaint)
        to.setAcceptThirdPartyCookies(CookieManager.getInstance(), acceptThirdPartyCookies)

        to.setScrollableHeight(scrollableHeight)

        val fromSetting = webSettings
        val toSetting = to.webSettings

        toSetting.minimumFontSize = fromSetting.minimumFontSize
        toSetting.minimumLogicalFontSize = fromSetting.minimumLogicalFontSize

        toSetting.setNeedInitialFocus(true)
        toSetting.setSupportMultipleWindows(fromSetting.supportMultipleWindows())
        toSetting.defaultFontSize = fromSetting.defaultFontSize
        toSetting.defaultFixedFontSize = fromSetting.defaultFixedFontSize
        fromSetting.textZoom = toSetting.textZoom
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
        toSetting.displayZoomButtons = fromSetting.displayZoomButtons
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
        toSetting.appCacheEnabled = fromSetting.appCacheEnabled
        toSetting.setAppCachePath(context.appCacheFilePath)
        toSetting.geolocationEnabled = fromSetting.geolocationEnabled
    }

    override fun setMyOnScrollChangedListener(l: OnScrollChangedListener?) {
        onScrollChangedListener = l
        currentPage.webView.setMyOnScrollChangedListener(l)
    }

    override fun setLayerType(layerType: Int, paint: Paint?) {
        this.webLayerType = layerType
        webLayerPaint = paint
        for (web in tabs) {
            web.webView.setLayerType(layerType, paint)
        }
    }

    override fun setAcceptThirdPartyCookies(manager: CookieManager, accept: Boolean) {
        acceptThirdPartyCookies = accept
        for (web in tabs) {
            web.webView.setAcceptThirdPartyCookies(manager, accept)
        }
    }

    override fun setDoubleTapFling(fling: Boolean) {
        currentPage.webView.setDoubleTapFling(fling)
    }

    override val isTouching: Boolean
        get() = currentPage.webView.isTouching

    override val isScrollable: Boolean
        get() = currentPage.webView.isScrollable

    override var isToolbarShowing: Boolean
        get() = currentPage.webView.isToolbarShowing
        set(value) {
            currentPage.webView.isToolbarShowing = value
        }

    override var isNestedScrollingEnabledMethod: Boolean
        get() = currentPage.webView.isNestedScrollingEnabledMethod
        set(value) {
            currentPage.webView.isNestedScrollingEnabledMethod = value
        }

    override val verticalScrollRange: Int
        get() = currentPage.webView.verticalScrollRange

    override fun setVerticalScrollBarEnabled(enabled: Boolean) {
        verticalScrollBarEnabled = enabled
        for (web in tabs) {
            web.webView.setVerticalScrollBarEnabled(enabled)
        }
    }

    override fun setSwipeable(swipeable: Boolean) {
        currentPage.webView.setSwipeable(swipeable)
    }

    override fun setScrollableHeight(listener: (() -> Int)?) {
        scrollableHeight = listener
        for (web in tabs) {
            web.webView.setScrollableHeight(listener)
        }
    }

    override fun onPreferenceReset() {}

    protected fun Map<String, String>.getHeaderMap(referrer: String?): ArrayMap<String, String> {
        cachedMap.clear()
        if (this is ArrayMap<String, String>) {
            cachedMap.putAllItems(this)
        } else {
            cachedMap.putAll(this)
        }

        if (referrer != null) cachedMap["Referer"] = referrer
        return cachedMap
    }

    private fun <K, V> ArrayMap<K, V>.putAllItems(map: ArrayMap<K, V>) {
        putAll(map as SimpleArrayMap<out K, out V>)
    }

    protected fun getReferrerMap(referrer: String?): MutableMap<String, String> {
        if (referrer == null) return mutableEmptyMap
        referrerMap["Referer"] = referrer

        return referrerMap
    }

    companion object {
        private val mutableEmptyMap = ArrayMap<String, String>()
        @JvmStatic
        protected val emptyMap: Map<String, String>
            get() = mutableEmptyMap

        private val referrerMap = ArrayMap<String, String>(1)
        private val cachedMap = ArrayMap<String, String>()

        internal const val CAN_NOT_MOVE = 0
        internal const val CAN_EXTERNAL_MOVE = 1
        internal const val CAN_INTERNAL_MOVE = 2
    }
}
