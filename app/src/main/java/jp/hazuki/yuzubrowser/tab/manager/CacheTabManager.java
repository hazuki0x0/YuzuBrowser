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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.hazuki.yuzubrowser.BrowserActivity;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.data.ThemeData;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

class CacheTabManager implements TabManager, TabCache.OnCacheOverFlowListener, ThumbnailManager.OnThumbnailListener {
    private int mCurrentNo = -1;

    private BrowserActivity mWebBrowser;
    private TabCache mTabCache;
    private TabStorage mTabStorage;
    private ThumbnailManager thumbnailManager;

    private List<View> mTabView;

    CacheTabManager(BrowserActivity activity) {
        mWebBrowser = activity;
        mTabCache = new TabCache(AppData.tabs_cache_number.get(), this);
        mTabStorage = new TabStorage(activity);
        mTabView = new ArrayList<>();
        thumbnailManager = new ThumbnailManager(activity, this);
    }

    @Override
    public MainTabData add(CustomWebView web, View view) {
        MainTabData tabData = new MainTabData(web, view);
        mTabView.add(view);
        mTabStorage.addIndexData(tabData.getTabIndexData());
        mTabCache.put(tabData.getId(), tabData);
        return tabData;
    }

    @Override
    public void setCurrentTab(int no) {
        mCurrentNo = no;
        if (no >= 0 && no < mTabStorage.size()) {
            TabIndexData data = mTabStorage.getIndexData(no);
            MainTabData tabData = mTabCache.get(data.getId());
            if (tabData == null) {
                getTabData(data, no);
            }
        }
    }

    @Override
    public void remove(int no) {
        TabIndexData data = mTabStorage.removeAndDelete(no);
        mTabView.remove(no);
        thumbnailManager.removeThumbnailCache(data.getId());
        if (mTabCache.containsKey(data.getId()))
            mTabCache.remove(data.getId());
    }

    @Override
    public int move(int from, int to) {
        mTabStorage.move(from, to);
        ArrayUtils.move(mTabView, from, to);

        if (from == mCurrentNo) {
            return mCurrentNo = to;
        } else {
            if (from <= mCurrentNo && to >= mCurrentNo) {
                return --mCurrentNo;
            } else if (from >= mCurrentNo && to <= mCurrentNo) {
                return ++mCurrentNo;
            }
        }
        return mCurrentNo;
    }

    @Override
    public int indexOf(long id) {
        return mTabStorage.indexOf(id);
    }

    @Override
    public int size() {
        return mTabStorage.size();
    }

    @Override
    public boolean isEmpty() {
        return mTabStorage.size() == 0;
    }

    @Override
    public boolean isFirst() {
        return mCurrentNo == 0;
    }

    @Override
    public boolean isLast() {
        return mCurrentNo == mTabStorage.size() - 1;
    }

    @Override
    public boolean isFirst(int no) {
        return no == 0;
    }

    @Override
    public boolean isLast(int no) {
        return no == mTabStorage.size() - 1;
    }

    @Override
    public int getLastTabNo() {
        return mTabStorage.size() - 1;
    }

    @Override
    public int getCurrentTabNo() {
        return mCurrentNo;
    }

    @Override
    public void swap(int a, int b) {
        mTabStorage.swap(a, b);
        Collections.swap(mTabView, a, b);
    }

    @Override
    public MainTabData getCurrentTabData() {
        return get(mCurrentNo);
    }

    @Override
    public MainTabData get(int no) {
        if (no < 0 || no >= mTabStorage.size()) return null;
        TabIndexData tabIndexData = mTabStorage.getIndexData(no);
        MainTabData tabData = mTabCache.get(tabIndexData.getId());
        if (tabData == null) {
            tabData = getTabData(tabIndexData, no);
        }
        return tabData;
    }

    @Override
    public MainTabData get(CustomWebView web) {
        for (MainTabData tabData : mTabCache.values()) {
            if (tabData.mWebView == web) return tabData;
        }
        int index = mTabStorage.indexOf(web.getIdentityId());
        if (index < 0) return null;
        return getTabData(mTabStorage.getIndexData(index), index);
    }

