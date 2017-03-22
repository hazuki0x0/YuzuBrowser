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
import jp.hazuki.yuzubrowser.bookmark.BookmarkManager;
import jp.hazuki.yuzubrowser.bookmark.util.BookmarkIdGenerator;

public class AddBookmarkFolderDialog {
    private final AlertDialog mDialog;
    private final EditText titleEditText;
    private BookmarkFolder mParent;
    private DialogInterface.OnClickListener mOnClickListener;
    private BookmarkFolder mItem;
    private BookmarkManager mManager;

    public AddBookmarkFolderDialog(Context context, BookmarkManager manager, BookmarkFolder item) {
        this(context, manager, item.title, item.parent);
        mItem = item;
    }

    public AddBookmarkFolderDialog(final Context context, BookmarkManager manager, String title, BookmarkFolder parent) {
        mManager = manager;
        mParent = parent;

        View view = LayoutInflater.from(context).inflate(R.layout.add_bookmark_folder_dialog, null);
        titleEditText = (EditText) view.findViewById(R.id.titleEditText);

        titleEditText.setText(title);

        mDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.add_folder)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
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

                if (mManager == null)
                    mManager = new BookmarkManager(mDialog.getContext());

                if (mParent == null)
                    mParent = mManager.getRoot();

                if (mItem == null) {
                    BookmarkFolder item = new BookmarkFolder(title.toString(), mParent, BookmarkIdGenerator.getNewId());
                    mParent.add(item);
                } else {
                    if (mItem.parent == null) {
                        mItem.parent = mParent;
                        mParent.add(mItem);
                    }
                    mItem.title = title.toString();
                }

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

    public AddBookmarkFolderDialog setOnClickListener(DialogInterface.OnClickListener l) {
        mOnClickListener = l;
        return this;
    }
}
