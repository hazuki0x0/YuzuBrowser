/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.download;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.view.ProgressDialogFragmentCompat;

public class FastDownloadActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<File> {

    public static final String EXTRA_FILE_URL = "fileURL";
    public static final String EXTRA_FILE_REFERER = "fileReferer";
    public static final String EXTRA_DEFAULT_EXTENSION = "defExt";

    private ProgressDialogFragmentCompat progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null) {
            finish();
            return;
        }

        String file = getIntent().getStringExtra(EXTRA_FILE_URL);

        if (file == null) {
            finish();
            return;
        }

        Bundle bundle = new Bundle();

        bundle.putString(EXTRA_FILE_URL, file);
        bundle.putString(EXTRA_FILE_REFERER, getIntent().getStringExtra(EXTRA_FILE_REFERER));
        bundle.putString(EXTRA_DEFAULT_EXTENSION, getIntent().getStringExtra(EXTRA_DEFAULT_EXTENSION));

        getSupportLoaderManager().initLoader(0, bundle, this);
    }

    @Override
    public Loader<File> onCreateLoader(int id, Bundle args) {
        progressDialog = ProgressDialogFragmentCompat.newInstance(getString(R.string.now_downloading));
        progressDialog.show(getSupportFragmentManager(), "dialog");

        return new FastDownloadTask(getApplicationContext(),
                args.getString(EXTRA_FILE_URL),
                args.getString(EXTRA_FILE_REFERER),
                args.getString(EXTRA_DEFAULT_EXTENSION));
    }

    @Override
    public void onLoadFinished(Loader<File> loader, File data) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });

        if (data != null) {
            Intent result = new Intent();
            result.setData(DownloadFileProvider.getUriForFIle(data));

            setResult(RESULT_OK, result);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    @Override
    public void onLoaderReset(Loader<File> loader) {

    }
}
