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

package jp.hazuki.yuzubrowser.legacy.utils.view.filelist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;

import java.io.File;

import androidx.fragment.app.Fragment;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity;

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
