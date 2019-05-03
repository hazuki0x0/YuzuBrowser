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

package jp.hazuki.yuzubrowser.bookmark.repository;

import com.rejasupotaro.android.kvs.PrefsSchema;
import com.rejasupotaro.android.kvs.annotations.Key;
import com.rejasupotaro.android.kvs.annotations.Table;

import static jp.hazuki.yuzubrowser.ui.ConstantsKt.BROWSER_LOAD_URL_TAB_CURRENT;
import static jp.hazuki.yuzubrowser.ui.ConstantsKt.PREFERENCE_FILE_NAME;

@Table(name = PREFERENCE_FILE_NAME)
public abstract class BookmarkPrefSchema extends PrefsSchema {

    @Key(name = "touch_scrollbar_fixed_toolbar")
    boolean touchScrollbarFixedToolbar = false;

    @Key(name = "bookmark_simpleDisplay")
    boolean bookmarkSimpleDisplay = false;

    @Key(name = "open_bookmark_new_tab")
    boolean openBookmarkNewTab = true;

    @Key(name = "newtab_bookmark")
    int newtabBookmark = BROWSER_LOAD_URL_TAB_CURRENT;

    @Key(name = "open_bookmark_icon_action")
    int openBookmarkIconAction = 1;

    @Key(name = "bookmark_breadcrumbs")
    boolean bookmarkBreadcrumbs = true;

    @Key(name = "save_bookmark_folder")
    boolean saveBookmarkFolder = false;

    @Key(name = "save_bookmark_folder_id")
    long saveBookmarkFolderId = -1;

    @Key(name = "font_size_bookmark")
    int fontSizeBookmark = -1;
}
