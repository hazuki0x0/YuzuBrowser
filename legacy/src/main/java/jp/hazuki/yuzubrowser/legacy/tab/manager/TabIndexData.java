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
import android.view.View;

import java.io.Serializable;

import jp.hazuki.yuzubrowser.webview.CustomWebView;

public class TabIndexData implements Serializable {
    private String mUrl;
    private String mOriginalUrl;
    private String mTitle;
    private int tabType;
    private long mId;
    private long parent;
    private Bitmap thumbnail;
    private boolean thumbnailUpdated;
    private boolean shotThumbnail;
    private boolean navLock;
    private boolean pinning;

    public TabIndexData() {
    }

    public TabIndexData(String url, String title, int tabType, long id, long parent) {
        mUrl = url;
        this.mTitle = title;
        this.tabType = tabType;
        this.mId = id;
        this.parent = parent;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getOriginalUrl() {
        return mOriginalUrl != null ? mOriginalUrl : mUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getTabType() {
        return tabType;
    }

    public long getId() {
        return mId;
    }

    public long getParent() {
        return parent;
    }

    void setTabType(int tabType) {
        this.tabType = tabType;
    }

    void setId(long id) {
        this.mId = id;
    }

    void setParent(long parent) {
        this.parent = parent;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public void setOriginalUrl(String url) {
        mOriginalUrl = url;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public MainTabData getMainTabData(CustomWebView webView, View view) {
        webView.setIdentityId(mId);

        return new MainTabData(webView, view, this);
    }

    public TabData getTabData(CustomWebView webView) {
        webView.setIdentityId(mId);

        return new TabData(webView, this);
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isThumbnailUpdated() {
        return thumbnailUpdated;
    }

    void setThumbnailUpdated(boolean thumbnailUpdated) {
        this.thumbnailUpdated = thumbnailUpdated;
    }

    public boolean isShotThumbnail() {
        return shotThumbnail;
    }

    public void setShotThumbnail(boolean shotThumbnail) {
        this.shotThumbnail = shotThumbnail;
        if (shotThumbnail) {
            thumbnailUpdated = true;
        }
    }

    public boolean isNavLock() {
        return navLock;
    }

    public void setNavLock(boolean navLock) {
        this.navLock = navLock;
    }

    public boolean isPinning() {
        return pinning;
    }

    public void setPinning(boolean pinning) {
        this.pinning = pinning;
    }
}
