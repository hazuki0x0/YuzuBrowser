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

package jp.hazuki.yuzubrowser.bookmark.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.bookmark.BookmarkSite;
import jp.hazuki.yuzubrowser.history.BrowserHistoryManager;
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

public class BookmarkItemAdapter extends ArrayRecyclerAdapter<BookmarkItem, BookmarkItemAdapter.BookmarkFolderHolder> {
    private static final int TYPE_SITE = 1;
    private static final int TYPE_FOLDER = 2;

    private static final PorterDuffColorFilter defaultColorFilter = new PorterDuffColorFilter(0xffe6e6e6, PorterDuff.Mode.SRC_ATOP);
    private static final PorterDuffColorFilter faviconColorFilter = new PorterDuffColorFilter(0, PorterDuff.Mode.SRC_ATOP);

    private final int normalBackGround;

    private OnBookmarkRecyclerListener bookmarkItemListener;
    private BrowserHistoryManager historyManager;
    private boolean pickMode;
    private boolean openNewTab;

    public BookmarkItemAdapter(Context context, List<BookmarkItem> list, boolean pick, boolean openNewTab, OnBookmarkRecyclerListener listener) {
        super(context, list, null);
        bookmarkItemListener = listener;
        pickMode = pick;
        this.openNewTab = openNewTab;
        TypedArray a = context.obtainStyledAttributes(R.style.CustomThemeBlack, new int[]{android.R.attr.selectableItemBackground});
        normalBackGround = a.getResourceId(0, 0);
        a.recycle();

        historyManager = BrowserHistoryManager.getInstance(context);

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
            ((BookmarkSiteHolder) holder).url.setText(((BookmarkSite) item).url);
            if (!openNewTab || pickMode || isMultiSelectMode()) {
                ((BookmarkSiteHolder) holder).imageButton.setEnabled(false);
                ((BookmarkSiteHolder) holder).imageButton.setClickable(false);
            } else {
                ((BookmarkSiteHolder) holder).imageButton.setEnabled(true);
                ((BookmarkSiteHolder) holder).imageButton.setClickable(true);
                ((BookmarkSiteHolder) holder).imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bookmarkItemListener.onIconClick(v, holder.getAdapterPosition());
                    }
                });
            }

            Bitmap bitmap = historyManager.getFavicon(((BookmarkSite) item).url);
            if (bitmap != null) {
                ((BookmarkSiteHolder) holder).imageButton.setImageBitmap(bitmap);
                ((BookmarkSiteHolder) holder).imageButton.setColorFilter(faviconColorFilter);
            } else {
                ((BookmarkSiteHolder) holder).imageButton.setImageResource(R.drawable.ic_bookmark_white_24dp);
                ((BookmarkSiteHolder) holder).imageButton.setColorFilter(defaultColorFilter);
            }
        }
        holder.title.setText(item.title);

        if (isMultiSelectMode()) {
            setSelectedBackground(holder.itemView, isSelected(position));
        } else {
            holder.itemView.setBackgroundResource(normalBackGround);
        }
    }

    @Override
    protected BookmarkFolderHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SITE:
                return new BookmarkSiteHolder(inflater.inflate(R.layout.bookmark_item_site, parent, false));
            case TYPE_FOLDER:
                return new BookmarkFolderHolder(inflater.inflate(R.layout.bookmark_item_folder, parent, false));
            default:
                throw new IllegalStateException("Unknown BookmarkItem type");
        }
    }

    public byte[] getFavicon(BookmarkSite site) {
        return historyManager.getFaviconImage(site.url);
    }

    private void setSelectedBackground(View view, boolean selected) {
        if (selected) {
            view.setBackgroundResource(R.drawable.selectable_selected_item_background);
        } else {
            view.setBackgroundResource(normalBackGround);
        }
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

    static class BookmarkFolderHolder extends RecyclerView.ViewHolder {
        TextView title;

        BookmarkFolderHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    static class BookmarkSiteHolder extends BookmarkFolderHolder {
        ImageButton imageButton;
        TextView url;

        BookmarkSiteHolder(View itemView) {
            super(itemView);
            imageButton = (ImageButton) itemView.findViewById(R.id.imageButton);
            url = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }

    public interface OnBookmarkRecyclerListener extends OnRecyclerListener {
        void onIconClick(View v, int position);
    }
}
