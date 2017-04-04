/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.tab.manager;

import android.view.View;

import java.io.Serializable;

import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class TabIndexData implements Serializable {
    public String mUrl;
    public String mTitle;
    private int tabType;
    private long mId;
    private long parent;

    public TabIndexData() {
    }

    public TabIndexData(String mUrl, String mTitle, int tabType, long id, long parent) {
        this.mUrl = mUrl;
        this.mTitle = mTitle;
        this.tabType = tabType;
        this.mId = id;
        this.parent = parent;
    }

    public String getUrl() {
        return mUrl;
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

    void setId(long mId) {
        this.mId = mId;
    }

    void setParent(long parent) {
        this.parent = parent;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public MainTabData getMainTabData(CustomWebView webView, View view) {
        webView.setIdentityId(mId);

        MainTabData tabData = new MainTabData(webView, view, this);
        tabData.setTabType(tabType);
        tabData.setParent(parent);

        return tabData;
    }
}
