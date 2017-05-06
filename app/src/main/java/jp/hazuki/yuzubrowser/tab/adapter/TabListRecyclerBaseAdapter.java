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

package jp.hazuki.yuzubrowser.tab.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.tab.manager.TabIndexData;
import jp.hazuki.yuzubrowser.tab.manager.TabManager;

public abstract class TabListRecyclerBaseAdapter extends RecyclerView.Adapter<TabListRecyclerBaseAdapter.ViewHolder> {
    private static final PorterDuffColorFilter IMAGE_FILTER = new PorterDuffColorFilter(0x64FFFFFF, PorterDuff.Mode.SRC_ATOP);

    private LayoutInflater mInflater;
    private TabManager tabManager;
    private OnRecyclerListener mListener;

    TabListRecyclerBaseAdapter(Context context, TabManager list, OnRecyclerListener listener) {
        mInflater = LayoutInflater.from(context);
        tabManager = list;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateViewHolder(mInflater, parent, viewType);
    }

    abstract ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // データ表示
        TabIndexData indexData = getItem(holder.getAdapterPosition());
        if (indexData != null) {
            Bitmap thumbNail = indexData.getThumbnail();
            if (thumbNail != null) {
                holder.thumbNail.setImageBitmap(thumbNail);
            } else {
                holder.thumbNail.setImageResource(R.drawable.empty_thumbnail);
            }
            holder.title.setText(indexData.getTitle());
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

        onBindViewHolder(holder, indexData);

    }

    abstract void onBindViewHolder(ViewHolder holder, TabIndexData indexData);

    @Override
    public int getItemCount() {
        return tabManager.size();
    }

    public TabIndexData getItem(int pos) {
        return tabManager.getIndexData(pos);
    }

    protected TabManager getTabManager() {
        return tabManager;
    }

    public interface OnRecyclerListener {
        void onRecyclerItemClicked(View v, int position);

        void onCloseButtonClicked(View v, int position);

        void onHistoryButtonClicked(View v, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

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

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            thumbNail.setColorFilter(IMAGE_FILTER);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            thumbNail.setColorFilter(null);
                            break;
                    }
                    return false;
                }
            });
        }

        public CharSequence getTitle() {
            if (TextUtils.isEmpty(title.getText()))
                return url.getText();
            else
                return title.getText();
        }
    }
}
