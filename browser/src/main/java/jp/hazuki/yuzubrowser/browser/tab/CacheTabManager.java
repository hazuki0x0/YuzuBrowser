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

package jp.hazuki.yuzubrowser.browser.tab;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.core.content.res.ResourcesCompat;
import jp.hazuki.yuzubrowser.browser.BrowserActivity;
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.favicon.FaviconManager;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabCache;
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabFaviconManager;
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabIndexData;
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabManager;
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabStorage;
import jp.hazuki.yuzubrowser.legacy.tab.manager.ThumbnailManager;
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs;
import jp.hazuki.yuzubrowser.ui.theme.ThemeData;
import jp.hazuki.yuzubrowser.webview.CustomWebView;
import jp.hazuki.yuzubrowser.webview.WebViewFactory;

public class CacheTabManager implements TabManager, TabCache.OnCacheOverFlowListener<MainTabData> {
    private int mCurrentNo = -1;
    private long currentId = 0;
    private boolean cleared = false;

    private BrowserActivity mWebBrowser;
    private final TabCache<MainTabData> mTabCache;
    private final TabStorage mTabStorage;
    private final FaviconManager faviconManager;
    private final ThumbnailManager thumbnailManager;
    private final TabFaviconManager tabFaviconManager;

    private List<View> mTabView;

    CacheTabManager(BrowserActivity activity, WebViewFactory factory, FaviconManager faviconManager) {
        mWebBrowser = activity;
        mTabCache = new TabCache<>(AppPrefs.tabs_cache_number.get(), this);
        mTabStorage = new TabStorage(activity, factory);
        this.faviconManager = faviconManager;
        mTabView = new ArrayList<>();
        thumbnailManager = new ThumbnailManager(activity);
        tabFaviconManager = new TabFaviconManager(activity, faviconManager);
        mTabStorage.setOnWebViewCreatedListener(tab -> {
            synchronized (mTabCache) {
                mTabCache.put(tab.getId(), tab);
            }
        });
    }

    @Override
    public MainTabData add(CustomWebView web, View view) {
        MainTabData tabData = new MainTabData(web, view);
        mTabView.add(view);
        mTabStorage.addIndexData(tabData.getTabIndexData());
        synchronized (mTabCache) {
            mTabCache.put(tabData.getId(), tabData);
        }
        return tabData;
    }

    @Override
    public void addTab(int index, MainTabData tabData) {
        mTabView.add(index, tabData.getTabView());
        mTabStorage.addIndexData(index, tabData.getTabIndexData());
        synchronized (mTabCache) {
            mTabCache.put(tabData.getId(), tabData);
        }
        if (mCurrentNo >= index) {
            mCurrentNo++;
        }
    }

    @Override
    public void setCurrentTab(int no) {
        mCurrentNo = no;
        if (no >= 0 && no < mTabStorage.size()) {
            TabIndexData data = mTabStorage.getIndexData(no);
            MainTabData tabData;
            synchronized (mTabCache) {
                tabData = mTabCache.get(data.getId());
            }
            if (tabData == null) {
                getTabData(data, no);
            }
            currentId = data.getId();
        }
    }

    @Override
    public void remove(int no) {
        if (no == mCurrentNo)
            throw new IllegalArgumentException("Remove tab is current tab");

        TabIndexData data = mTabStorage.removeAndDelete(no);
        mTabView.remove(no);
        synchronized (mTabCache) {
            mTabCache.remove(data.getId());
        }
        if (mCurrentNo > no) {
            mCurrentNo -= 1;
        }
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
        if (mCurrentNo >= mTabStorage.size()) {
            mCurrentNo = mTabStorage.indexOf(currentId);
        }
        return get(mCurrentNo);
    }

    @Override
    public MainTabData get(int no) {
        if (no == mTabStorage.size()) no -= 1;
        if (no < 0 || no >= mTabStorage.size()) return null;
        TabIndexData tabIndexData = mTabStorage.getIndexData(no);
        MainTabData tabData;
        synchronized (mTabCache) {
            tabData = mTabCache.get(tabIndexData.getId());
        }
        if (tabData == null) {
            tabData = getTabData(tabIndexData, no);
        }
        return tabData;
    }

    @Override
    public MainTabData get(CustomWebView web) {
        synchronized (mTabCache) {
            for (MainTabData tabData : mTabCache.values()) {
                if (tabData.mWebView == web) return tabData;
            }
        }
        int index = mTabStorage.indexOf(web.getIdentityId());
        if (index < 0) return null;
        return getTabData(mTabStorage.getIndexData(index), index);
    }

    public TabIndexData getIndexData(int no) {
        return mTabStorage.getIndexData(no);
    }

