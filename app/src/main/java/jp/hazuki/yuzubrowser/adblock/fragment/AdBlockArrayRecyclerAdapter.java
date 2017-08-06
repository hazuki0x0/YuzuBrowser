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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
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

    private final DateFormat dateFormat;
    private final Drawable foregroundOverlay;

    public AdBlockArrayRecyclerAdapter(Context context, List<AdBlock> list, OnRecyclerListener listener) {
        super(context, list, listener);
        dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
        foregroundOverlay = new ColorDrawable(ResourcesCompat.getColor(
                context.getResources(), R.color.selected_overlay, context.getTheme()));
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

        if (isMultiSelectMode() && isSelected(position)) {
            holder.foreground.setBackground(foregroundOverlay);
        } else {
            holder.foreground.setBackground(null);
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
        View foreground;

        ItemHolder(View itemView, AdBlockArrayRecyclerAdapter adapter) {
            super(itemView, adapter);

            match = itemView.findViewById(R.id.matchTextView);
            count = itemView.findViewById(R.id.countTextView);
            time = itemView.findViewById(R.id.timeTextView);
            checkBox = itemView.findViewById(R.id.checkBox);
            foreground = itemView.findViewById(R.id.foreground);
        }
    }
}
