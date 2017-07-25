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

package jp.hazuki.yuzubrowser.adblock.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.adblock.AdBlock;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

public class AdBlockArrayRecyclerAdapter extends ArrayRecyclerAdapter<AdBlock, AdBlockArrayRecyclerAdapter.ItemHolder> {

    private final int normalBackGround;
    private final DateFormat dateFormat;

    public AdBlockArrayRecyclerAdapter(Context context, List<AdBlock> list, OnRecyclerListener listener) {
        super(context, list, listener);
        TypedArray a = context.obtainStyledAttributes(R.style.CustomThemeBlack, new int[]{android.R.attr.selectableItemBackground});
        normalBackGround = a.getResourceId(0, 0);
        a.recycle();
        dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, AdBlock item, int position) {
        holder.match.setText(item.getMatch());
        holder.count.setText(Integer.toString(item.getCount()));
        holder.checkBox.setChecked(item.isEnable());
        if (item.getTime() > 0) {
            holder.time.setText(dateFormat.format(new Date(item.getTime())));
        } else {
            holder.time.setText("");
        }

        if (isMultiSelectMode()) {
            setSelectedBackground(holder.itemView, isSelected(position));
        } else {
            holder.itemView.setBackgroundResource(normalBackGround);
        }
    }

    private void setSelectedBackground(View view, boolean selected) {
        if (selected) {
            view.setBackgroundResource(R.drawable.selectable_selected_item_background);
        } else {
            view.setBackgroundResource(normalBackGround);
        }
    }

    @Override
    protected ItemHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new ItemHolder(inflater.inflate(R.layout.fragment_ad_block_list_item, parent, false), this);
    }

    static class ItemHolder extends ArrayRecyclerAdapter.ArrayViewHolder<AdBlock> {

        TextView match;
        TextView count;
        TextView time;
        CheckBox checkBox;

        ItemHolder(View itemView, AdBlockArrayRecyclerAdapter adapter) {
            super(itemView, adapter);

            match = (TextView) itemView.findViewById(R.id.matchTextView);
            count = (TextView) itemView.findViewById(R.id.countTextView);
            time = (TextView) itemView.findViewById(R.id.timeTextView);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
        }
    }
}
