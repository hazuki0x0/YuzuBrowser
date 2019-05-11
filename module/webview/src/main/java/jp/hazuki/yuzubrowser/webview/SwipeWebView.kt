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
import android.graphics.Paint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import jp.hazuki.yuzubrowser.core.utility.extensions.getThemeResId
import jp.hazuki.yuzubrowser.webview.listener.OnScrollableChangeListener

internal class SwipeWebView private constructor(context: Context, override val webView: NormalWebView) : SwipeRefreshLayout(context), CustomWebView by webView, SwipeRefreshLayout.OnRefreshListener, OnScrollableChangeListener {

    constructor(context: Context, webViewId: Long = -1) : this(context, NormalWebView(context, id = webViewId))

    private var enableSwipe = false
    private var isSwipeEnable = false
    override var scrollableChangeListener: OnScrollableChangeListener? = null
    private var disableWhileZooming = false
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            disableWhileZooming = true
            return false
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            if (e.actionMasked == MotionEvent.ACTION_UP) {
                disableWhileZooming = false
            }
            return false
        }
    })

    private val mWebChromeClientWrapper = object : CustomWebChromeClientWrapper(this) {
        override fun onProgressChanged(web: CustomWebView, newProgress: Int) {
            if (isRefreshing && newProgress > 80)
                isRefreshing = false

            super.onProgressChanged(web, newProgress)
        }
    }

    private val mWebViewClientWrapper = CustomWebViewClientWrapper(this)

    init {
        webView.setOnScrollableChangeListener(this)
        addView(webView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        setOnRefreshListener(this)
    }

    override fun setMyWebChromeClient(client: CustomWebChromeClient?) {
        mWebChromeClientWrapper.setWebChromeClient(client)
        webView.setMyWebChromeClient(mWebChromeClientWrapper)
    }

    override fun setMyWebViewClient(client: CustomWebViewClient?) {
        mWebViewClientWrapper.setWebViewClient(client)
        webView.setMyWebViewClient(mWebViewClientWrapper)
    }

    override val view: View
        get() = this

    override var swipeEnable: Boolean
        get() = enableSwipe
        set(value) {
            enableSwipe = value

            setEnableInternal(value)
        }

    override fun setWebViewTheme(theme: CustomWebView.WebViewTheme?) {
        if (theme != null) {
            setColorSchemeColors(theme.progressColor)
            if (theme.backGroundColor > 0) {
                setProgressBackgroundColorSchemeColor(theme.backGroundColor)
            }
        } else {
            setColorSchemeResources(context.getThemeResId(R.attr.colorAccent))
        }
    }

    override fun onRefresh() {
        webView.reload()
        postDelayed({ isRefreshing = false }, TIMEOUT.toLong())
    }

    override fun onScrollableChanged(scrollable: Boolean) {
        setEnableInternal(enableSwipe && scrollable)
        scrollableChangeListener?.onScrollableChanged(scrollable)
    }

    override fun setEnabled(enabled: Boolean) {
        setEnableInternal(enableSwipe && webView.isSwipeable())
    }

    private fun setEnableInternal(enabled: Boolean) {
        isSwipeEnable = enabled
        if (isEnabled != enabled) {
            if (!enabled) {
                isRefreshing = false
            }
            super.setEnabled(enabled)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(ev)
        if (isSwipeEnable && isScrollable && !disableWhileZooming) {
            return super.onInterceptTouchEvent(ev)
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isSwipeEnable && isScrollable && !disableWhileZooming) {
            return super.onTouchEvent(ev)
        }
        return false
    }

    override fun setVerticalScrollBarEnabled(enabled: Boolean) {
        webView.isVerticalScrollBarEnabled = enabled
    }

    override fun hasFocus(): Boolean = webView.hasFocus()

    override fun scrollBy(x: Int, y: Int) = webView.scrollBy(x, y)

    override fun scrollTo(x: Int, y: Int) = webView.scrollTo(x, y)

    override fun setLayerType(layerType: Int, paint: Paint?) = webView.setLayerType(layerType, paint)

    override fun setScrollBarStyle(style: Int) {
        webView.scrollBarStyle = style
    }

    companion object {
        private const val TIMEOUT = 7500
    }
}
