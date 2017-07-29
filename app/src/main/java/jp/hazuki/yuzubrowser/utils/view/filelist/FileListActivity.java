package jp.hazuki.yuzubrowser.utils.view.filelist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;

import java.io.File;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

public class FileListActivity extends ThemeActivity implements FileListFragment.OnFileSelectedListener {
    public static final String EXTRA_FILE = "FileListActivity.extra.EXTRA_TARGET_DIRECTORY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        File file = null;
        Intent intent = getIntent();
        if (intent != null)
            file = (File) intent.getSerializableExtra(EXTRA_FILE);
        if (file == null)
            file = new File(getApplicationInfo().dataDir);

        Fragment fragment = FileListFragment.newInstance(file, false, false);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();

    }

    @Override
    public void onBackPressed() {
        FileListFragment fragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            if (!fragment.goBack()) {
                finish();
            }
        }
    }

    @Override
    public void onFileSelected(File file) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FILE, file);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.set_folder_path).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                final EditText edittext = new EditText(FileListActivity.this);
                edittext.setSingleLine(true);
                edittext.setText(getCurrentAbsolutePath());

                new AlertDialog.Builder(FileListActivity.this)
                        .setTitle(R.string.set_folder_path)
                        .setView(edittext)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                setFilePath(new File(edittext.getText().toString()));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }
        });
        menu.add(R.string.go_data_folder).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                setFilePath(new File(getApplicationInfo().dataDir));
                return true;
            }
        });
        menu.add(R.string.go_sd_folder).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                setFilePath(Environment.getExternalStorageDirectory());
                return true;
            }
        });
        menu.add(R.string.quit).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                finish();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private String getCurrentAbsolutePath() {
        FileListFragment fragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            return fragment.getCurrentFolder().getAbsolutePath();
        }
        return null;
    }

    private void setFilePath(File filePath) {
        FileListFragment fragment = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            fragment.setFilePath(filePath);
        }
    }
}
