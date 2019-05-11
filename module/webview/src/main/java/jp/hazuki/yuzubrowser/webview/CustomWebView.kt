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

import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Bundle
import android.os.Message
import android.print.PrintDocumentAdapter
import android.view.View
import android.webkit.*
import jp.hazuki.yuzubrowser.core.utility.common.listener.OnTouchEventListener
import jp.hazuki.yuzubrowser.webview.WebViewRenderingManager.Companion.RENDERING_INVERT
import jp.hazuki.yuzubrowser.webview.WebViewRenderingManager.Companion.RENDERING_INVERT_GRAY
import jp.hazuki.yuzubrowser.webview.listener.OnScrollChangedListener
import jp.hazuki.yuzubrowser.webview.listener.OnScrollableChangeListener

interface CustomWebView {

    val favicon: Bitmap?

    val hitTestResult: WebView.HitTestResult?

    val originalUrl: String?

    val progress: Int

    val webSettings: YuzuWebSettings

    val title: String?

    val url: String?

    val webScrollX: Int

    val webScrollY: Int

    val view: View

    val webView: WebView

    val theme: WebViewTheme?

    var swipeEnable: Boolean

    val isBackForwardListEmpty: Boolean

    val overScrollModeMethod: Int

    var identityId: Long

    val isTouching: Boolean

    val isScrollable: Boolean

    var isToolbarShowing: Boolean

    var isNestedScrollingEnabledMethod: Boolean

    var scrollableChangeListener: OnScrollableChangeListener?

    val verticalScrollRange: Int

    val isRedirect: Boolean
        get() {
            val hitTestResult = hitTestResult
            return if (hitTestResult != null) {
                hitTestResult.type <= 0
            } else {
                false
            }
        }

    var renderingMode: Int

    val isInvertMode: Boolean
        get() = renderingMode == RENDERING_INVERT || renderingMode == RENDERING_INVERT_GRAY

    fun canGoBack(): Boolean

    fun canGoBackOrForward(steps: Int): Boolean

    fun canGoForward(): Boolean

    fun clearCache(includeDiskFiles: Boolean)

    fun clearFormData()

    fun clearHistory()

    fun clearMatches()

    fun copyMyBackForwardList(): CustomWebBackForwardList

    fun destroy()

    fun findAllAsync(find: String)

    fun setFindListener(listener: WebView.FindListener)

    fun findNext(forward: Boolean)

    fun flingScroll(vx: Int, vy: Int)

    @Deprecated("")
    fun getHttpAuthUsernamePassword(host: String, realm: String): Array<String>?

    fun goBack()

    fun goBackOrForward(steps: Int)

    fun goForward()

    fun loadUrl(url: String?)

    fun loadUrl(url: String?, additionalHttpHeaders: MutableMap<String, String>?)

    fun evaluateJavascript(js: String?, callback: ValueCallback<String>?)

    fun onPause()

    fun onResume()

    fun pageDown(bottom: Boolean): Boolean

    fun pageUp(top: Boolean): Boolean

    fun pauseTimers()

    fun reload()

    fun requestWebFocus(): Boolean

    fun requestFocusNodeHref(hrefMsg: Message)

    fun requestImageRef(msg: Message)

    fun restoreState(inState: Bundle): WebBackForwardList?

    fun resumeTimers()

    fun saveState(outState: Bundle): WebBackForwardList?

    fun setDownloadListener(listener: DownloadListener?)

    @Deprecated("")
    fun setHttpAuthUsernamePassword(host: String, realm: String, username: String, password: String)

    fun setNetworkAvailable(networkUp: Boolean)

    fun setScrollBarStyle(style: Int)

    fun setMyWebChromeClient(client: CustomWebChromeClient?)

    fun setMyWebViewClient(client: CustomWebViewClient?)

    fun stopLoading()

    fun zoomIn(): Boolean

    fun zoomOut(): Boolean

    fun hasFocus(): Boolean

    fun setOnMyCreateContextMenuListener(webContextMenuListener: CustomOnCreateContextMenuListener?)

    fun scrollTo(x: Int, y: Int)

    fun scrollBy(x: Int, y: Int)

    fun saveWebArchiveMethod(filename: String): Boolean

    fun setWebViewTouchDetector(d: OnTouchEventListener?)

    fun setMyOnScrollChangedListener(l: OnScrollChangedListener?)

    fun setEmbeddedTitleBarMethod(view: View?): Boolean

    fun notifyFindDialogDismissedMethod(): Boolean

    fun setOverScrollModeMethod(arg: Int): Boolean

    fun computeVerticalScrollRangeMethod(): Int

    fun computeVerticalScrollOffsetMethod(): Int

    fun computeVerticalScrollExtentMethod(): Int

    fun computeHorizontalScrollRangeMethod(): Int

    fun computeHorizontalScrollOffsetMethod(): Int

    fun computeHorizontalScrollExtentMethod(): Int

    fun createPrintDocumentAdapter(documentName: String?): PrintDocumentAdapter?

    fun loadDataWithBaseURL(baseUrl: String, data: String,
                            mimeType: String, encoding: String, historyUrl: String)

    fun setWebViewTheme(theme: WebViewTheme?)

    fun setLayerType(layerType: Int, paint: Paint?)

    fun onPreferenceReset()

    fun setAcceptThirdPartyCookies(manager: CookieManager, accept: Boolean)

    fun setDoubleTapFling(fling: Boolean)

    fun setVerticalScrollBarEnabled(enabled: Boolean)

    fun setSwipeable(swipeable: Boolean)

    fun setScrollableHeight(listener: (() -> Int)?)

    data class WebViewTheme(val progressColor: Int, val backGroundColor: Int)
}
