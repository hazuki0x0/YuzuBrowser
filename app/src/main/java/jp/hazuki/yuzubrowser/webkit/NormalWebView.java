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

package jp.hazuki.yuzubrowser.webkit;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;

import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector;

public class NormalWebView extends WebView implements CustomWebView, NestedScrollingChild {
    private long id = System.currentTimeMillis();

    private MultiTouchGestureDetector mGestureDetector;
    private OnScrollChangedListener mOnScrollChangedListener;
    private OnScrollChangedListener mScrollBarListener;
    private OnSwipeableChangeListener scrollableChangeListener;
    private boolean swipeable = true;
    private View mTitleBar;

    private int firstY;
    private int mLastY;
    private int scrollY;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedOffsetY;
    private NestedScrollingChildHelper mChildHelper;
    private boolean firstScroll = true;
    private boolean doubleTapFling;

    private boolean scrollExcessPlay;
    private final int scrollSlop;
    private boolean touching = false;
    private boolean scrollable = false;

    public NormalWebView(Context context) {
        this(context, null);
    }

    public NormalWebView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    public NormalWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        scrollSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
    }

    @Override
    public CustomWebBackForwardList copyMyBackForwardList() {
        return new CustomWebBackForwardList(copyBackForwardList());
    }

    @Override
    public void setMyWebChromeClient(CustomWebChromeClient client) {
        setWebChromeClient(client);
    }

    @Override
    public void setMyWebViewClient(CustomWebViewClient client) {
        setWebViewClient(client);
    }

    @Override
    public void setOnMyCreateContextMenuListener(CustomOnCreateContextMenuListener webContextMenuListener) {
        setOnCreateContextMenuListener(webContextMenuListener);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public WebView getWebView() {
        return this;
    }

    @Override
    public void setSwipeEnable(boolean enable) {
        //do nothing
    }

    @Override
    public boolean getSwipeEnable() {
        return false;
    }

    @Override
    public void setGestureDetector(MultiTouchGestureDetector d) {
        mGestureDetector = d;
    }

    @Override
    public void setOnCustomWebViewStateChangeListener(OnWebStateChangeListener l) {
        //do nothing
    }

    @Override
    public boolean setEmbeddedTitleBarMethod(View view) {
        if (mTitleBar != view) {
            if (mTitleBar != null) {
                removeView(mTitleBar);
            }
            if (view != null) {
                if (view.getParent() != null) {
                    ((ViewGroup) view.getParent()).removeView(view);
                }
                addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0, 0));
                view.setTranslationX(getScrollX());//can move X
            }
            mTitleBar = view;
        }
        return true;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mScrollBarListener != null)
            mScrollBarListener.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null)
            mOnScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
        if (mTitleBar != null)
            mTitleBar.setTranslationX(l);//can move X
    }

    @Override
    public boolean notifyFindDialogDismissedMethod() {
        return false;
    }

    @Override
    public boolean setOverScrollModeMethod(int arg) {
        setOverScrollMode(arg);
        return true;
    }

    @TargetApi(9)
    @Override
    public int getOverScrollModeMethod() {
        return getOverScrollMode();
    }

    @Override
    public int computeVerticalScrollRangeMethod() {
        return computeVerticalScrollRange();
    }

    @Override
    public int computeVerticalScrollOffsetMethod() {
        return computeVerticalScrollOffset();
    }

    @Override
    public int computeVerticalScrollExtentMethod() {
        return computeVerticalScrollExtent();
    }

    @Override
    public int computeHorizontalScrollRangeMethod() {
        return computeHorizontalScrollRange();
    }

    @Override
    public int computeHorizontalScrollOffsetMethod() {
        return computeHorizontalScrollOffset();
    }

    @Override
    public int computeHorizontalScrollExtentMethod() {
        return computeHorizontalScrollExtent();
    }

    @Override
    public long getIdentityId() {
        return id;
    }

    @Override
    public void setIdentityId(long identityId) {
        if (id > identityId)
            id = identityId;
    }

    @Override
    public void resetTheme() {

    }

    @Override
    public void onPreferenceReset() {
    }

    @Override
    public void setAcceptThirdPartyCookies(CookieManager manager, boolean accept) {
        manager.setAcceptThirdPartyCookies(this, accept);
    }

    @Override
    public void setDoubleTapFling(boolean fling) {
        doubleTapFling = fling;
    }

    @Override
    public boolean isBackForwardListEmpty() {
        return copyBackForwardList().getSize() == 0;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (x < 0)
            x = 0;
        else if (x > computeHorizontalScrollRange() - computeHorizontalScrollExtent())
            x = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (y < 0)
            y = 0;
        else if (y > computeVerticalScrollRange() - computeVerticalScrollExtent())
            y = computeVerticalScrollRange() - computeVerticalScrollExtent();

        super.scrollTo(x, y);
    }

    @Override
    public void scrollBy(int x, int y) {
        scrollTo(x + getScrollX(), y + getScrollY());
    }

    @Override
    public boolean saveWebArchiveMethod(String filename) {
        saveWebArchive(filename);
        return true;
    }

    boolean nestedScrolled = false;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mGestureDetector != null && ev != null && mGestureDetector.onTouchEvent(ev)) {
            ev.setAction(MotionEvent.ACTION_CANCEL);
            super.onTouchEvent(ev);
            return true;
        }

        MotionEvent event = MotionEvent.obtain(ev);
        final int action = event.getActionMasked();

        if (doubleTapFling) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE)
                doubleTapFling = false;
            return super.onTouchEvent(ev);
        }

        boolean returnValue;
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0;
            touching = true;
        }
        int eventY = (int) event.getY();
        event.offsetLocation(0, mNestedOffsetY);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() != 1) {
                    returnValue = super.onTouchEvent(event);
                } else if (scrollExcessPlay && Math.abs(firstY - eventY) < scrollSlop
                        || scrollY != 0 && scrollY == getScrollY()) {
                    returnValue = super.onTouchEvent(ev);
                    mLastY = eventY;
                } else {
                    int deltaY = mLastY - eventY;
                    if (scrollExcessPlay) {
                        scrollExcessPlay = false;
                        // start NestedScroll
                        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    }

                    // NestedPreScroll
                    if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                        deltaY -= mScrollConsumed[1];
                        mLastY = eventY - mScrollOffset[1];
                        event.offsetLocation(0, -mScrollOffset[1]);
                        mNestedOffsetY = mScrollOffset[1];
                        setSwipeable(false);
                    } else {
                        setSwipeable(true);
                    }
                    returnValue = super.onTouchEvent(event);

                    // NestedScroll
                    if (dispatchNestedScroll(0, mScrollConsumed[1], 0, deltaY, mScrollOffset)) {
                        event.offsetLocation(0, mScrollOffset[1]);
                        mNestedOffsetY = mScrollOffset[1];
                        mLastY -= deltaY;
                        nestedScrolled = true;
                    } else {
                        nestedScrolled = false;
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                returnValue = super.onTouchEvent(ev);
                scrollExcessPlay = true;
                if (firstScroll) {
                    mLastY = eventY - 5;
                    firstScroll = false;
                } else {
                    mLastY = eventY;
                }
                firstY = eventY;
                scrollY = getScrollY();
                break;
            default:
                touching = false;
                returnValue = super.onTouchEvent(ev);
                // end NestedScroll
                stopNestedScroll();
                break;
        }
        return returnValue;
    }

    @Override
    public void destroy() {
        setDownloadListener(null);
        setEmbeddedTitleBarMethod(null);
        setGestureDetector(null);
        setMyWebChromeClient(null);
        setMyWebViewClient(null);
        setOnCustomWebViewStateChangeListener(null);
        setOnMyCreateContextMenuListener(null);
        setMyOnScrollChangedListener(null);

        post(NormalWebView.super::destroy);
    }

    @Override
    public void setMyOnScrollChangedListener(OnScrollChangedListener l) {
        mOnScrollChangedListener = l;
    }

    @Override
    public void setScrollBarListener(OnScrollChangedListener l) {
        mScrollBarListener = l;
    }

    // Nested Scroll implements

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                                        int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    public void setOnScrollableChangeListener(OnSwipeableChangeListener listener) {
        scrollableChangeListener = listener;
    }

    @Override
    public void setSwipeable(boolean enable) {
        if (swipeable != enable) {
            swipeable = enable;
            if (scrollableChangeListener != null) {
                scrollableChangeListener.onSwipeableChanged(scrollable && swipeable);
            }
        }
    }

    @Override
    public boolean isTouching() {
        return touching;
    }

    @Override
    protected int computeVerticalScrollRange() {
        int scrollRange = super.computeVerticalScrollRange();
        boolean old = scrollable;
        scrollable = scrollRange > getHeight();
        if (old != scrollable && scrollableChangeListener != null)
            scrollableChangeListener.onSwipeableChanged(scrollable && swipeable);
        return scrollRange;
    }

    @Override
    public boolean isScrollable() {
        return scrollable;
    }
}
