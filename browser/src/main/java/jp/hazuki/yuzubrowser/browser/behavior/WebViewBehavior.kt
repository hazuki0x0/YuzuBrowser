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

package jp.hazuki.yuzubrowser.browser.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.appbar.AppBarLayout
import jp.hazuki.yuzubrowser.browser.R
import jp.hazuki.yuzubrowser.legacy.browser.BrowserController
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.ui.widget.PaddingFrameLayout
import jp.hazuki.yuzubrowser.webview.CustomWebView

class WebViewBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.ScrollingViewBehavior(context, attrs) {

    private var isInitialized = false
    private lateinit var topToolBar: View
    private lateinit var bottomBar: View
    private lateinit var paddingFrame: View
    private lateinit var overlayPaddingFrame: PaddingFrameLayout
    private lateinit var controller: BrowserController
    private var webView: CustomWebView? = null
    private var prevY: Int = 0
    private var paddingHeight = 0
    var isImeShown = false

    override fun layoutDependsOn(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: View, dependency: View): Boolean {
        if (dependency is AppBarLayout) {
            topToolBar = parent.findViewById(R.id.topToolbar)
            bottomBar = parent.findViewById(R.id.bottomOverlayLayout)
            paddingFrame = child.findViewById(R.id.toolbarPadding)
            overlayPaddingFrame = child.findViewById(R.id.bottomAlwaysOverlayToolbarPadding)
            isInitialized = true
            return true
        }
        return false
    }

    override fun onDependentViewChanged(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: View, dependency: View): Boolean {
        val bottom = dependency.bottom

        val webView = webView
        if (webView != null) {
            webView.isToolbarShowing = dependency.top == 0
            if (!webView.isTouching && webView.webScrollY != 0) {
                webView.scrollBy(0, bottom - prevY)
                if (bottom == 0) {
                    webView.setSwipeable(false)
                }
            }

            val data = controller.getTabOrNull(webView)
            if (data != null) {
                adjustWebView(data, topToolBar.height + bottomBar.height)
            }
        }

        prevY = bottom

        return super.onDependentViewChanged(parent, child, dependency)
    }

    fun adjustWebView(data: MainTabData, height: Int) {
        if (!isInitialized) return
        if (paddingHeight != height) {
            paddingHeight = height
            val params = paddingFrame.layoutParams
            params.height = height
            paddingFrame.layoutParams = params
            data.mWebView.computeVerticalScrollRangeMethod()
        }

        if (data.isFinished && !data.mWebView.isScrollable && !isImeShown) {
            controller.expandToolbar()
            data.mWebView.isNestedScrollingEnabledMethod = false
            paddingFrame.visibility = View.VISIBLE
            overlayPaddingFrame.forceHide = true
            data.mWebView.computeVerticalScrollRangeMethod()
        } else {
            paddingFrame.visibility = View.GONE
            overlayPaddingFrame.forceHide = false
            data.mWebView.computeVerticalScrollRangeMethod()
        }
    }

    fun setWebView(webView: CustomWebView?) {
        this.webView = webView
    }

    fun setController(browserController: BrowserController) {
        controller = browserController
    }
}
