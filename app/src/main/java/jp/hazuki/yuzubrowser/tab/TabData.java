package jp.hazuki.yuzubrowser.tab;

import android.graphics.Bitmap;

import jp.hazuki.yuzubrowser.webkit.CustomWebView;
import jp.hazuki.yuzubrowser.webkit.TabType;

public class TabData {
    public TabData(CustomWebView web) {
        mWebView = web;
    }

    public boolean equals(CustomWebView web) {
        return mWebView.equals(web);
    }

    public final CustomWebView mWebView;
    public String mUrl;
    private String mOriginalUrl;
    public String mTitle;
    public int mProgress = -1;
    private int tabType;


    public int getTabType() {
        return tabType;
    }

    public void setTabType(@TabType int type) {
        tabType = type;
    }

    public String getOriginalUrl() {
        if (mOriginalUrl == null)
            return mUrl;
        return mOriginalUrl;
    }

    protected static final int STATE_LOADING = 0x001;
    protected static final int STATE_NAV_LOCK = 0x002;
    protected int mState;

    public boolean isInPageLoad() {
        return (mState & STATE_LOADING) != 0;
    }

    public void setInPageLoad(boolean b) {
        if (b)
            mState |= STATE_LOADING;
        else
            mState &= ~STATE_LOADING;
    }

    public boolean isNavLock() {
        return (mState & STATE_NAV_LOCK) != 0;
    }

    public void setNavLock(boolean b) {
        if (b)
            mState |= STATE_NAV_LOCK;
        else
            mState &= ~STATE_NAV_LOCK;
    }

    public void onPageStarted(String url, Bitmap favicon) {
        mState |= STATE_LOADING;
        mProgress = 0;
        mUrl = url;
        mOriginalUrl = url;
        mTitle = null;
    }

    public void onPageFinished(CustomWebView web, String url) {
        mState &= ~STATE_LOADING;//moved from onProgressChanged
        mUrl = web.getUrl();
        mOriginalUrl = web.getOriginalUrl();
        mTitle = web.getTitle();
    }

    public void onReceivedTitle(String title) {
        mTitle = title;
    }

    public void onStateChanged(TabData tabdata) {
        mProgress = tabdata.mProgress;
        mTitle = tabdata.mTitle;
        mUrl = tabdata.mUrl;
        mOriginalUrl = tabdata.mOriginalUrl;
        setInPageLoad(tabdata.isInPageLoad());
    }

    public void onProgressChanged(int newProgress) {
        mProgress = newProgress;
    }
}
