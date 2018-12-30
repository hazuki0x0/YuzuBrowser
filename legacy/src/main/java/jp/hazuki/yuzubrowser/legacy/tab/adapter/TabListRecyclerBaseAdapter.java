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
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabIndexData;
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabManager;

public abstract class TabListRecyclerBaseAdapter extends RecyclerView.Adapter<TabListRecyclerBaseAdapter.ViewHolder> {
    private static final PorterDuffColorFilter IMAGE_FILTER = new PorterDuffColorFilter(0x64FFFFFF, PorterDuff.Mode.SRC_ATOP);

    private final Drawable closeIcon;
    private final Drawable pinIcon;

    private LayoutInflater mInflater;
    private TabManager tabManager;
    private OnRecyclerListener mListener;

    TabListRecyclerBaseAdapter(Context context, TabManager list, OnRecyclerListener listener) {
        mInflater = LayoutInflater.from(context);
        tabManager = list;
        mListener = listener;

        closeIcon = context.getDrawable(R.drawable.ic_close_black_24dp);
        pinIcon = context.getDrawable(R.drawable.ic_pin_24dp);
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
            holder.setIndexData(indexData);
            Bitmap thumbNail = indexData.getThumbnail();
            if (thumbNail != null) {
                holder.thumbNail.setImageBitmap(thumbNail);
            } else {
                holder.thumbNail.setImageResource(R.drawable.empty_thumbnail);
            }
            holder.title.setText(indexData.getTitle());
            if (indexData.isPinning()) {
                holder.closeButton.setImageDrawable(pinIcon);
                holder.closeButton.setEnabled(false);
            } else {
                holder.closeButton.setImageDrawable(closeIcon);
                holder.closeButton.setEnabled(true);
            }
        }

        onBindViewHolder(holder, indexData);

    }

    private void onItemClicked(View v, int position, TabIndexData data) {
        position = searchPosition(position, data);
        if (position < 0) return;
        mListener.onRecyclerItemClicked(v, position);
    }

    private void onCloseClicked(View v, int position, TabIndexData data) {
        position = searchPosition(position, data);
        if (position < 0) return;
        mListener.onCloseButtonClicked(v, position);
    }

    private void onHistoryClicked(View v, int position, TabIndexData data) {
        position = searchPosition(position, data);
        if (position < 0) return;
        mListener.onHistoryButtonClicked(v, position);
    }

    protected int searchPosition(int position, TabIndexData item) {
        if (position < 0 || position >= getItemCount() || !getItem(position).equals(item)) {
            if (position > 0 && getItem(position - 1).equals(item))
                return position - 1;

            position = tabManager.indexOf(item.getId());
            if (position < 0) notifyDataSetChanged();
            return position;
        }
        return position;
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

        private TabIndexData indexData;

        ImageView thumbNail;
        TextView title;
        TextView url;
        View disable;
        ImageButton closeButton;
        View historyButton;

        public ViewHolder(View itemView, final TabListRecyclerBaseAdapter adapter) {
            super(itemView);
            thumbNail = itemView.findViewById(R.id.thumbNailImageView);
            title = itemView.findViewById(R.id.titleTextView);
            url = itemView.findViewById(R.id.urlTextView);
            disable = itemView.findViewById(R.id.disable);
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onItemClicked(v, getAdapterPosition(), indexData);
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onCloseClicked(v, getAdapterPosition(), indexData);
                }
            });

            historyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onHistoryClicked(v, getAdapterPosition(), indexData);
                }
            });
        }

        public CharSequence getTitle() {
            if (TextUtils.isEmpty(title.getText()))
                return url.getText();
            else
                return title.getText();
        }

        public TabIndexData getIndexData() {
            return indexData;
        }

        public void setIndexData(TabIndexData indexData) {
            this.indexData = indexData;
        }
    }
}
