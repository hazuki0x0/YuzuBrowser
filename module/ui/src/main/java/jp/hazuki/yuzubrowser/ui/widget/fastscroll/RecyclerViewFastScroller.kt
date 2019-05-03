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

package jp.hazuki.yuzubrowser.ui.widget.fastscroll

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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.appbar.AppBarLayout
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.ui.R
import jp.hazuki.yuzubrowser.ui.extensions.getColorFromThemeRes

class RecyclerViewFastScroller @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val bar = View(context)
    private val handle = View(context)
    private var hiddenTranslationX: Int = 0
    private val minScrollHandleHeight: Int
    private var mOnTouchListener: OnTouchListener? = null

    internal var appBarLayoutOffset: Int = 0

    internal var recyclerView: androidx.recyclerview.widget.RecyclerView? = null
    internal var coordinatorLayout: CoordinatorLayout? = null
    internal var appBarLayout: AppBarLayout? = null

    internal var animator: AnimatorSet? = null
    internal var animatingIn: Boolean = false

    /**
     * @property  hideDelay the delay in millis to hide the scrollbar
     */
    var hideDelay: Long = 0
    private var hidingEnabled: Boolean = false
    private var barInset: Int = 0

    private var hideOverride: Boolean = false
    private var adapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>? = null
    private val adapterObserver = object : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            requestLayout()
        }
    }

    var isShowLeft: Boolean = false
        @SuppressLint("RtlHardcoded")
        set(showLeft) {
            if (this.isShowLeft != showLeft) {
                field = showLeft
                updateBarColorAndInset()
                updateHandleColorsAndInset()
                hiddenTranslationX *= -1
                val params = layoutParams as LayoutParams
                if (showLeft) {
                    params.gravity = Gravity.LEFT
                } else {
                    params.gravity = Gravity.RIGHT
                }
            }
        }

    @ColorInt
    var handlePressedColor = 0
        set(@ColorInt colorPressed) {
            field = colorPressed
            updateHandleColorsAndInset()
        }

    @ColorInt
    var handleNormalColor = 0
        set(@ColorInt colorNormal) {
            field = colorNormal
            updateHandleColorsAndInset()
        }

    /**
     * @property  scrollBarColor Scroll bar color. Alpha will be set to ~22% to match stock scrollbar.
     */
    @ColorInt
    var scrollBarColor = 0
        set(@ColorInt scrollBarColor) {
            field = scrollBarColor
            updateBarColorAndInset()
        }

    /**
     * @property  touchTargetWidth In pixels, less than or equal to 48dp
     */
    var touchTargetWidth = 0
        set(touchTargetWidth) {
            field = touchTargetWidth

            val eightDp = context.convertDpToPx(8)
            barInset = this.touchTargetWidth - eightDp

            val fortyEightDp = context.convertDpToPx(48)
            if (this.touchTargetWidth > fortyEightDp) {
                throw RuntimeException("Touch target width cannot be larger than 48dp!")
            }

            bar.layoutParams = LayoutParams(touchTargetWidth, ViewGroup.LayoutParams.MATCH_PARENT, GravityCompat.END)
            handle.layoutParams = LayoutParams(touchTargetWidth, ViewGroup.LayoutParams.MATCH_PARENT, GravityCompat.END)

            updateHandleColorsAndInset()
            updateBarColorAndInset()
        }

    /**
     * @property isHidingEnabled whether hiding is enabled
     */
    var isHidingEnabled: Boolean
        get() = hidingEnabled
        set(hidingEnabled) {
            this.hidingEnabled = hidingEnabled
            if (hidingEnabled) {
                postAutoHide()
            }
        }

    var isFixed: Boolean = false


    private val mHide: Runnable = Runnable {
        if (!handle.isPressed) {
            animator?.run {
                if (isStarted) {
                    cancel()
                }
            }
            animator = AnimatorSet().apply {
                val animator2 = ObjectAnimator.ofFloat(this@RecyclerViewFastScroller, View.TRANSLATION_X, hiddenTranslationX.toFloat())
                animator2.interpolator = FastOutLinearInInterpolator()
                animator2.duration = 150
                handle.isEnabled = false
                play(animator2)
                start()
            }
        }
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.WebViewFastScroller, defStyleAttr, defStyleRes)

        this.scrollBarColor = a.getColor(
                R.styleable.WebViewFastScroller_fs_barColor,
            context.getColorFromThemeRes(R.attr.colorControlNormal))

        this.handleNormalColor = a.getColor(
                R.styleable.WebViewFastScroller_fs_handleNormalColor,
            context.getColorFromThemeRes(R.attr.colorControlNormal))

        this.handlePressedColor = a.getColor(
                R.styleable.WebViewFastScroller_fs_handlePressedColor,
            context.getColorFromThemeRes(R.attr.colorAccent))

        this.touchTargetWidth = a.getDimensionPixelSize(
                R.styleable.WebViewFastScroller_fs_touchTargetWidth,
                context.convertDpToPx(24))

        hideDelay = a.getInt(R.styleable.WebViewFastScroller_fs_hideDelay,
                DEFAULT_AUTO_HIDE_DELAY).toLong()

        hidingEnabled = a.getBoolean(R.styleable.WebViewFastScroller_fs_hidingEnabled, true)

        a.recycle()

        val fortyEightDp = context.convertDpToPx(48)
        layoutParams = ViewGroup.LayoutParams(fortyEightDp, ViewGroup.LayoutParams.MATCH_PARENT)

        addView(bar)
        addView(handle)

        this.touchTargetWidth = this.touchTargetWidth

        minScrollHandleHeight = fortyEightDp

        val eightDp = getContext().convertDpToPx(8)
        hiddenTranslationX = (if (isShowLeft) -1 else 1) * eightDp

        handle.setOnTouchListener(object : OnTouchListener {
            private var mInitialBarHeight: Float = 0.toFloat()
            private var mLastPressedYAdjustedToInitial: Float = 0.toFloat()

            override fun onTouch(v: View, ev: MotionEvent): Boolean {
                val recyclerView = recyclerView ?: return true
                val event = MotionEvent.obtain(ev)
                val action = event.actionMasked
                event.offsetLocation(0f, (-appBarLayoutOffset).toFloat())
                mOnTouchListener?.onTouch(v, event)

                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        handle.isPressed = true

                        recyclerView.stopScroll()

                        var nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE
                        nestedScrollAxis = nestedScrollAxis or ViewCompat.SCROLL_AXIS_VERTICAL

                        recyclerView.startNestedScroll(nestedScrollAxis)

                        mInitialBarHeight = bar.height.toFloat()
                        mLastPressedYAdjustedToInitial = event.y + handle.y + bar.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newHandlePressedY = event.y + handle.y
                        val barHeight = bar.height
                        val newHandlePressedYAdjustedToInitial = newHandlePressedY + (mInitialBarHeight - barHeight) + bar.y

                        val deltaPressedYFromLastAdjustedToInitial = newHandlePressedYAdjustedToInitial - mLastPressedYAdjustedToInitial

                        val dY = (deltaPressedYFromLastAdjustedToInitial / mInitialBarHeight *
                                (recyclerView.computeVerticalScrollRange() + if (appBarLayout == null) 0 else appBarLayout!!.totalScrollRange).toFloat()
                                * computeDeltaScale(event)).toInt()

                        val coordinator = coordinatorLayout
                        val appBar = appBarLayout
                        if (!isFixed && coordinator != null && appBar != null) {
                            val params = appBar.layoutParams as CoordinatorLayout.LayoutParams
                            val behavior = params.behavior as AppBarLayout.Behavior?
                            behavior?.onNestedPreScroll(coordinator, appBar,
                                    this@RecyclerViewFastScroller, 0, dY, IntArray(2), ViewCompat.TYPE_TOUCH)
                        }

                        updateRvScroll(dY)

                        mLastPressedYAdjustedToInitial = newHandlePressedYAdjustedToInitial
                    }
                    MotionEvent.ACTION_UP -> {
                        mLastPressedYAdjustedToInitial = -1f

                        recyclerView.stopNestedScroll()

                        handle.isPressed = false
                        postAutoHide()
                    }
                }

                return true
            }
        })

        translationX = hiddenTranslationX.toFloat()
    }

    private fun computeDeltaScale(e: MotionEvent): Float {
        val width = recyclerView!!.width
        val scrollbarX = if (isShowLeft) x else x + getWidth()
        var scale = Math.abs(width - scrollbarX + recyclerView!!.x - e.rawX) / width
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
                    InsetDrawable(ColorDrawable(this.handlePressedColor), barInset, 0, 0, 0))
            drawable.addState(View.EMPTY_STATE_SET,
                    InsetDrawable(ColorDrawable(this.handleNormalColor), barInset, 0, 0, 0))
        } else {
            drawable.addState(View.PRESSED_ENABLED_STATE_SET,
                    InsetDrawable(ColorDrawable(this.handlePressedColor), 0, 0, barInset, 0))
            drawable.addState(View.EMPTY_STATE_SET,
                    InsetDrawable(ColorDrawable(this.handleNormalColor), 0, 0, barInset, 0))
        }
        handle.background = drawable
    }

    private fun updateBarColorAndInset() {
        val drawable = if (!isShowLeft) {
            InsetDrawable(ColorDrawable(this.scrollBarColor), barInset, 0, 0, 0)
        } else {
            InsetDrawable(ColorDrawable(this.scrollBarColor), 0, 0, barInset, 0)
        }

        drawable.alpha = 22
        bar.background = drawable
    }

    fun attachRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                show(true)
            }
        })
        recyclerView.adapter?.let {
            attachAdapter(it)
        }
    }

    fun attachAdapter(adapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>?) {
        if (this.adapter === adapter) return
        this.adapter?.unregisterAdapterDataObserver(adapterObserver)
        adapter?.registerAdapterDataObserver(adapterObserver)
        this.adapter = adapter
    }

    fun attachAppBarLayout(coordinatorLayout: CoordinatorLayout, appBarLayout: AppBarLayout) {
        this.coordinatorLayout = coordinatorLayout
        this.appBarLayout = appBarLayout
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            show(true)

            val layoutParams = layoutParams as ViewGroup.MarginLayoutParams

            appBarLayoutOffset = -verticalOffset

            setLayoutParams(layoutParams)
        })
    }

    fun setOnHandleTouchListener(listener: OnTouchListener) {
        mOnTouchListener = listener
    }

    /**
     * Show the fast scroller and hide after delay
     *
     * @param animate whether to animate showing the scroller
     */
    fun show(animate: Boolean) {
        requestLayout()

        post(Runnable {
            if (hideOverride) {
                return@Runnable
            }

            handle.isEnabled = true
            if (animate) {
                if (!animatingIn && translationX != 0f) {
                    animator?.run {
                        if (isStarted) {
                            cancel()
                        }
                    }
                    animator = AnimatorSet().apply {
                        val animator = ObjectAnimator.ofFloat(this@RecyclerViewFastScroller, View.TRANSLATION_X, 0f)
                        animator.interpolator = LinearOutSlowInInterpolator()
                        animator.duration = 100
                        animator.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                animatingIn = false
                            }
                        })
                        animatingIn = true
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
        val recyclerView = recyclerView
        if (recyclerView != null && hidingEnabled) {
            recyclerView.removeCallbacks(mHide)
            recyclerView.postDelayed(mHide, hideDelay)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val recyclerView = recyclerView ?: return

        val appBarScrollRange = appBarLayout?.totalScrollRange ?: 0
        val scrollOffset = recyclerView.computeVerticalScrollOffset() + appBarLayoutOffset
        val verticalScrollRange = recyclerView.computeVerticalScrollRange() + appBarScrollRange

        val barHeight = bar.height

        val ratio = scrollOffset.toFloat() / (verticalScrollRange - barHeight)

        var calculatedHandleHeight = (barHeight.toFloat() / verticalScrollRange * barHeight).toInt()
        if (calculatedHandleHeight < minScrollHandleHeight) {
            calculatedHandleHeight = minScrollHandleHeight
        }

        if (calculatedHandleHeight >= barHeight) {
            translationX = hiddenTranslationX.toFloat()
            hideOverride = true
            return
        }

        hideOverride = false

        val y = ratio * (barHeight - calculatedHandleHeight) + appBarLayoutOffset - appBarScrollRange

        handle.layout(handle.left, y.toInt(), handle.right, y.toInt() + calculatedHandleHeight)
    }

    internal fun updateRvScroll(dY: Int) {
        recyclerView?.run {
            try {
                scrollBy(0, dY)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    companion object {
        private const val DEFAULT_AUTO_HIDE_DELAY = 1500
    }
}
