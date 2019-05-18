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

package jp.hazuki.yuzubrowser.history.repository;

import com.rejasupotaro.android.kvs.PrefsSchema;
import com.rejasupotaro.android.kvs.annotations.Key;
import com.rejasupotaro.android.kvs.annotations.Table;

import static jp.hazuki.yuzubrowser.ui.ConstantsKt.BROWSER_LOAD_URL_TAB_CURRENT;
import static jp.hazuki.yuzubrowser.ui.ConstantsKt.PREFERENCE_FILE_NAME;

@Table(name = PREFERENCE_FILE_NAME)
public class HistoryPrefSchema extends PrefsSchema {

    @Key(name = "newtab_history")
    public int newtabHistory = BROWSER_LOAD_URL_TAB_CURRENT;

    @Key(name = "font_size_history")
    public int fontSizeHistory = -1;

    @Key(name = "history_max_day")
    public int historyMaxDay = 0;

    @Key(name = "history_max_count")
    public int historyMaxCount = 0;
}
