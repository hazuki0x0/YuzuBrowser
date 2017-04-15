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

package jp.hazuki.yuzubrowser.tab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.tab.manager.TabIndexData;
import jp.hazuki.yuzubrowser.tab.manager.TabManager;

public class TabListRecyclerAdapter extends RecyclerView.Adapter<TabListRecyclerAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private TabManager tabManager;
    private OnRecyclerListener mListener;
    private boolean horizontal;

    public TabListRecyclerAdapter(Context context, TabManager list, boolean isHorizontal, OnRecyclerListener listener) {
        mInflater = LayoutInflater.from(context);
        tabManager = list;
        mListener = listener;
        horizontal = isHorizontal;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (horizontal) {
            return new ViewHolder(mInflater.inflate(R.layout.tab_list_item_horizontal, parent, false));
        } else {
            return new ViewHolder(mInflater.inflate(R.layout.tab_list_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // データ表示
        TabIndexData indexData = getItem(position);
        if (indexData != null) {
            Bitmap thumbNail = tabManager.getThumbnail(indexData.getId());
            if (thumbNail != null) {
                holder.thumbNail.setImageBitmap(thumbNail);
            } else {
                holder.thumbNail.setImageResource(R.drawable.empty_thumbnail);
            }
            holder.title.setText(indexData.getTitle());
            holder.url.setText(indexData.getUrl());
        }

        // クリック処理
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRecyclerItemClicked(v, holder.getAdapterPosition());
            }
        });

        holder.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCloseButtonClicked(v, holder.getAdapterPosition());
            }
        });

        holder.historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onHistoryButtonClicked(v, holder.getAdapterPosition());
            }
        });
        if (horizontal) {
            if (position == tabManager.getCurrentTabNo())
                holder.thumbNail.setBackgroundColor(0xFF33B5E5);
            else
                holder.thumbNail.setBackgroundColor(Color.TRANSPARENT);
        } else {
            if (position == tabManager.getCurrentTabNo())
                holder.itemView.setBackgroundResource(R.drawable.tab_list_item_background_selected);
            else
                holder.itemView.setBackgroundResource(R.drawable.tab_list_item_background);
        }

    }

    @Override
    public int getItemCount() {
        return tabManager.size();
    }

    public TabIndexData getItem(int pos) {
        return tabManager.getIndexData(pos);
    }

    public interface OnRecyclerListener {
        void onRecyclerItemClicked(View v, int position);

        void onCloseButtonClicked(View v, int position);

        void onHistoryButtonClicked(View v, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbNail;
        TextView title;
        TextView url;
        View closeButton;
        View historyButton;

        public ViewHolder(View itemView) {
            super(itemView);
            thumbNail = (ImageView) itemView.findViewById(R.id.thumbNailImageView);
            title = (TextView) itemView.findViewById(R.id.titleTextView);
            url = (TextView) itemView.findViewById(R.id.urlTextView);
            closeButton = itemView.findViewById(R.id.closeImageButton);
            historyButton = itemView.findViewById(R.id.tabHistoryImageButton);
        }
    }
}
