package jp.hazuki.yuzubrowser.bookmark.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.bookmark.BookmarkManager;
import jp.hazuki.yuzubrowser.bookmark.view.BookmarkFoldersDialog.OnFolderSelectedListener;
import jp.hazuki.yuzubrowser.utils.view.SpinnerButton;

public abstract class AddBookmarkDialog<S extends BookmarkItem, T> implements OnFolderSelectedListener {
    protected final Context mContext;
    protected final AlertDialog mDialog;
    protected final EditText titleEditText;
    protected final EditText urlEditText;
    protected final SpinnerButton folderButton;
    protected DialogInterface.OnClickListener mOnClickListener;
    protected final S mItem;
    protected BookmarkManager mManager;
    protected BookmarkFolder mParent;

    public AddBookmarkDialog(final Context context, BookmarkManager manager, S item, String title, T url) {
        mContext = context;
        mItem = item;
        mManager = manager;

        if (mManager == null)
            mManager = new BookmarkManager(context);

        View view = inflateView();
        titleEditText = (EditText) view.findViewById(R.id.titleEditText);
        urlEditText = (EditText) view.findViewById(R.id.urlEditText);
        folderButton = (SpinnerButton) view.findViewById(R.id.folderButton);

        initView(view, title, url);

        //titleEditText.setText((title == null)?url:title);
        //urlEditText.setText(url);

        mDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.add_bookmark)
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
            folderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new BookmarkFoldersDialog(mContext, mManager)
                            .setTitle(R.string.folder)
                            .setCurrentFolder(root)
                            .setOnFolderSelectedListener(AddBookmarkDialog.this)
                            .show();
                }
            });
        } else {
            folderButton.setVisibility(View.GONE);
        }
    }

    public void show() {
        mDialog.show();

        mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                if (item != null)
                    mParent.add(item);

                if (mManager.write()) {
                    Toast.makeText(mDialog.getContext(), R.string.succeed, Toast.LENGTH_SHORT).show();
                    if (mOnClickListener != null)
                        mOnClickListener.onClick(mDialog, DialogInterface.BUTTON_POSITIVE);
                    mDialog.dismiss();
                } else {
                    Toast.makeText(mDialog.getContext(), R.string.failed, Toast.LENGTH_LONG).show();
                }
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
