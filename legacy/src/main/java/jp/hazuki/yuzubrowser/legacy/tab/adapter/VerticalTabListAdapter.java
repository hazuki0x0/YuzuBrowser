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

package jp.hazuki.yuzubrowser.legacy.tab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabIndexData;
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabManager;
import jp.hazuki.yuzubrowser.ui.extensions.UrlExtensionsKt;

class VerticalTabListAdapter extends TabListRecyclerBaseAdapter {
    VerticalTabListAdapter(Context context, TabManager list, OnRecyclerListener listener) {
        super(context, list, listener);
    }

    @Override
    ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.tab_list_item, parent, false), this);
    }

    @Override
    void onBindViewHolder(ViewHolder holder, TabIndexData indexData) {
        holder.url.setText(UrlExtensionsKt.decodePunyCodeUrl(indexData.getUrl()));

        if (holder.getAdapterPosition() == getTabManager().getCurrentTabNo())
            holder.itemView.setBackgroundResource(R.drawable.tab_list_item_background_selected);
        else
            holder.itemView.setBackgroundResource(R.drawable.tab_list_item_background_normal);
    }
}
