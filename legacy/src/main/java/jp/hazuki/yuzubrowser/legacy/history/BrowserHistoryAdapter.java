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

package jp.hazuki.yuzubrowser.legacy.history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter;
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration;
import jp.hazuki.yuzubrowser.core.utility.utils.FontUtils;
import jp.hazuki.yuzubrowser.core.utility.utils.UrlUtils;
import jp.hazuki.yuzubrowser.favicon.FaviconManager;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData;
import jp.hazuki.yuzubrowser.legacy.utils.ThemeUtils;
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener;

public class BrowserHistoryAdapter extends RecyclerView.Adapter<BrowserHistoryAdapter.HistoryHolder>
        implements StickyHeaderAdapter<BrowserHistoryAdapter.HeaderHolder> {

    private final PorterDuffColorFilter defaultColorFilter;
    private static final PorterDuffColorFilter faviconColorFilter = new PorterDuffColorFilter(0, PorterDuff.Mode.SRC_ATOP);

    private DateFormat dateFormat;
    private DateFormat timeFormat;
    private BrowserHistoryManager mManager;
    private FaviconManager faviconManager;
    private List<BrowserHistory> histories;
    private OnHistoryRecyclerListener mListener;
    private String mQuery;
    private LayoutInflater inflater;
    private StickyHeaderDecoration mDecoration;
    private Calendar calendar;
    private boolean pickMode;

    private boolean multiSelectMode;
    private SparseBooleanArray itemSelected = new SparseBooleanArray();
    private Drawable foregroundOverlay;

    @SuppressLint("SimpleDateFormat")
    public BrowserHistoryAdapter(Context context, BrowserHistoryManager manager, boolean pick, OnHistoryRecyclerListener listener) {
        inflater = LayoutInflater.from(context);
        dateFormat = android.text.format.DateFormat.getLongDateFormat(context);
        timeFormat = new SimpleDateFormat("kk:mm");
        mManager = manager;
        faviconManager = FaviconManager.Companion.getInstance(context.getApplicationContext());
        mListener = listener;
        histories = mManager.getList(0, 100);
        mQuery = null;
        calendar = Calendar.getInstance();
        pickMode = pick;

        defaultColorFilter = new PorterDuffColorFilter(
                ThemeUtils.getColorFromThemeRes(context, R.attr.iconColor), PorterDuff.Mode.SRC_ATOP);
        foregroundOverlay = new ColorDrawable(ResourcesCompat.getColor(context.getResources(),
                R.color.selected_overlay, context.getTheme()));
    }

    @Override
    public HistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HistoryHolder(inflater.inflate(R.layout.history_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final HistoryHolder holder, int position) {
        BrowserHistory item = histories.get(holder.getAdapterPosition());
        String url = UrlUtils.decodeUrlHost(item.getUrl());
        Bitmap image = faviconManager.get(item.getUrl());

        if (image == null) {
            holder.imageButton.setImageResource(R.drawable.ic_public_white_24dp);
            holder.imageButton.setColorFilter(defaultColorFilter);
        } else {
            holder.imageButton.setImageBitmap(image);
            holder.imageButton.setColorFilter(faviconColorFilter);
        }

        if (multiSelectMode && isSelected(position)) {
            holder.foreground.setBackground(foregroundOverlay);
        } else {
            holder.foreground.setBackground(null);
        }
        holder.titleTextView.setText(item.getTitle());
        holder.urlTextView.setText(url);
        holder.timeTextView.setText(timeFormat.format(new Date(item.getTime())));

        holder.itemView.setOnClickListener(v -> {
            if (multiSelectMode) {
                toggle(holder.getAdapterPosition());
            } else {
                mListener.onRecyclerItemClicked(v, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> mListener.onRecyclerItemLongClicked(v, holder.getAdapterPosition()));

        if (pickMode) {
            holder.imageButton.setClickable(false);
        } else {
            holder.imageButton.setOnClickListener(v -> mListener.onIconClicked(v, holder.getAdapterPosition()));
        }
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    public BrowserHistory getItem(int position) {
        return histories.get(position);
    }

    public BrowserHistory remove(int position) {
        return histories.remove(position);
    }

    public void loadMore() {
        if (mQuery == null) {
            histories.addAll(mManager.getList(getItemCount(), 100));
        } else {
            histories.addAll(mManager.search(mQuery, getItemCount(), 100));
        }
        mDecoration.clearHeaderCache();
    }

    public void reLoad() {
        mQuery = null;
        histories = mManager.getList(0, 100);
        mDecoration.clearHeaderCache();
        notifyDataSetChanged();
    }

    public void search(String query) {
        mQuery = query;
        histories = mManager.search(mQuery, 0, 100);
        mDecoration.clearHeaderCache();
        notifyDataSetChanged();
    }

    public void setDecoration(StickyHeaderDecoration headerDecoration) {
        mDecoration = headerDecoration;
    }

    @Override
    public long getHeaderId(int position) {
        long time = histories.get(position).getTime();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    public HeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return new HeaderHolder(inflater.inflate(R.layout.history_header, parent, false));
    }

    @Override
    public void onBindHeaderViewHolder(HeaderHolder viewHolder, int position) {
        viewHolder.header.setText(dateFormat.format(new Date(histories.get(position).getTime())));
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    public void setMultiSelectMode(boolean multiSelect) {
        if (multiSelect != multiSelectMode) {
            multiSelectMode = multiSelect;

            if (!multiSelect) {
                itemSelected.clear();
            }

            notifyDataSetChanged();
        }
    }

    public void toggle(int position) {
        setSelect(position, !itemSelected.get(position, false));
    }

    public void setSelect(int position, boolean isSelect) {
        boolean old = itemSelected.get(position, false);
        itemSelected.put(position, isSelect);

        if (old != isSelect) {
            notifyDataSetChanged();
        }
    }

    public boolean isSelected(int position) {
        return itemSelected.get(position, false);
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>();
        for (int i = 0; itemSelected.size() > i; i++) {
            if (itemSelected.valueAt(i)) {
                items.add(itemSelected.keyAt(i));
            }
        }
        return items;
    }

    static class HistoryHolder extends RecyclerView.ViewHolder {

        View foreground;
        ImageButton imageButton;
        TextView titleTextView;
        TextView urlTextView;
        TextView timeTextView;

        HistoryHolder(View itemView) {
            super(itemView);

            foreground = itemView.findViewById(R.id.foreground);
            imageButton = itemView.findViewById(R.id.imageButton);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            urlTextView = itemView.findViewById(R.id.urlTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);

            int fontSizeSetting = AppData.font_size.history.get();
            if (fontSizeSetting >= 0) {
                int normal = FontUtils.getTextSize(fontSizeSetting);
                int small = FontUtils.getSmallerTextSize(fontSizeSetting);

                titleTextView.setTextSize(normal);
                urlTextView.setTextSize(small);
                timeTextView.setTextSize(small);
            }
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView header;

        HeaderHolder(View itemView) {
            super(itemView);

            header = (TextView) itemView;

            int fontSizeSetting = AppData.font_size.history.get();
            if (fontSizeSetting >= 0) {
                header.setTextSize(FontUtils.getTextSize(fontSizeSetting));
            }
        }
    }

    public interface OnHistoryRecyclerListener extends OnRecyclerListener {
        void onIconClicked(View v, int position);
    }
}
