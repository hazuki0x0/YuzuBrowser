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

package jp.hazuki.yuzubrowser.legacy.bookmark.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import androidx.core.content.res.ResourcesCompat;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkSite;
import jp.hazuki.yuzubrowser.legacy.favicon.FaviconManager;
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData;
import jp.hazuki.yuzubrowser.legacy.utils.FontUtils;
import jp.hazuki.yuzubrowser.legacy.utils.ThemeUtils;
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener;

public class BookmarkItemAdapter extends ArrayRecyclerAdapter<BookmarkItem, BookmarkItemAdapter.BookmarkFolderHolder> {
    private static final int TYPE_SITE = 1;
    private static final int TYPE_FOLDER = 2;

    private final PorterDuffColorFilter defaultColorFilter;

    private final Drawable foregroundOverlay;

    private OnBookmarkRecyclerListener bookmarkItemListener;
    private FaviconManager faviconManager;
    private boolean pickMode;
    private boolean openNewTab;

    public BookmarkItemAdapter(Context context, List<BookmarkItem> list, boolean pick, boolean openNewTab, OnBookmarkRecyclerListener listener) {
        super(context, list, null);
        bookmarkItemListener = listener;
        pickMode = pick;
        this.openNewTab = openNewTab;
        foregroundOverlay = new ColorDrawable(ResourcesCompat.getColor(
                context.getResources(), R.color.selected_overlay, context.getTheme()));

        faviconManager = FaviconManager.getInstance(context);
        defaultColorFilter = new PorterDuffColorFilter(ThemeUtils.getColorFromThemeRes(context, R.attr.iconColor), PorterDuff.Mode.SRC_ATOP);

        setRecyclerListener(new OnRecyclerListener() {
            @Override
            public void onRecyclerItemClicked(View v, int position) {
                if (isMultiSelectMode()) {
                    toggle(position);
                } else {
                    if (bookmarkItemListener != null)
                        bookmarkItemListener.onRecyclerItemClicked(v, position);
                }
            }

            @Override
            public boolean onRecyclerItemLongClicked(View v, int position) {
                return bookmarkItemListener.onRecyclerItemLongClicked(v, position);
            }
        });
    }

    @Override
    public void onBindViewHolder(final BookmarkFolderHolder holder, BookmarkItem item, final int position) {
        if (item instanceof BookmarkSite) {
            ((BookmarkSiteHolder) holder).url.setText(((BookmarkSite) item).getUrl());
            if (!openNewTab || pickMode || isMultiSelectMode()) {
                ((BookmarkSiteHolder) holder).imageButton.setEnabled(false);
                ((BookmarkSiteHolder) holder).imageButton.setClickable(false);
            } else {
                ((BookmarkSiteHolder) holder).imageButton.setEnabled(true);
                ((BookmarkSiteHolder) holder).imageButton.setClickable(true);
            }

            Bitmap bitmap = faviconManager.get(((BookmarkSite) item).getUrl());
            if (bitmap != null) {
                ((BookmarkSiteHolder) holder).imageButton.setImageBitmap(bitmap);
                ((BookmarkSiteHolder) holder).imageButton.clearColorFilter();
            } else {
                ((BookmarkSiteHolder) holder).imageButton.setImageResource(R.drawable.ic_bookmark_white_24dp);
                ((BookmarkSiteHolder) holder).imageButton.setColorFilter(defaultColorFilter);
            }
        }

        if (isMultiSelectMode() && isSelected(position)) {
            holder.foreground.setBackground(foregroundOverlay);
        } else {
            holder.foreground.setBackground(null);
        }
    }

    private void onIconClick(View v, int position, BookmarkItem item) {
        position = searchPosition(position, item);
        if (position < 0) return;
        bookmarkItemListener.onIconClick(v, position);
    }

    @Override
    protected BookmarkFolderHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SITE:
                return new BookmarkSiteHolder(inflater.inflate(R.layout.bookmark_item_site, parent, false), this);
            case TYPE_FOLDER:
                return new BookmarkFolderHolder(inflater.inflate(R.layout.bookmark_item_folder, parent, false), this);
            default:
                throw new IllegalStateException("Unknown BookmarkItem type");
        }
    }

    public byte[] getFavicon(BookmarkSite site) {
        return faviconManager.getFaviconBytes(site.getUrl());
    }

    @Override
    public int getItemViewType(int position) {
        BookmarkItem item = get(position);
        if (item instanceof BookmarkSite) {
            return TYPE_SITE;
        } else if (item instanceof BookmarkFolder) {
            return TYPE_FOLDER;
        } else {
            throw new IllegalStateException("Unknown BookmarkItem type");
        }
    }

    @Override
    public void onBindViewHolder(BookmarkFolderHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    static class BookmarkFolderHolder extends ArrayRecyclerAdapter.ArrayViewHolder<BookmarkItem> {
        TextView title;
        View foreground;


        public BookmarkFolderHolder(View itemView, BookmarkItemAdapter adapter) {
            super(itemView, adapter);
            title = itemView.findViewById(android.R.id.text1);
            foreground = itemView.findViewById(R.id.foreground);

            int fontSize = AppData.font_size.bookmark.get();
            if (fontSize >= 0) {
                title.setTextSize(FontUtils.getTextSize(fontSize));
            }
        }

        @Override
        public void setUp(BookmarkItem item) {
            super.setUp(item);
            title.setText(item.getTitle());
        }
    }

    static class BookmarkSiteHolder extends BookmarkFolderHolder {
        ImageButton imageButton;
        TextView url;

        BookmarkSiteHolder(View itemView, final BookmarkItemAdapter adapter) {
            super(itemView, adapter);
            imageButton = itemView.findViewById(R.id.imageButton);
            url = itemView.findViewById(android.R.id.text2);

            imageButton.setOnClickListener(v -> adapter.onIconClick(v, getAdapterPosition(), getItem()));

            int fontSize = AppData.font_size.bookmark.get();
            if (fontSize >= 0) {
                url.setTextSize(FontUtils.getSmallerTextSize(fontSize));
            }
        }
    }

    public interface OnBookmarkRecyclerListener extends OnRecyclerListener {
        void onIconClick(View v, int position);
    }
}
