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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import androidx.annotation.NonNull;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkManager;

public class BookmarkFoldersDialog {
    private final Context mContext;
    private final BookmarkManager mManager;
    private final AlertDialog mDialog;
    private final ListView mListView;
    private BookmarkFolder mCurrentFolder;
    private final ArrayList<BookmarkFolder> mFolderList = new ArrayList<>();
    private Collection<BookmarkItem> mExcludeList;
    private OnFolderSelectedListener mOnFolderSelectedListener;

    private TextView titleText;

    public interface OnFolderSelectedListener {
        boolean onFolderSelected(DialogInterface dialog, BookmarkFolder folder);
    }

    public BookmarkFoldersDialog(Context context, final BookmarkManager manager) {
        mContext = context;
        mManager = manager;
        mListView = new ListView(context);

        final LayoutInflater inflater = LayoutInflater.from(mContext);

        View top = inflater.inflate(R.layout.dialog_title, null);
        titleText = top.findViewById(R.id.titleText);
        ImageButton button = top.findViewById(R.id.addButton);

        button.setOnClickListener(v -> new AddBookmarkFolderDialog(mContext, mManager, mContext.getString(R.string.new_folder_name), mCurrentFolder)
                .setOnClickListener((dialog, which) -> setFolder(mCurrentFolder))
                .show());
        button.setOnLongClickListener(v -> {
            Toast.makeText(mContext, R.string.new_folder_name, Toast.LENGTH_SHORT).show();
            return true;
        });

        mDialog = new AlertDialog.Builder(context)
                .setView(mListView)
                .setCustomTitle(top)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnFolderSelectedListener != null)
                            mOnFolderSelectedListener.onFolderSelected(mDialog, mCurrentFolder);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();


        mListView.setAdapter(new ArrayAdapter<BookmarkFolder>(mContext.getApplicationContext(), 0, mFolderList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null)
                    convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
                BookmarkItem item = getItem(position);
                ((TextView) convertView.findViewById(android.R.id.text1)).setText((item != null) ? item.getTitle() : "..");
                return convertView;
            }
        });

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            BookmarkFolder folder = mFolderList.get(position);
            if (folder == null)
                folder = mCurrentFolder.getParent();
            setFolder(folder);
        });

        mListView.setOnItemLongClickListener((parent, view, position, id) -> {
            BookmarkFolder folder = mFolderList.get(position);
            if (folder == null)
                folder = mCurrentFolder.getParent();
            return mOnFolderSelectedListener != null &&
                    mOnFolderSelectedListener.onFolderSelected(mDialog, folder);
        });
    }

    public BookmarkFoldersDialog setTitle(CharSequence title) {
        //mDialog.setTitle(title);
        titleText.setText(title);
        return this;
    }

    public BookmarkFoldersDialog setTitle(int title) {
        //mDialog.setTitle(title);
        titleText.setText(title);
        return this;
    }

    public BookmarkFoldersDialog setCurrentFolder(BookmarkFolder folder) {
        mExcludeList = null;
        setFolder(folder);
        return this;
    }

    public BookmarkFoldersDialog setCurrentFolder(BookmarkFolder folder, Collection<BookmarkItem> excludeList) {
        mExcludeList = excludeList;
        setFolder(folder);
        return this;
    }

    public BookmarkFoldersDialog setCurrentFolder(BookmarkFolder folder, BookmarkItem excludeItem) {
        mExcludeList = new HashSet<>();
        mExcludeList.add(excludeItem);
        setFolder(folder);
        return this;
    }

    public BookmarkFoldersDialog setOnFolderSelectedListener(OnFolderSelectedListener l) {
        mOnFolderSelectedListener = l;
        return this;
    }

    private void setFolder(BookmarkFolder folder) {
        mFolderList.clear();
        mCurrentFolder = folder;
        if (folder.getParent() != null)
            mFolderList.add(null);//for move to prev folder
        for (BookmarkItem i : folder.getItemList())
            if (i instanceof BookmarkFolder && (mExcludeList == null || !mExcludeList.contains(i)))
                mFolderList.add((BookmarkFolder) i);
        ((ArrayAdapter<?>) mListView.getAdapter()).notifyDataSetChanged();
    }

    public void show() {
        mDialog.show();
    }
}
