/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.tab.adapter;

import android.content.Context;

import jp.hazuki.yuzubrowser.legacy.tab.manager.TabManager;

public class TabListRecyclerAdapterFactory {
    public static TabListRecyclerBaseAdapter create(Context context, TabManager list, boolean isHorizontal, TabListRecyclerBaseAdapter.OnRecyclerListener listener) {
        if (isHorizontal)
            return new HorizontalTabListAdapter(context, list, listener);
        else
            return new VerticalTabListAdapter(context, list, listener);
    }
}
