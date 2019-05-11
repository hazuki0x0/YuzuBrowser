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
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import jp.hazuki.yuzubrowser.core.utility.common.listener.OnTouchEventListener
import jp.hazuki.yuzubrowser.webview.listener.OnScrollChangedListener
import jp.hazuki.yuzubrowser.webview.listener.OnScrollableChangeListener

internal class NormalWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = android.R.attr.webViewStyle, id: Long = -1) : JvmWebViewBridge(context, attrs, defStyle) {

    private var titleBar: View? = null
    private var nestedOffsetY = 0
    private var doubleTapFling = false
    private var isSwipeable = true
    private var firstY = 0
    private var lastY = 0
    private var downScrollY = 0
    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)
    private var scrollExcessPlay = false
    private var nestedScrolled = false
    private var firstScroll = true
    override val webSettings = YuzuWebSettings(settings)
    override var theme: CustomWebView.WebViewTheme? = null
        private set

    private var touchDetector: OnTouchEventListener? = null
    private var onScrollChangedListener: OnScrollChangedListener? = null
    override var scrollableChangeListener: OnScrollableChangeListener? = null

    private val childHelper = NestedScrollingChildHelper(this)
    private val scrollSlop: Int = ViewConfiguration.get(context).scaledPagingTouchSlop
    private var scrollableHeight: (() -> Int)? = null

    init {
        isNestedScrollingEnabled = true
    }

    override val isBackForwardListEmpty: Boolean
        get() = copyBackForwardList().size == 0

    override val overScrollModeMethod: Int
        get() = overScrollMode

    override var identityId: Long = if (id >= 0) id else System.currentTimeMillis()
        set(value) {
            if (field > value) {
                field = value
            }
        }

    override var verticalScrollRange: Int = -1
        get() {
            if (field == -1) computeVerticalScrollRange()
            return field
        }

    override var isTouching: Boolean = false
        private set
    override var isScrollable: Boolean = false
        private set
    override var isToolbarShowing: Boolean = false
    override var renderingMode = 0

    override fun copyMyBackForwardList(): CustomWebBackForwardList = CustomWebBackForwardList(copyBackForwardList())

    override fun setMyWebChromeClient(client: CustomWebChromeClient?) {
        webChromeClient = client
    }

    override fun setMyWebViewClient(client: CustomWebViewClient?) {
        webViewClient = client
    }

    override fun setOnMyCreateContextMenuListener(webContextMenuListener: CustomOnCreateContextMenuListener?) {
        setOnCreateContextMenuListener(webContextMenuListener)
    }

    override fun setWebViewTouchDetector(d: OnTouchEventListener?) {
        touchDetector = d
    }

    override fun setMyOnScrollChangedListener(l: OnScrollChangedListener?) {
        onScrollChangedListener = l
    }

    fun setOnScrollableChangeListener(listener: OnScrollableChangeListener) {
        scrollableChangeListener = listener
    }

    override fun setEmbeddedTitleBarMethod(view: View?): Boolean {
        if (titleBar !== view) {
            if (titleBar != null) {
                removeView(titleBar)
            }
            if (view != null) {
                if (view.parent != null) {
                    (view.parent as ViewGroup).removeView(view)
                }
                addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0, 0))
                view.translationX = scrollX.toFloat()//can move X
            }
            titleBar = view
        }
        return true
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrollChangedListener?.invoke(this, l, t, oldl, oldt)
        titleBar?.translationX = l.toFloat() //can move X
    }

    override fun notifyFindDialogDismissedMethod(): Boolean = false

    override fun setWebViewTheme(theme: CustomWebView.WebViewTheme?) {
        this.theme = theme
    }

    override fun onPreferenceReset() {}

    override fun setDoubleTapFling(fling: Boolean) {
        doubleTapFling = fling
    }

    override fun scrollTo(x: Int, y: Int) {
        val px = when {
            x < 0 -> 0
            x > computeHorizontalScrollRange() - computeHorizontalScrollExtent() -> computeHorizontalScrollRange() - computeHorizontalScrollExtent()
            else -> x
        }
        val py = when {
            y < 0 -> 0
            y > computeVerticalScrollRange() - computeVerticalScrollExtent() -> computeVerticalScrollRange() - computeVerticalScrollExtent()
            else -> y
        }

        super.scrollTo(px, py)
    }

    override fun scrollBy(x: Int, y: Int) {
        scrollTo(x + scrollX, y + scrollY)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        val touchDetector = touchDetector
        if (touchDetector != null && ev != null && touchDetector.onTouchEvent(ev)) {
            ev.action = MotionEvent.ACTION_CANCEL
            super.onTouchEvent(ev)
            return true
        }

        val event = MotionEvent.obtain(ev)
        val action = event.actionMasked

        if (doubleTapFling) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE)
                doubleTapFling = false
            return super.onTouchEvent(ev)
        }

        val returnValue: Boolean
        if (action == MotionEvent.ACTION_DOWN) {
            nestedOffsetY = 0
            isTouching = true
        }
        val eventY = event.y.toInt()
        event.offsetLocation(0f, nestedOffsetY.toFloat())
        when (action) {
            MotionEvent.ACTION_MOVE -> if (event.pointerCount != 1) {
                returnValue = super.onTouchEvent(event)
            } else if (scrollExcessPlay && Math.abs(firstY - eventY) < scrollSlop || downScrollY != 0 && downScrollY == scrollY) {
                returnValue = super.onTouchEvent(ev)
                lastY = eventY
            } else {
                var deltaY = lastY - eventY
                if (scrollExcessPlay) {
                    scrollExcessPlay = false
                    // start NestedScroll
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
                }

                if (scrollY > scrollSlop) {
                    setSwipeable(false)
                }

                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1]
                    lastY = eventY - mScrollOffset[1]
                    event.offsetLocation(0f, (-mScrollOffset[1]).toFloat())
                    nestedOffsetY = mScrollOffset[1]
                    setSwipeable(false)
                }
                returnValue = super.onTouchEvent(event)

                // NestedScroll
                if (dispatchNestedScroll(0, mScrollConsumed[1], 0, deltaY, mScrollOffset)) {
                    event.offsetLocation(0f, mScrollOffset[1].toFloat())
                    nestedOffsetY = mScrollOffset[1]
                    lastY -= deltaY
                    nestedScrolled = true
                    setSwipeable(false)
                } else {
                    nestedScrolled = false
                }
            }
            MotionEvent.ACTION_DOWN -> {
                returnValue = super.onTouchEvent(ev)
                scrollExcessPlay = true
                if (firstScroll) {
                    lastY = eventY - 5
                    firstScroll = false
                } else {
                    lastY = eventY
                }
                firstY = eventY
                downScrollY = scrollY
                if (downScrollY < scrollSlop && isToolbarShowing) {
                    setSwipeable(true)
                }
            }
            else -> {
                isTouching = false
                returnValue = super.onTouchEvent(ev)
                // end NestedScroll
                stopNestedScroll()
                if (scrollY < scrollSlop && isToolbarShowing) {
                    setSwipeable(true)
                }
            }
        }
        return returnValue
    }

    override fun destroy() {
        setDownloadListener(null)
        setEmbeddedTitleBarMethod(null)
        setWebViewTouchDetector(null)
        setMyWebChromeClient(null)
        setMyWebViewClient(null)
        setOnMyCreateContextMenuListener(null)
        setMyOnScrollChangedListener(null)

        post { super.destroy() }
    }

    // Nested Scroll implements
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean = childHelper.isNestedScrollingEnabled

    override fun startNestedScroll(axes: Int): Boolean = childHelper.startNestedScroll(axes)

    override fun stopNestedScroll() {
        childHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean = childHelper.hasNestedScrollingParent()

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean =
            childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean =
            childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean =
            childHelper.dispatchNestedFling(velocityX, velocityY, consumed)

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean =
            childHelper.dispatchNestedPreFling(velocityX, velocityY)

    override fun setSwipeable(swipeable: Boolean) {
        if (isSwipeable != swipeable) {
            isSwipeable = swipeable
            scrollableChangeListener?.onScrollableChanged(isScrollable && isSwipeable)
        }
    }

    fun isSwipeable(): Boolean {
        return isSwipeable
    }

    override fun computeVerticalScrollRange(): Int {
        val scrollRange = super.computeVerticalScrollRange()
        verticalScrollRange = scrollRange
        val old = isScrollable
        isScrollable = scrollRange > height + scrollSlop + (scrollableHeight?.invoke() ?: 0)
        if (old != isScrollable) {
            scrollableChangeListener?.onScrollableChanged(isScrollable && isSwipeable)
        }

        if (isScrollable && !isNestedScrollingEnabled) {
            isNestedScrollingEnabledMethod = true
        }

        return scrollRange
    }

    override fun setScrollableHeight(listener: (() -> Int)?) {
        scrollableHeight = listener
    }
}
