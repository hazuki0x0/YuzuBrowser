/*
 * Copyright (C) 2017-2018 Hazuki
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
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkManager;
import jp.hazuki.yuzubrowser.legacy.bookmark.view.BookmarkFoldersDialog.OnFolderSelectedListener;
import jp.hazuki.yuzubrowser.legacy.browser.BrowserController;
import jp.hazuki.yuzubrowser.legacy.utils.view.SpinnerButton;

public abstract class AddBookmarkDialog<S extends BookmarkItem, T> implements OnFolderSelectedListener {
    protected final Context mContext;
    protected final AlertDialog mDialog;
    protected final EditText titleEditText;
    protected final EditText urlEditText;
    protected final TextView folderTextView;
    protected final SpinnerButton folderButton;
    protected final CheckBox addToTopCheckBox;
    protected DialogInterface.OnClickListener mOnClickListener;
    protected final S mItem;
    protected BookmarkManager mManager;
    protected BookmarkFolder mParent;

    public AddBookmarkDialog(final Context context, BookmarkManager manager, S item, String title, T url) {
        mContext = context;
        mItem = item;
        mManager = manager;

        if (mManager == null)
            mManager = BookmarkManager.getInstance(context);

        View view = inflateView();
        titleEditText = view.findViewById(R.id.titleEditText);
        urlEditText = view.findViewById(R.id.urlEditText);
        folderTextView = view.findViewById(R.id.folderTextView);
        folderButton = view.findViewById(R.id.folderButton);
        addToTopCheckBox = view.findViewById(R.id.addToTopCheckBox);

        initView(view, title, url);

        //titleEditText.setText((title == null)?url:title);

        mDialog = new AlertDialog.Builder(context)
                .setTitle(mItem == null ? R.string.add_bookmark : R.string.edit_bookmark)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    protected Context getContext() {
        return mContext;
    }

    protected View inflateView() {
        return LayoutInflater.from(mContext).inflate(R.layout.add_bookmark_dialog, null);
    }

    protected void initView(View view, String title, T url) {
        if (mItem == null) {
            final BookmarkFolder root = mManager.getRoot();
            mParent = root;
            folderButton.setText(root.title);
            folderButton.setOnClickListener(v -> new BookmarkFoldersDialog(mContext, mManager)
                    .setTitle(R.string.folder)
                    .setCurrentFolder(root)
                    .setOnFolderSelectedListener(AddBookmarkDialog.this)
                    .show());
        } else {
            folderTextView.setVisibility(View.GONE);
            folderButton.setVisibility(View.GONE);
            addToTopCheckBox.setVisibility(View.GONE);
        }
    }

    public void show() {
        mDialog.show();

        mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            Editable title = titleEditText.getText();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(mDialog.getContext(), R.string.title_empty_mes, Toast.LENGTH_SHORT).show();
                return;
            }
            Editable url = urlEditText.getText();
            if (TextUtils.isEmpty(url)) {
                Toast.makeText(mDialog.getContext(), R.string.url_empty_mes, Toast.LENGTH_SHORT).show();
                return;
            }

            S item = makeItem(mItem, title.toString(), url.toString());
            if (item != null) {
                if (addToTopCheckBox.isChecked())
                    mManager.addFirst(mParent, item);
                else
                    mManager.add(mParent, item);
            }
            if (mItem != null && addToTopCheckBox.isChecked()) {
                mManager.moveToFirst(mParent, mItem);
            }

            if (mManager.save()) {
                Toast.makeText(mDialog.getContext(), R.string.succeed, Toast.LENGTH_SHORT).show();
                if (mOnClickListener != null)
                    mOnClickListener.onClick(mDialog, DialogInterface.BUTTON_POSITIVE);
                if (mContext instanceof BrowserController) {
                    ((BrowserController) mContext).requestIconChange();
                }
                mDialog.dismiss();
            } else {
                Toast.makeText(mDialog.getContext(), R.string.failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected abstract S makeItem(S item, String title, String url);

    public AddBookmarkDialog<S, T> setOnClickListener(DialogInterface.OnClickListener l) {
        mOnClickListener = l;
        return this;
    }

    @Override
    public boolean onFolderSelected(DialogInterface dialog, BookmarkFolder folder) {
        folderButton.setText(folder.title);
        mParent = folder;
        return false;
    }
}
