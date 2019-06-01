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

package jp.hazuki.yuzubrowser.legacy.utils.view.fastscroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.appbar.AppBarLayout
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.utils.ThemeUtils
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.webview.CustomWebView

class WebViewFastScroller @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val mBar = View(context)
    private val mHandle = View(context)
    private var mHiddenTranslationX: Int = 0
    private val mMinScrollHandleHeight: Int
    private var mOnTouchListener: View.OnTouchListener? = null

    internal var mAppBarLayoutOffset: Int = 0

    internal var mWebView: CustomWebView? = null
    internal var mCoordinatorLayout: androidx.coordinatorlayout.widget.CoordinatorLayout? = null
    internal var mAppBarLayout: AppBarLayout? = null

    internal var mAnimator: AnimatorSet? = null
    internal var mAnimatingIn: Boolean = false

    /**
     * @property  hideDelay the delay in millis to hide the scrollbar
     */
    var hideDelay: Int = 0
    private var mHidingEnabled: Boolean = false
    private var mHandleNormalColor: Int = 0
    private var mHandlePressedColor: Int = 0
    private var mBarColor: Int = 0
    private var mTouchTargetWidth: Int = 0
    private var mBarInset: Int = 0

    private var mHideOverride: Boolean = false
    var isShowLeft: Boolean = false
        @SuppressLint("RtlHardcoded")
        set(showLeft) {
            if (this.isShowLeft != showLeft) {
                field = showLeft
                updateBarColorAndInset()
                updateHandleColorsAndInset()
                mHiddenTranslationX *= -1
                val params = layoutParams as FrameLayout.LayoutParams
                if (showLeft) {
                    params.gravity = Gravity.LEFT
                } else {
                    params.gravity = Gravity.RIGHT
                }
            }
        }
    private var isScrollEnabled = true

    var handlePressedColor: Int
        @ColorInt
        get() = mHandlePressedColor
        set(@ColorInt colorPressed) {
            mHandlePressedColor = colorPressed
            updateHandleColorsAndInset()
        }

    var handleNormalColor: Int
        @ColorInt
        get() = mHandleNormalColor
        set(@ColorInt colorNormal) {
            mHandleNormalColor = colorNormal
            updateHandleColorsAndInset()
        }

    /**
     * @property  scrollBarColor Scroll bar color. Alpha will be set to ~22% to match stock scrollbar.
     */
    var scrollBarColor: Int
        @ColorInt
        get() = mBarColor
        set(@ColorInt scrollBarColor) {
            mBarColor = scrollBarColor
            updateBarColorAndInset()
        }

    /**
     * @property  touchTargetWidth In pixels, less than or equal to 48dp
     */
    var touchTargetWidth: Int
        get() = mTouchTargetWidth
        set(touchTargetWidth) {
            mTouchTargetWidth = touchTargetWidth

            val eightDp = context.convertDpToPx(8)
            mBarInset = mTouchTargetWidth - eightDp

            val fortyEightDp = context.convertDpToPx(48)
            if (mTouchTargetWidth > fortyEightDp) {
                throw RuntimeException("Touch target width cannot be larger than 48dp!")
            }

            mBar.layoutParams = FrameLayout.LayoutParams(touchTargetWidth, ViewGroup.LayoutParams.MATCH_PARENT, GravityCompat.END)
            mHandle.layoutParams = FrameLayout.LayoutParams(touchTargetWidth, ViewGroup.LayoutParams.MATCH_PARENT, GravityCompat.END)

            updateHandleColorsAndInset()
            updateBarColorAndInset()
        }

    /**
     * @property isHidingEnabled whether hiding is enabled
     */
    var isHidingEnabled: Boolean
        get() = mHidingEnabled
        set(hidingEnabled) {
            mHidingEnabled = hidingEnabled
            if (hidingEnabled) {
                postAutoHide()
            }
        }


    private val mHide: Runnable = Runnable {
        if (!mHandle.isPressed) {
            mAnimator?.run {
                if (isStarted) {
                    cancel()
                }
            }
            mAnimator = AnimatorSet().apply {
                val animator2 = ObjectAnimator.ofFloat(this@WebViewFastScroller, View.TRANSLATION_X, mHiddenTranslationX.toFloat())
                animator2.interpolator = FastOutLinearInInterpolator()
                animator2.duration = 150
                mHandle.isEnabled = false
                play(animator2)
                start()
            }
        }
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.WebViewFastScroller, defStyleAttr, defStyleRes)

        mBarColor = a.getColor(
                R.styleable.WebViewFastScroller_fs_barColor,
                ThemeUtils.getColorFromThemeRes(context, R.attr.colorControlNormal))

        mHandleNormalColor = a.getColor(
                R.styleable.WebViewFastScroller_fs_handleNormalColor,
                ThemeUtils.getColorFromThemeRes(context, R.attr.colorControlNormal))

        mHandlePressedColor = a.getColor(
                R.styleable.WebViewFastScroller_fs_handlePressedColor,
                ThemeUtils.getColorFromThemeRes(context, R.attr.colorAccent))

        mTouchTargetWidth = a.getDimensionPixelSize(
                R.styleable.WebViewFastScroller_fs_touchTargetWidth,
                context.convertDpToPx(24))

        hideDelay = a.getInt(R.styleable.WebViewFastScroller_fs_hideDelay,
                DEFAULT_AUTO_HIDE_DELAY)

        mHidingEnabled = a.getBoolean(R.styleable.WebViewFastScroller_fs_hidingEnabled, true)

        a.recycle()

        val fortyEightDp = context.convertDpToPx(48)
        layoutParams = ViewGroup.LayoutParams(fortyEightDp, ViewGroup.LayoutParams.MATCH_PARENT)

        addView(mBar)
        addView(mHandle)

        touchTargetWidth = mTouchTargetWidth

        mMinScrollHandleHeight = fortyEightDp

        val eightDp = getContext().convertDpToPx(8)
        mHiddenTranslationX = (if (isShowLeft) -1 else 1) * eightDp

        mHandle.setOnTouchListener(object : View.OnTouchListener {
            private var mInitialBarHeight: Float = 0.toFloat()
            private var mLastPressedYAdjustedToInitial: Float = 0.toFloat()

            override fun onTouch(v: View, ev: MotionEvent): Boolean {
                val event = MotionEvent.obtain(ev)
                val action = event.actionMasked
                event.offsetLocation(0f, (-mAppBarLayoutOffset).toFloat())
                mOnTouchListener?.onTouch(v, event)

                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        mHandle.isPressed = true

                        //mRecyclerView.stopScroll();

                        var nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE
                        nestedScrollAxis = nestedScrollAxis or ViewCompat.SCROLL_AXIS_VERTICAL

                        mWebView!!.webView.startNestedScroll(nestedScrollAxis)

                        mInitialBarHeight = mBar.height.toFloat()
                        mLastPressedYAdjustedToInitial = event.y + mHandle.y + mBar.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newHandlePressedY = event.y + mHandle.y
                        val barHeight = mBar.height
                        val newHandlePressedYAdjustedToInitial = newHandlePressedY + (mInitialBarHeight - barHeight) + mBar.y

                        val deltaPressedYFromLastAdjustedToInitial = newHandlePressedYAdjustedToInitial - mLastPressedYAdjustedToInitial

                        val dY = (deltaPressedYFromLastAdjustedToInitial / mInitialBarHeight *
                                (mWebView!!.computeVerticalScrollRangeMethod() + if (mAppBarLayout == null) 0 else mAppBarLayout!!.totalScrollRange).toFloat()
                                * computeDeltaScale(event)).toInt()

                        val coordinator = mCoordinatorLayout
                        val appBar = mAppBarLayout
                        if (!AppPrefs.touch_scrollbar_fixed_toolbar.get() && coordinator != null && appBar != null) {
                            val params = appBar.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
                            val behavior = params.behavior as AppBarLayout.Behavior?
                            behavior?.onNestedPreScroll(coordinator, appBar,
                                    this@WebViewFastScroller, 0, dY, IntArray(2), ViewCompat.TYPE_TOUCH)
                        }

                        updateRvScroll(dY)

                        mLastPressedYAdjustedToInitial = newHandlePressedYAdjustedToInitial
                    }
                    MotionEvent.ACTION_UP -> {
                        mLastPressedYAdjustedToInitial = -1f

                        mWebView!!.webView.stopNestedScroll()

                        mHandle.isPressed = false
                        postAutoHide()
                    }
                }

                return true
            }
        })

        translationX = mHiddenTranslationX.toFloat()
    }

    private fun computeDeltaScale(e: MotionEvent): Float {
        val width = mWebView!!.webView.width
        val scrollbarX = if (isShowLeft) x else x + getWidth()
        var scale = Math.abs(width - scrollbarX + mWebView!!.webView.x - e.rawX) / width
        if (scale < 0.1f) {
            scale = 0.1f
        } else if (scale > 0.9f) {
            scale = 1.0f
        }
        return scale
    }

    private fun updateHandleColorsAndInset() {
        val drawable = StateListDrawable()

        if (!isShowLeft) {
            drawable.addState(View.PRESSED_ENABLED_STATE_SET,
                    InsetDrawable(ColorDrawable(mHandlePressedColor), mBarInset, 0, 0, 0))
            drawable.addState(View.EMPTY_STATE_SET,
                    InsetDrawable(ColorDrawable(mHandleNormalColor), mBarInset, 0, 0, 0))
        } else {
            drawable.addState(View.PRESSED_ENABLED_STATE_SET,
                    InsetDrawable(ColorDrawable(mHandlePressedColor), 0, 0, mBarInset, 0))
            drawable.addState(View.EMPTY_STATE_SET,
                    InsetDrawable(ColorDrawable(mHandleNormalColor), 0, 0, mBarInset, 0))
        }
        mHandle.background = drawable
    }

    private fun updateBarColorAndInset() {
        val drawable = if (!isShowLeft) {
            InsetDrawable(ColorDrawable(mBarColor), mBarInset, 0, 0, 0)
        } else {
            InsetDrawable(ColorDrawable(mBarColor), 0, 0, mBarInset, 0)
        }

        drawable.alpha = 22
        mBar.background = drawable
    }

    fun attachWebView(webView: CustomWebView) {
        mWebView = webView
        if (isScrollEnabled)
            webView.webView.isVerticalScrollBarEnabled = false
    }

    fun detachWebView() {
        mWebView?.run {
            view.removeCallbacks(mHide)
            webView.isVerticalScrollBarEnabled = true
        }
        mWebView = null
    }

    fun attachAppBarLayout(coordinatorLayout: androidx.coordinatorlayout.widget.CoordinatorLayout, appBarLayout: AppBarLayout) {
        mCoordinatorLayout = coordinatorLayout
        mAppBarLayout = appBarLayout

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            show(true)

            val layoutParams = layoutParams as ViewGroup.MarginLayoutParams

            mAppBarLayoutOffset = -verticalOffset

            setLayoutParams(layoutParams)
        })
    }

    fun setOnHandleTouchListener(listener: View.OnTouchListener) {
        mOnTouchListener = listener
    }

    fun onPageScroll() {
        show(true)
    }

    /**
     * Show the fast scroller and hide after delay
     *
     * @param animate whether to animate showing the scroller
     */
    fun show(animate: Boolean) {
        requestLayout()

        post(Runnable {
            if (mHideOverride) {
                return@Runnable
            }

            mHandle.isEnabled = true
            if (animate) {
                if (!mAnimatingIn && translationX != 0f) {
                    mAnimator?.run {
                        if (isStarted) {
                            cancel()
                        }
                    }
                    mAnimator = AnimatorSet().apply {
                        val animator = ObjectAnimator.ofFloat(this@WebViewFastScroller, View.TRANSLATION_X, 0f)
                        animator.interpolator = LinearOutSlowInInterpolator()
                        animator.duration = 100
                        animator.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                mAnimatingIn = false
                            }
                        })
                        mAnimatingIn = true
                        play(animator)
                        start()
                    }
                }
            } else {
                translationX = 0f
            }
            postAutoHide()
        })
    }

    internal fun postAutoHide() {
        val webView = mWebView
        if (webView != null && mHidingEnabled) {
            webView.view.removeCallbacks(mHide)
            webView.view.postDelayed(mHide, hideDelay.toLong())
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val webView = mWebView ?: return

        val appBarScrollRange = mAppBarLayout?.totalScrollRange ?: 0
        val scrollOffset = webView.computeVerticalScrollOffsetMethod() + mAppBarLayoutOffset
        val verticalScrollRange = webView.computeVerticalScrollRangeMethod() + appBarScrollRange

        val barHeight = mBar.height

        val ratio = scrollOffset.toFloat() / (verticalScrollRange - barHeight)

        var calculatedHandleHeight = (barHeight.toFloat() / verticalScrollRange * barHeight).toInt()
        if (calculatedHandleHeight < mMinScrollHandleHeight) {
            calculatedHandleHeight = mMinScrollHandleHeight
        }

        if (calculatedHandleHeight >= barHeight || !webView.isScrollable) {
            translationX = mHiddenTranslationX.toFloat()
            mHideOverride = true
            return
        }

        mHideOverride = false

        val y = ratio * (barHeight - calculatedHandleHeight) + mAppBarLayoutOffset - appBarScrollRange

        mHandle.layout(mHandle.left, y.toInt(), mHandle.right, y.toInt() + calculatedHandleHeight)
    }

    internal fun updateRvScroll(dY: Int) {
        mWebView?.run {
            try {
                scrollBy(0, dY)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    fun setScrollEnabled(enable: Boolean) {
        if (this.isScrollEnabled != enable) {
            this.isScrollEnabled = enable
            if (enable) {
                visibility = View.VISIBLE
                mWebView?.run {
                    webView.isVerticalScrollBarEnabled = false
                }
            } else {
                visibility = View.GONE
                mWebView?.run {
                    webView.isVerticalScrollBarEnabled = true
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_AUTO_HIDE_DELAY = 1500
    }
}
