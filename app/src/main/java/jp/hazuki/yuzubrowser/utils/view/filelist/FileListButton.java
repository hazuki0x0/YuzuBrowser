package jp.hazuki.yuzubrowser.utils.view.filelist;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class FileListButton extends AppCompatButton implements Button.OnClickListener {
    private final FileListDialog mDialog;

    public FileListButton(Context context) {
        this(context, null);
    }

    public FileListButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        mDialog = new FileListDialog(context);
    }

    public void setFilePath(File file) {
        mDialog.setFilePath(file);
    }

    public void setShowDirectoryOnly(boolean b) {
        mDialog.setShowDirectoryOnly(b);
    }

    public void setOnFileSelectedListener(FileListViewController.OnFileSelectedListener l) {
        mDialog.setOnFileSelectedListener(l);
    }

    public File getCurrentFolder() {
        return mDialog.getCurrentFolder();
    }

    public File[] getCurrentFileList() {
        return mDialog.getCurrentFileList();
    }

    @Override
    public void onClick(View v) {
        mDialog.show();
    }
}