    @Override
    public TabIndexData getIndexData(long id) {
        int index = mTabStorage.indexOf(id);
        if (index > -1) {
            return mTabStorage.getIndexData(index);
        } else {
            return null;
        }
    }

    @Override
    public int searchParentTabNo(long id) {
        return mTabStorage.searchParentTabNo(id);
    }

    @Override
    public void destroy() {
        synchronized (mTabCache) {
            for (MainTabData data : mTabCache.values()) {
                data.mWebView.setEmbeddedTitleBarMethod(null);
                data.mWebView.destroy();
            }
        }
        thumbnailManager.destroy();
    }

    @Override
    public void saveData() {
        if (!cleared) {
            synchronized (mTabCache) {
                for (MainTabData tabData : mTabCache.values()) {
                    mTabStorage.saveWebView(tabData);
                }
            }
            mTabStorage.saveIndexData();
            mTabStorage.saveCurrentTab(mCurrentNo);
        }
    }

    @Override
    public void loadData() {
        List<TabIndexData> list = mTabStorage.getTabIndexDataList();
        for (TabIndexData data : list) {
            View v = mWebBrowser.getToolbarManager().addNewTabView();
            moveTabToBackground(v, mWebBrowser.getResources(), mWebBrowser.getTheme());
            mTabView.add(v);
            setText(v, data);
            setIcon(v, data);
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

        TextView textView = v.findViewById(R.id.textView);
        if (themedata != null && themedata.tabTextColorNormal != 0)
            textView.setTextColor(themedata.tabTextColorNormal);
        else
            textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_normal, theme));
    }

    @Override
    public void clear() {
        mTabStorage.clear();
        cleared = true;
    }

    @Override
    public void clearExceptPinnedTab() {
        mTabStorage.clearExceptPinnedTab((index, id) -> {
            synchronized (mTabCache) {
                mTabCache.remove(id);
            }
        });
    }

    @Override
    public void onPreferenceReset() {
        synchronized (mTabCache) {
            mTabCache.setSize(AppPrefs.tabs_cache_number.get());
        }
        onLayoutCreated();
    }

    @Override
    public void onLayoutCreated() {
        tabFaviconManager.onPreferenceReset(mTabView, mTabStorage.getTabIndexDataList());
    }

    @Override
    public List<MainTabData> getLoadedData() {
        synchronized (mTabCache) {
            return new ArrayList<>(mTabCache.values());
        }
    }

    @Override
    public List<TabIndexData> getIndexDataList() {
        return mTabStorage.getTabIndexDataList();
    }

    @Override
    public void takeThumbnailIfNeeded(MainTabData data) {
        thumbnailManager.takeThumbnailIfNeeded(data);
    }

    @Override
    public void removeThumbnailCache(String url) {
        thumbnailManager.removeThumbnailCache(url);
    }

    @Override
    public void forceTakeThumbnail(MainTabData data) {
        thumbnailManager.forceTakeThumbnail(data);
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

    private void setIcon(View view, TabIndexData indexData) {
        if (indexData.getOriginalUrl() == null || indexData.getOriginalUrl().startsWith("yuzu:")) {
            return;
        }

        Bitmap icon = faviconManager.get(indexData.getOriginalUrl());
        Drawable drawable;
        if (icon != null) {
            drawable = new BitmapDrawable(view.getResources(), icon);
        } else {
            drawable = view.getContext().getDrawable(R.drawable.ic_page_white_24px);
        }
        TextView titleTextView = view.findViewById(R.id.textView);
        int size = titleTextView.getHeight() - titleTextView.getPaddingTop() - titleTextView.getPaddingBottom();

        assert drawable != null;
        drawable.setBounds(0, 0, size, size);
        titleTextView.setCompoundDrawables(drawable, null, null, null);
    }

    private MainTabData getTabData(TabIndexData tabIndexData, int no) {
        MainTabData tabData = mTabStorage.loadWebView(mWebBrowser, tabIndexData, mTabView.get(no));
        synchronized (mTabCache) {
            mTabCache.put(tabIndexData.getId(), tabData);
        }
        if (ThemeData.isEnabled())
            tabData.onMoveTabToBackground(mWebBrowser.getResources(), mWebBrowser.getTheme());
        return tabData;
    }

    @Override
    public void onCacheOverflow(MainTabData tabData) {
        mTabStorage.saveWebView(tabData);
        mTabStorage.saveIndexData();
        tabData.mWebView.setEmbeddedTitleBarMethod(null);
        tabData.mWebView.destroy();
        int index = mTabStorage.indexOf(tabData.getId());
        tabFaviconManager.setFavicon(mTabView.get(index), mTabStorage.getIndexData(index));
    }
}
