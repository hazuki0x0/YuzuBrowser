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

import java.util.List;

import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public interface TabManager {

    public MainTabData add(CustomWebView web, View view);

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

    int searchParentTabNo(long id);

    void destroy();

    void clearCache(boolean includeDiskFiles);

    void saveData();

    void loadData();

    void clear();

    void onPreferenceReset();

    List<MainTabData> getLoadedData();

    List<TabIndexData> getIndexDataList();
}
