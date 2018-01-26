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

package jp.hazuki.yuzubrowser.utils.view.behavior

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.browser.BrowserController
import jp.hazuki.yuzubrowser.tab.manager.MainTabData

import jp.hazuki.yuzubrowser.webkit.CustomWebView

class WebViewBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.ScrollingViewBehavior(context, attrs) {

    private var isInitialized = false
    private lateinit var topToolBar: View
    private lateinit var bottomBar: View
    private lateinit var webFrame: View
    private lateinit var controller: BrowserController
    private var webView: CustomWebView? = null
    private var prevY: Int = 0

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View?): Boolean {
        if (dependency is AppBarLayout) {
            topToolBar = parent.findViewById(R.id.topToolbarLayout)
            bottomBar = parent.findViewById(R.id.bottomOverlayLayout)
            webFrame = child.findViewById(R.id.webFrameLayout)
            isInitialized = true
            return true
        }
        return false
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dependency: View): Boolean {
        val bottom = dependency.bottom

        val webView = webView
        if (webView != null) {
            webView.isToolbarShowing = dependency.top == 0
            if (!webView.isTouching) {
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

        if (data.isFinished && !data.mWebView.isScrollable) {
            controller.expandToolbar()
            data.mWebView.isNestedScrollingEnabledMethod = false
            webFrame.setPadding(0, 0, 0, height)
        } else {
            webFrame.setPadding(0, 0, 0, 0)
        }
    }

    fun setWebView(webView: CustomWebView?) {
        this.webView = webView
    }

    fun setController(browserController: BrowserController) {
        controller = browserController
    }
}
