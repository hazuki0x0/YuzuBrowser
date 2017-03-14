package jp.hazuki.yuzubrowser.webkit;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector;

public class NormalWebView extends WebView implements CustomWebView, NestedScrollingChild {
    private long id = System.currentTimeMillis();

    private MultiTouchGestureDetector mGestureDetector;
    private OnScrollChangedListener mOnScrollChangedListener;
    private View mTitleBar;

    private int mLastY;
    private ScrollController mScrollController;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedOffsetY;
    private NestedScrollingChildHelper mChildHelper;
    private boolean firstScroll = true;
    private CustomWebViewClient webViewClient;

    public NormalWebView(Context context) {
        this(context, null);
    }

    public NormalWebView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    public NormalWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
        webViewClient = client;
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

        boolean returnValue;
        MotionEvent event = MotionEvent.obtain(ev);
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsetY = 0;
        }
        int eventY = (int) event.getY();
        event.offsetLocation(0, mNestedOffsetY);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int deltaY = mLastY - eventY;
                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1];
                    mLastY = eventY - mScrollOffset[1];
                    event.offsetLocation(0, -mScrollOffset[1]);
                    mNestedOffsetY = mScrollOffset[1];
                }
                returnValue = super.onTouchEvent(ev);

                // NestedScroll
                if (dispatchNestedScroll(0, mScrollConsumed[1], 0, deltaY, mScrollOffset)) {
                    event.offsetLocation(0, mScrollOffset[1]);
                    mNestedOffsetY = mScrollOffset[1];
                    mLastY -= deltaY;
                    nestedScrolled = true;
                } else {
                    nestedScrolled = false;
                }

                if (mScrollController != null) {
                    mScrollController.onMove(getScrollY());
                }
                break;
            case MotionEvent.ACTION_DOWN:
                returnValue = super.onTouchEvent(ev);
                if (firstScroll) {
                    mLastY = eventY - 5;
                    firstScroll = false;
                } else {
                    mLastY = eventY;
                }
                // start NestedScroll
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            case MotionEvent.ACTION_UP:
                /* no break */
            case MotionEvent.ACTION_CANCEL:
                returnValue = super.onTouchEvent(ev);
                // end NestedScroll
                stopNestedScroll();
                break;
            default:
                returnValue = super.onTouchEvent(ev);
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

        post(new Runnable() {
            @Override
            public void run() {
                NormalWebView.super.destroy();
            }
        });
    }

    @Override
    public void loadUrl(String url) {
        if (webViewClient == null || !webViewClient.shouldOverrideUrlLoading(this, url, Uri.parse(url)))
            super.loadUrl(url);
    }

    @Override
    public void setMyOnScrollChangedListener(OnScrollChangedListener l) {
        mOnScrollChangedListener = l;
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

    public void setScrollController(ScrollController controller) {
        mScrollController = controller;
    }
}
