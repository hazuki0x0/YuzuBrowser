/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.theme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.activity.MainSettingsActivity;
import jp.hazuki.yuzubrowser.utils.view.ProgressDialogFragmentCompat;

public class ThemeImportActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ThemeImportTask.Result> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        if (getIntent() != null && Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Intent.EXTRA_STREAM, getIntent().getData());
            getSupportLoaderManager().initLoader(0, bundle, this);
        } else {
            finish();
        }
    }

    @Override
    public Loader<ThemeImportTask.Result> onCreateLoader(int id, Bundle args) {
        ProgressDialogFragmentCompat progressDialog = ProgressDialogFragmentCompat.newInstance("Installing...");
        progressDialog.show(getSupportFragmentManager(), "dialog");

        return new ThemeImportTask(this, (Uri) args.getParcelable(Intent.EXTRA_STREAM));
    }

    @Override
    public void onLoadFinished(Loader<ThemeImportTask.Result> loader, final ThemeImportTask.Result data) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("dialog");
                if (fragment instanceof ProgressDialogFragmentCompat)
                    ((ProgressDialogFragmentCompat) fragment).dismiss();

                if (data.isSuccess()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.theme_imported, data.getMessage()), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainSettingsActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), data.getMessage(), Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<ThemeImportTask.Result> loader) {

    }
}
