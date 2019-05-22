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

package jp.hazuki.yuzubrowser.search.repository;

import com.rejasupotaro.android.kvs.annotations.Key;
import com.rejasupotaro.android.kvs.annotations.Table;

import static jp.hazuki.yuzubrowser.ui.ConstantsKt.PREFERENCE_FILE_NAME;

@Table(name = PREFERENCE_FILE_NAME)
public class SearchPrefsSchema {

    @Key(name = "search_suggest_histories")
    public boolean searchSuggestHistories = true;

    @Key(name = "search_suggest_bookmarks")
    public boolean searchSuggestBookmarks = true;

    @Key(name = "private_mode")
    public boolean isPrivateMode = false;

    @Key(name = "search_suggest")
    public int searchSuggestType = 0;

    @Key(name = "search_url")
    public String searchUrl = "http://www.google.com/m?q=%s";

    @Key(name = "search_url_show_icon")
    public boolean searchUrlShowIcon = true;

    @Key(name = "search_url_save_switching")
    public boolean searchUrlSaveSwitching = false;
}
