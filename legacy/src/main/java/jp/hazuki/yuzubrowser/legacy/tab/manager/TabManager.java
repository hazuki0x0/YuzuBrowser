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

import android.view.View;

import java.util.List;

import jp.hazuki.yuzubrowser.webview.CustomWebView;

public interface TabManager {

    MainTabData add(CustomWebView web, View view);

    void addTab(int index, MainTabData tabData);

    void setCurrentTab(int no);

    void remove(int no);

    int move(int from, int to);

    int indexOf(long id);

    int size();

    boolean isEmpty();

    boolean isFirst();

    boolean isLast();

    boolean isFirst(int no);

    boolean isLast(int no);

    int getLastTabNo();

    int getCurrentTabNo();

    void swap(int a, int b);

    MainTabData getCurrentTabData();

    MainTabData get(int no);

    MainTabData get(CustomWebView web);

    TabIndexData getIndexData(int no);

    TabIndexData getIndexData(long id);

    int searchParentTabNo(long id);

    void destroy();

    void saveData();

    void loadData();

    void clear();

    void clearExceptPinnedTab();

    void onPreferenceReset();

    void onLayoutCreated();

    List<MainTabData> getLoadedData();

    List<TabIndexData> getIndexDataList();

    void takeThumbnailIfNeeded(MainTabData data);

    void removeThumbnailCache(String url);

    void forceTakeThumbnail(MainTabData data);
}
