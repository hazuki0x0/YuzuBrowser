package jp.hazuki.yuzubrowser.legacy.utils.view.filelist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.io.File;

import jp.hazuki.yuzubrowser.legacy.utils.view.filelist.FileListViewController.OnFileSelectedListener;

public class FileListDialog {
    private final Context mContext;
    private final FileListViewController mController;
    private final AlertDialog.Builder mBuilder;

    public FileListDialog(Context context) {
        mContext = context;
        mController = new FileListViewController(context);
        mBuilder = new AlertDialog.Builder(context);
        mBuilder.setNegativeButton(android.R.string.cancel, null);
    }

    private void createListView() {
        ListView listView = new ListView(mContext);
        mBuilder.setView(listView);
        mController.setShowParentMover(true);
        mController.setListView(listView);
    }

    public FileListDialog setFilePath(File file) {
        mController.setFilePath(file);
        mBuilder.setTitle(file.getName());
        return this;
    }

    public FileListDialog setShowParentMover(boolean b) {
        mController.setShowParentMover(b);
        return this;
    }

    public FileListDialog setShowDirectoryOnly(boolean b) {
        mController.setShowDirectoryOnly(b);
        return this;
    }

    public FileListDialog setShowExtensionOnly(String ext) {
        mController.setShowExtensionOnly(ext);
        return this;
    }

    public FileListDialog setOnFileSelectedListener(OnFileSelectedListener l) {
        mController.setOnFileSelectedListener(l);
        return this;
    }

    public File getCurrentFolder() {
        return mController.getCurrentFolder();
    }

    public File[] getCurrentFileList() {
        return mController.getCurrentFileList();
    }

    public void show() {
        if (mController.isShowDirectoryOnly()) {
            mBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mController.notifySelectThisFolder();
                }
            });
        }

        createListView();
        final AlertDialog dialog = mBuilder.show();

        mController.getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mController.onItemClick(position))
                    dialog.dismiss();
                else
                    dialog.setTitle(mController.getCurrentFolder().getName());
            }
        });
    }
}