    public TabIndexData getIndexData(int no) {
        return mTabStorage.getIndexData(no);
    }

    @Override
    public int searchParentTabNo(long id) {
        return mTabStorage.searchParentTabNo(id);
    }

    @Override
    public void destroy() {
        thumbnailManager.destroy();
        for (MainTabData data : mTabCache.values()) {
            data.mWebView.setEmbeddedTitleBarMethod(null);
            data.mWebView.destroy();
        }
    }

    @Override
    public void clearCache(boolean includeDiskFiles) {
        for (MainTabData data : mTabCache.values()) {
            data.mWebView.clearCache(includeDiskFiles);
        }
    }

    @Override
    public void saveData() {
        for (MainTabData tabData : mTabCache.values()) {
            mTabStorage.saveWebView(tabData);
        }
        mTabStorage.saveIndexData();
        mTabStorage.saveCurrentTab(mCurrentNo);
    }

    @Override
    public void loadData() {
        List<TabIndexData> list = mTabStorage.getTabIndexDataList();
        for (TabIndexData data : list) {
            View v = mWebBrowser.getToolbar().addNewTabView();
            moveTabToBackground(v, mWebBrowser.getResources(), mWebBrowser.getTheme());
            mTabView.add(v);
            setText(v, data);
        }
        mCurrentNo = mTabStorage.loadCurrentTab();

        if (mCurrentNo >= list.size()) {
            mCurrentNo = list.size() - 1;
        }
    }

    private void moveTabToBackground(View v, Resources res, Resources.Theme theme) {
        ThemeData themedata = ThemeData.getInstance();
        if (themedata != null && themedata.tabBackgroundNormal != null)
            v.setBackground(themedata.tabBackgroundNormal);
        else
            v.setBackgroundResource(R.drawable.tab_background_normal);

        TextView textView = (TextView) v.findViewById(R.id.textView);
        if (themedata != null && themedata.tabTextColorNormal != 0)
            textView.setTextColor(themedata.tabTextColorNormal);
        else
            textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_normal, theme));
    }

    @Override
    public void clear() {
        mTabStorage.clear();
    }

    @Override
    public void onPreferenceReset() {
        mTabCache.setSize(AppData.tabs_cache_number.get());
    }

    @Override
    public List<MainTabData> getLoadedData() {
        return new ArrayList<>(mTabCache.values());
    }

    @Override
    public List<TabIndexData> getIndexDataList() {
        return mTabStorage.getTabIndexDataList();
    }

    @Override
    public void onStartPage(CustomWebView webView) {
        thumbnailManager.onStartPage(webView.getIdentityId());
    }

    @Override
    public void takeThumbnailIfNeeded(CustomWebView webView) {
        thumbnailManager.takeThumbnailIfNeeded(webView);
    }

    @Override
    public Bitmap getThumbnail(long id) {
        return thumbnailManager.getThumbnail(id);
    }

    private void setText(View view, TabIndexData indexData) {
        String text;
        if (indexData.getTitle() != null) {
            text = indexData.getTitle();
        } else {
            text = indexData.getUrl();
        }
        ((TextView) view.findViewById(R.id.textView)).setText(text);
    }

    private MainTabData getTabData(TabIndexData tabIndexData, int no) {
        CustomWebView webView = mTabStorage.loadWebView(mWebBrowser, tabIndexData);
        MainTabData tabData = tabIndexData.getMainTabData(webView, mTabView.get(no));
        mTabCache.put(tabIndexData.getId(), tabData);
        if (ThemeData.isEnabled())
            tabData.onMoveTabToBackground(mWebBrowser.getResources(), mWebBrowser.getTheme());
        return tabData;
    }

    @Override
    public void onCacheOverflow(MainTabData tabData) {
        mTabStorage.saveWebView(tabData);
        tabData.mWebView.setEmbeddedTitleBarMethod(null);
        tabData.mWebView.destroy();
    }

    @Override
    public void onSaveThumbnail(long id, byte[] image) {
        mTabStorage.saveThumbnail(id, image);
    }

    @Override
    public byte[] onLoadThumbnail(long id) {
        return mTabStorage.getThumbnail(id);
    }
}
