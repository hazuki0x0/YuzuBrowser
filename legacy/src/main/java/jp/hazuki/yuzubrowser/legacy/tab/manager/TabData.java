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

package jp.hazuki.yuzubrowser.legacy.tab.manager;

import android.graphics.Bitmap;

import jp.hazuki.yuzubrowser.core.android.utils.BitmapUtilsKt;
import jp.hazuki.yuzubrowser.favicon.FaviconManager;
import jp.hazuki.yuzubrowser.legacy.webkit.TabType;
import jp.hazuki.yuzubrowser.webview.CustomWebView;

public class TabData {
    protected static final int STATE_LOADING = 0x001;

    public final CustomWebView mWebView;
    private TabIndexData mIndexData;
    public int mProgress = -1;
    private int scrollRange;
    private int scrollXOffset;
    private boolean canRetry;
    private long thumbnailHash;

    protected int mState;

    public TabData(CustomWebView web) {
        this(web, new TabIndexData());
        mIndexData.setId(web.getIdentityId());
    }

    public TabData(CustomWebView web, TabIndexData data) {
        mWebView = web;
        mIndexData = data;
    }

    public boolean equals(CustomWebView web) {
        return mWebView.equals(web);
    }

    public int getTabType() {
        return mIndexData.getTabType();
    }

    public void setTabType(@TabType int type) {
        mIndexData.setTabType(type);
    }

    public long getParent() {
        return mIndexData.getParent();
    }

    public void setParent(long parent) {
        mIndexData.setParent(parent);
    }

    public String getOriginalUrl() {
        return mIndexData.getOriginalUrl();
    }

    public String getUrl() {
        return mIndexData.getUrl();
    }

    public void setUrl(String url) {
        mIndexData.setUrl(url);
    }

    public String getTitle() {
        return mIndexData.getTitle();
    }

    public void setTitle(String title) {
        mIndexData.setTitle(title);
    }

    public TabIndexData getTabIndexData() {
        return mIndexData;
    }

    public long getId() {
        return mIndexData.getId();
    }

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
        return mIndexData.isNavLock();
    }

    public void setNavLock(boolean b) {
        mIndexData.setNavLock(b);
    }

    public void onPageStarted(String url, Bitmap favicon) {
        mState |= STATE_LOADING;
        mProgress = 0;
        mIndexData.setUrl(url);
        mIndexData.setOriginalUrl(url);
        mIndexData.setTitle(null);
    }

    public void onPageFinished(CustomWebView web, String url) {
        mState &= ~STATE_LOADING;//moved from onProgressChanged
        mIndexData.setUrl(web.getUrl());
        mIndexData.setOriginalUrl(web.getOriginalUrl());
        mIndexData.setTitle(web.getTitle());
    }

    public void onReceivedTitle(String title) {
        mIndexData.setTitle(title);
    }

    public void onStateChanged(String title, String url, String originalUrl, int progress, Boolean isLoading, FaviconManager faviconManager) {
        mProgress = progress;
        mIndexData.setTitle(title);
        mIndexData.setUrl(url);
        mIndexData.setOriginalUrl(originalUrl);
        setInPageLoad(isLoading);
    }

    public void onProgressChanged(int newProgress) {
        mProgress = newProgress;
    }

    public int getProgress() {
        return mProgress;
    }

    public void onStartPage() {
        mIndexData.setShotThumbnail(false);
    }

    public boolean isShotThumbnail() {
        return mIndexData.isShotThumbnail();
    }

    public boolean isNeedShotThumbnail() {
        int sr = mWebView.computeVerticalScrollRangeMethod();
//        if (sr == 0)
//            return false;

        int old = scrollRange;
        scrollRange = sr;

        return old != scrollRange || !isShotThumbnail();
    }

    public void shotThumbnail(Bitmap thumbnail) {
        mIndexData.setThumbnail(thumbnail);
        mIndexData.setShotThumbnail(true);
        thumbnailHash = BitmapUtilsKt.calcImageVectorHash(thumbnail);
        canRetry = true;
    }

    public boolean needRetry() {
        return canRetry && thumbnailHash == 0;
    }

    public void setCanRetry(boolean canRetry) {
        this.canRetry = canRetry;
    }

    public void onDown() {
        scrollXOffset = mWebView.computeHorizontalScrollOffsetMethod();
    }

    public boolean isMoved() {
        return scrollXOffset != mWebView.computeHorizontalScrollOffsetMethod();
    }

    public boolean isPinning() {
        return mIndexData.isPinning();
    }

    public void setPinning(boolean pinning) {
        mIndexData.setPinning(pinning);
    }
}
