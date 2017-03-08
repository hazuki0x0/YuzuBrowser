package jp.hazuki.yuzubrowser.debug;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.FileUtils;
import jp.hazuki.yuzubrowser.utils.view.filelist.FileListActivity;
import jp.hazuki.yuzubrowser.utils.view.filelist.FileListFragment;

public class DebugFileListActivity extends FileListActivity implements FileListFragment.OnFileItemLongClickListener {
    private File mCopiedFile;

    @Override
    public void onFileSelected(File file) {
        Intent intent = new Intent(getApplicationContext(), TextEditActivity.class);
        intent.putExtra(Intent.EXTRA_STREAM, file);
        startActivity(intent);
    }

    @Override
    public boolean onListFileItemLongClick(ListView l, View v, final File file, int position, long id) {
        (new AlertDialog.Builder(this))
                .setTitle(file.getName())
                .setItems(
                        new String[]{"Copy", "Delete"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        if (file.isFile()) {
                                            Toast.makeText(getApplicationContext(), "file copied", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "folder copied", Toast.LENGTH_SHORT).show();
                                        }
                                        mCopiedFile = file;
                                        break;
                                    case 1:
                                        (new AlertDialog.Builder(DebugFileListActivity.this))
                                                .setTitle("Confirm")
                                                .setMessage("Delete?")
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface arg0, int arg1) {
                                                        if (!FileUtils.deleteFile(file)) {
                                                            Toast.makeText(getApplicationContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                                                        }
                                                        notifyDataSetChanged();
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.no, null)
                                                .show();
                                        break;
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add("Paste").setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                if (mCopiedFile == null) {
                    Toast.makeText(getApplicationContext(), "File not available", Toast.LENGTH_SHORT).show();
                    return true;
                }

                (new AlertDialog.Builder(DebugFileListActivity.this))
                        .setTitle("Confirm")
                        .setMessage("Paste?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (!FileUtils.copyFile(mCopiedFile, getCurrentFolder())) {
                                    Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
                                }
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
            }
        });
        menu.add("Make folder").setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                final EditText edittext = new EditText(DebugFileListActivity.this);
                edittext.setMaxLines(1);
                edittext.setText("new folder");

                (new AlertDialog.Builder(DebugFileListActivity.this))
                        .setTitle("Make folder")
                        .setView(edittext)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                new File(getCurrentFolder(), edittext.getText().toString()).mkdir();
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }
        });
        return true;
    }

    private void notifyDataSetChanged() {
        FileListFragment fragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            fragment.notifyDataSetChanged();
        }
    }

    private File getCurrentFolder() {
        FileListFragment fragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            return fragment.getCurrentFolder();
        } else {
            return null;
        }
    }
}
