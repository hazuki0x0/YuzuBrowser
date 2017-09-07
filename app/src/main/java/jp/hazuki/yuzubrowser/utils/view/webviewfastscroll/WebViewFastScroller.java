/*
 * Copyright 2016 Daniel Ciao
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

package jp.hazuki.yuzubrowser.utils.view.webviewfastscroll;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.utils.ThemeUtils;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class WebViewFastScroller extends FrameLayout {
    private static final int DEFAULT_AUTO_HIDE_DELAY = 1500;

    protected final View mBar;
    protected final View mHandle;
    private int mHiddenTranslationX;
    private final Runnable mHide;
    private final int mMinScrollHandleHeight;
    protected OnTouchListener mOnTouchListener;

    int mAppBarLayoutOffset;

    CustomWebView mWebView;
    CoordinatorLayout mCoordinatorLayout;
    AppBarLayout mAppBarLayout;

    AnimatorSet mAnimator;
    boolean mAnimatingIn;

    private int mHideDelay;
    private boolean mHidingEnabled;
    private int mHandleNormalColor;
    private int mHandlePressedColor;
    private int mBarColor;
    private int mTouchTargetWidth;
    private int mBarInset;

    private boolean mHideOverride;
    private boolean showLeft;
    private boolean enabled = true;

    public WebViewFastScroller(Context context) {
        this(context, null, 0);
    }

    public WebViewFastScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebViewFastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WebViewFastScroller(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WebViewFastScroller, defStyleAttr, defStyleRes);

        mBarColor = a.getColor(
                R.styleable.WebViewFastScroller_wfs_barColor,
                ThemeUtils.getColorFromThemeRes(context, R.attr.colorControlNormal));

        mHandleNormalColor = a.getColor(
                R.styleable.WebViewFastScroller_wfs_handleNormalColor,
                ThemeUtils.getColorFromThemeRes(context, R.attr.colorControlNormal));

        mHandlePressedColor = a.getColor(
                R.styleable.WebViewFastScroller_wfs_handlePressedColor,
                ThemeUtils.getColorFromThemeRes(context, R.attr.colorAccent));

        mTouchTargetWidth = a.getDimensionPixelSize(
                R.styleable.WebViewFastScroller_wfs_touchTargetWidth,
                DisplayUtils.convertDpToPx(context, 24));

        mHideDelay = a.getInt(R.styleable.WebViewFastScroller_wfs_hideDelay,
                DEFAULT_AUTO_HIDE_DELAY);

        mHidingEnabled = a.getBoolean(R.styleable.WebViewFastScroller_wfs_hidingEnabled, true);

        a.recycle();

        int fortyEightDp = DisplayUtils.convertDpToPx(context, 48);
        setLayoutParams(new ViewGroup.LayoutParams(fortyEightDp, ViewGroup.LayoutParams.MATCH_PARENT));

        mBar = new View(context);
        mHandle = new View(context);
        addView(mBar);
        addView(mHandle);

        setTouchTargetWidth(mTouchTargetWidth);

        mMinScrollHandleHeight = fortyEightDp;

        final int eightDp = DisplayUtils.convertDpToPx(getContext(), 8);
        mHiddenTranslationX = (showLeft ? -1 : 1) * eightDp;
        mHide = () -> {
            if (!mHandle.isPressed()) {
                if (mAnimator != null && mAnimator.isStarted()) {
                    mAnimator.cancel();
                }
                mAnimator = new AnimatorSet();
                ObjectAnimator animator2 = ObjectAnimator.ofFloat(WebViewFastScroller.this, View.TRANSLATION_X,
                        mHiddenTranslationX);
                animator2.setInterpolator(new FastOutLinearInInterpolator());
                animator2.setDuration(150);
                mHandle.setEnabled(false);
                mAnimator.play(animator2);
                mAnimator.start();
            }
        };

        mHandle.setOnTouchListener(new OnTouchListener() {
            private float mInitialBarHeight;
            private float mLastPressedYAdjustedToInitial;

            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                MotionEvent event = MotionEvent.obtain(ev);
                int action = event.getActionMasked();
                event.offsetLocation(0, -mAppBarLayoutOffset);
                if (mOnTouchListener != null) {
                    mOnTouchListener.onTouch(v, event);
                }
                if (action == MotionEvent.ACTION_DOWN) {
                    mHandle.setPressed(true);

                    //mRecyclerView.stopScroll();

                    int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;

                    mWebView.getWebView().startNestedScroll(nestedScrollAxis);

                    mInitialBarHeight = mBar.getHeight();
                    mLastPressedYAdjustedToInitial = event.getY() + mHandle.getY() + mBar.getY();
                } else if (action == MotionEvent.ACTION_MOVE) {
                    float newHandlePressedY = event.getY() + mHandle.getY();
                    int barHeight = mBar.getHeight();
                    float newHandlePressedYAdjustedToInitial =
                            newHandlePressedY + (mInitialBarHeight - barHeight) + mBar.getY();

                    float deltaPressedYFromLastAdjustedToInitial =
                            newHandlePressedYAdjustedToInitial - mLastPressedYAdjustedToInitial;

                    int dY = (int) ((deltaPressedYFromLastAdjustedToInitial / mInitialBarHeight) *
                            (mWebView.computeVerticalScrollRangeMethod() + (mAppBarLayout == null ? 0 : mAppBarLayout.getTotalScrollRange()))
                            * computeDeltaScale(event));

                    if (!AppData.touch_scrollbar_fixed_toolbar.get() && mCoordinatorLayout != null && mAppBarLayout != null) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
                        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
                        if (behavior != null) {
                            behavior.onNestedPreScroll(mCoordinatorLayout, mAppBarLayout,
                                    WebViewFastScroller.this, 0, dY, new int[2], ViewCompat.TYPE_TOUCH);
                        }
                    }

                    updateRvScroll(dY);

                    mLastPressedYAdjustedToInitial = newHandlePressedYAdjustedToInitial;
                } else if (action == MotionEvent.ACTION_UP) {
                    mLastPressedYAdjustedToInitial = -1;

                    mWebView.getWebView().stopNestedScroll();

                    mHandle.setPressed(false);
                    postAutoHide();
                }

                return true;
            }
        });

        setTranslationX(mHiddenTranslationX);
    }

    private float computeDeltaScale(MotionEvent e) {
        int width = mWebView.getWebView().getWidth();
        float scrollbarX = showLeft ? getX() : (getX() + getWidth());
        float scale = (Math.abs(width - scrollbarX + mWebView.getWebView().getX() - e.getRawX())) / width;
        if (scale < 0.1f) {
            scale = 0.1f;
        } else if (scale > 0.9f) {
            scale = 1.0f;
        }
        return scale;
    }

    @ColorInt
    public int getHandlePressedColor() {
        return mHandlePressedColor;
    }

    public void setHandlePressedColor(@ColorInt int colorPressed) {
        mHandlePressedColor = colorPressed;
        updateHandleColorsAndInset();
    }

    @ColorInt
    public int getHandleNormalColor() {
        return mHandleNormalColor;
    }

    public void setHandleNormalColor(@ColorInt int colorNormal) {
        mHandleNormalColor = colorNormal;
        updateHandleColorsAndInset();
    }

    @ColorInt
    public int getBarColor() {
        return mBarColor;
    }

    /**
     * @param scrollBarColor Scroll bar color. Alpha will be set to ~22% to match stock scrollbar.
     */
    public void setBarColor(@ColorInt int scrollBarColor) {
        mBarColor = scrollBarColor;
        updateBarColorAndInset();
    }

    public int getHideDelay() {
        return mHideDelay;
    }

    /**
     * @param hideDelay the delay in millis to hide the scrollbar
     */
    public void setHideDelay(int hideDelay) {
        mHideDelay = hideDelay;
    }

    public int getTouchTargetWidth() {
        return mTouchTargetWidth;
    }

    /**
     * @param touchTargetWidth In pixels, less than or equal to 48dp
     */
    public void setTouchTargetWidth(int touchTargetWidth) {
        mTouchTargetWidth = touchTargetWidth;

        int eightDp = DisplayUtils.convertDpToPx(getContext(), 8);
        mBarInset = mTouchTargetWidth - eightDp;

        int fortyEightDp = DisplayUtils.convertDpToPx(getContext(), 48);
        if (mTouchTargetWidth > fortyEightDp) {
            throw new RuntimeException("Touch target width cannot be larger than 48dp!");
        }

        mBar.setLayoutParams(new LayoutParams(touchTargetWidth, ViewGroup.LayoutParams.MATCH_PARENT, GravityCompat.END));
        mHandle.setLayoutParams(new LayoutParams(touchTargetWidth, ViewGroup.LayoutParams.MATCH_PARENT, GravityCompat.END));

        updateHandleColorsAndInset();
        updateBarColorAndInset();
    }

    public boolean isHidingEnabled() {
        return mHidingEnabled;
    }

    /**
     * @param hidingEnabled whether hiding is enabled
     */
    public void setHidingEnabled(boolean hidingEnabled) {
        mHidingEnabled = hidingEnabled;
        if (hidingEnabled) {
            postAutoHide();
        }
    }

    private void updateHandleColorsAndInset() {
        StateListDrawable drawable = new StateListDrawable();

        if (!showLeft) {
            drawable.addState(View.PRESSED_ENABLED_STATE_SET,
                    new InsetDrawable(new ColorDrawable(mHandlePressedColor), mBarInset, 0, 0, 0));
            drawable.addState(View.EMPTY_STATE_SET,
                    new InsetDrawable(new ColorDrawable(mHandleNormalColor), mBarInset, 0, 0, 0));
        } else {
            drawable.addState(View.PRESSED_ENABLED_STATE_SET,
                    new InsetDrawable(new ColorDrawable(mHandlePressedColor), 0, 0, mBarInset, 0));
            drawable.addState(View.EMPTY_STATE_SET,
                    new InsetDrawable(new ColorDrawable(mHandleNormalColor), 0, 0, mBarInset, 0));
        }
        mHandle.setBackground(drawable);
    }

    private void updateBarColorAndInset() {
        Drawable drawable;

        if (!showLeft) {
            drawable = new InsetDrawable(new ColorDrawable(mBarColor), mBarInset, 0, 0, 0);
        } else {
            drawable = new InsetDrawable(new ColorDrawable(mBarColor), 0, 0, mBarInset, 0);
        }
        drawable.setAlpha(22);
        mBar.setBackground(drawable);
    }

    public void attachWebView(CustomWebView webView) {
        mWebView = webView;
        mWebView.setScrollBarListener(scrollChangedListener);
        if (enabled)
            mWebView.getWebView().setVerticalScrollBarEnabled(false);
    }

    public void detachWebView() {
        if (mWebView != null) {
            mWebView.getView().removeCallbacks(mHide);
            mWebView.setScrollBarListener(null);
            mWebView.getWebView().setVerticalScrollBarEnabled(true);
            mWebView = null;
        }
    }

    private CustomWebView.OnScrollChangedListener scrollChangedListener = new CustomWebView.OnScrollChangedListener() {
        @Override
        public void onScrollChanged(int l, int t, int oldl, int oldt) {
            show(true);
        }
    };

    public void attachAppBarLayout(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout) {
        mCoordinatorLayout = coordinatorLayout;
        mAppBarLayout = appBarLayout;

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                show(true);

                MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();

                mAppBarLayoutOffset = -verticalOffset;

                setLayoutParams(layoutParams);
            }
        });
    }

    public void setOnHandleTouchListener(OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    /**
     * Show the fast scroller and hide after delay
     *
     * @param animate whether to animate showing the scroller
     */
    public void show(final boolean animate) {
        requestLayout();

        post(new Runnable() {
            @Override
            public void run() {
                if (mHideOverride) {
                    return;
                }

                mHandle.setEnabled(true);
                if (animate) {
                    if (!mAnimatingIn && getTranslationX() != 0) {
                        if (mAnimator != null && mAnimator.isStarted()) {
                            mAnimator.cancel();
                        }
                        mAnimator = new AnimatorSet();
                        ObjectAnimator animator = ObjectAnimator.ofFloat(WebViewFastScroller.this, View.TRANSLATION_X, 0);
                        animator.setInterpolator(new LinearOutSlowInInterpolator());
                        animator.setDuration(100);
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mAnimatingIn = false;
                            }
                        });
                        mAnimatingIn = true;
                        mAnimator.play(animator);
                        mAnimator.start();
                    }
                } else {
                    setTranslationX(0);
                }
                postAutoHide();
            }
        });
    }

    void postAutoHide() {
        if (mWebView != null && mHidingEnabled) {
            mWebView.getView().removeCallbacks(mHide);
            mWebView.getView().postDelayed(mHide, mHideDelay);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mWebView == null) return;

        int appBarScrollRange = mAppBarLayout == null ? 0 : mAppBarLayout.getTotalScrollRange();
        int scrollOffset = mWebView.computeVerticalScrollOffsetMethod() + mAppBarLayoutOffset;
        int verticalScrollRange = mWebView.computeVerticalScrollRangeMethod() + appBarScrollRange;

        int barHeight = mBar.getHeight();

        float ratio = (float) scrollOffset / (verticalScrollRange - barHeight);

        int calculatedHandleHeight = (int) ((float) barHeight / verticalScrollRange * barHeight);
        if (calculatedHandleHeight < mMinScrollHandleHeight) {
            calculatedHandleHeight = mMinScrollHandleHeight;
        }

        if (calculatedHandleHeight >= barHeight || !mWebView.isScrollable()) {
            setTranslationX(mHiddenTranslationX);
            mHideOverride = true;
            return;
        }

        mHideOverride = false;

        float y = ratio * (barHeight - calculatedHandleHeight) + mAppBarLayoutOffset - appBarScrollRange;

        mHandle.layout(mHandle.getLeft(), (int) y, mHandle.getRight(), (int) y + calculatedHandleHeight);
    }

    void updateRvScroll(int dY) {
        if (mWebView != null && mHandle != null) {
            try {
                mWebView.scrollBy(0, dY);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public boolean isShowLeft() {
        return showLeft;
    }

    @SuppressLint("RtlHardcoded")
    public void setShowLeft(boolean showLeft) {
        if (this.showLeft != showLeft) {
            this.showLeft = showLeft;
            updateBarColorAndInset();
            updateHandleColorsAndInset();
            mHiddenTranslationX *= -1;
            FrameLayout.LayoutParams params = (LayoutParams) getLayoutParams();
            if (showLeft) {
                params.gravity = Gravity.LEFT;
            } else {
                params.gravity = Gravity.RIGHT;
            }
        }
    }

    public void setScrollEnabled(boolean enable) {
        if (this.enabled != enable) {
            this.enabled = enable;
            if (enable) {
                setVisibility(VISIBLE);
                if (mWebView != null)
                    mWebView.getWebView().setVerticalScrollBarEnabled(false);
            } else {
                setVisibility(GONE);
                if (mWebView != null)
                    mWebView.getWebView().setVerticalScrollBarEnabled(true);
            }
        }
    }
}
