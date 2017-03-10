package jp.hazuki.yuzubrowser.bookmark.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.bookmark.BookmarkManager;

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
        titleText = (TextView) top.findViewById(R.id.titleText);
        ImageButton button = (ImageButton) top.findViewById(R.id.addButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddBookmarkFolderDialog(mContext, mManager, mContext.getString(R.string.new_folder_name), mCurrentFolder)
                        .setOnClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setFolder(mCurrentFolder);
                            }
                        })
                        .show();
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(mContext, R.string.new_folder_name, Toast.LENGTH_SHORT).show();
                return true;
            }
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
                ((TextView) convertView.findViewById(android.R.id.text1)).setText((item != null) ? item.title : "..");
                return convertView;
            }
        });

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookmarkFolder folder = mFolderList.get(position);
                if (folder == null)
                    folder = mCurrentFolder.parent;
                setFolder(folder);
            }
        });

        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                BookmarkFolder folder = mFolderList.get(position);
                if (folder == null)
                    folder = mCurrentFolder.parent;
                return mOnFolderSelectedListener != null &&
                        mOnFolderSelectedListener.onFolderSelected(mDialog, folder);
            }
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
        mExcludeList = new ArrayList<>();
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
        if (folder.parent != null)
            mFolderList.add(null);//for move to prev folder
        for (BookmarkItem i : folder.list)
            if (i instanceof BookmarkFolder && (mExcludeList == null || !mExcludeList.contains(i)))
                mFolderList.add((BookmarkFolder) i);
        ((ArrayAdapter<?>) mListView.getAdapter()).notifyDataSetChanged();
    }

    public void show() {
        mDialog.show();
    }
}
