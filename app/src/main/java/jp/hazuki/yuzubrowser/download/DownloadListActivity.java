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

package jp.hazuki.yuzubrowser.download;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

import jp.hazuki.yuzubrowser.BrowserActivity;
import jp.hazuki.yuzubrowser.Constants;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.FileUtils;
import jp.hazuki.yuzubrowser.utils.PackageUtils;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;
import jp.hazuki.yuzubrowser.utils.database.ImplementedCursorLoader;
import jp.hazuki.yuzubrowser.utils.service.ServiceBindHelper;
import jp.hazuki.yuzubrowser.utils.service.ServiceConnectionHelper;

public class DownloadListActivity extends ThemeActivity implements LoaderCallbacks<Cursor>, ServiceConnectionHelper<Messenger> {
    //private static final String TAG = "DownloadListActivity";
    private ServiceBindHelper<Messenger> mServiceBindHelper;
    private Messenger mActivityMessenger;

    private ListView listView;
    private DownloadListAdapter mListAdapter;
    private DownloadInfoDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean fullscreen = AppData.fullscreen.get();
        int orientation = AppData.oritentation.get();
        if (getIntent() != null) {
            fullscreen = getIntent().getBooleanExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, fullscreen);
            orientation = getIntent().getIntExtra(Constants.intent.EXTRA_MODE_ORIENTATION, orientation);
        }

        if (fullscreen)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(orientation);

        setContentView(R.layout.download_activity);
        listView = (ListView) findViewById(R.id.listView);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) mListAdapter.getItem(position);
                switch (cursor.getInt(DownloadInfoDatabase.COLUMN_STATE_INDEX)) {
                    case DownloadInfo.STATE_DOWNLOADED:
                        try {
                            startActivity(PackageUtils.createFileOpenIntent(getApplicationContext(), cursor.getString(DownloadInfoDatabase.COLUMN_FILEPATH_INDEX)));
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), R.string.app_notfound, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });

        listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
                final int position = info.position;
                Cursor cursor = (Cursor) mListAdapter.getItem(position);
                final long id = cursor.getLong(DownloadInfoDatabase.COLUMN_ID_INDEX);
                final String url = cursor.getString(DownloadInfoDatabase.COLUMN_URL_INDEX);
                final String filePath = cursor.getString(DownloadInfoDatabase.COLUMN_FILEPATH_INDEX);

                switch (cursor.getInt(DownloadInfoDatabase.COLUMN_STATE_INDEX)) {
                    case DownloadInfo.STATE_DOWNLOADED:
                        menu.add(R.string.open_file).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                try {
                                    startActivity(PackageUtils.createFileOpenIntent(getApplicationContext(), filePath));
                                } catch (ActivityNotFoundException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), R.string.app_notfound, Toast.LENGTH_SHORT).show();
                                }
                                return false;
                            }
                        });
                        break;
                    case DownloadInfo.STATE_DOWNLOADING:
                        menu.add(R.string.cancel_download).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DownloadService.cancelDownload(mServiceBindHelper.getBinder(), mActivityMessenger, id);
                                return false;
                            }
                        });
                        break;
                }

                menu.add(R.string.open_url).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setClass(getApplicationContext(), BrowserActivity.class);
                        intent.putExtra(Intent.EXTRA_TEXT, url);
                        startActivity(intent);
                        finish();
                        return false;
                    }
                });

                menu.add(R.string.clear_download).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        mDb.delete(id);
                        getSupportLoaderManager().restartLoader(0, null, DownloadListActivity.this);
                        return false;
                    }
                });

                if (!TextUtils.isEmpty(filePath) &&
                        cursor.getInt(DownloadInfoDatabase.COLUMN_STATE_INDEX) == DownloadInfo.STATE_DOWNLOADED) {
                    final File file = new File(filePath);
                    if (file.exists()) {
                        menu.add(R.string.delete_download).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                FileUtils.deleteFile(file);
                                mDb.delete(id);
                                getSupportLoaderManager().restartLoader(0, null, DownloadListActivity.this);
                                return false;
                            }
                        });
                    }
                }
            }
        });

        mDb = DownloadInfoDatabase.getInstance(getApplicationContext());
        mActivityMessenger = new Messenger(new DownloadListActivityHandler(this));

        mServiceBindHelper = new ServiceBindHelper<>(getApplicationContext(), this);

        mListAdapter = new DownloadListAdapter(this, null);
        listView.setAdapter(mListAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    public void updateProgress(DownloadRequestInfo info) {
        if (mListAdapter != null) {
            mListAdapter.pushDownloadList(info);
            mListAdapter.notifyDataSetChanged();
        }
    }

    public void updateState() {
        if (mListAdapter != null) {
            getSupportLoaderManager().restartLoader(0, null, this);
        }
    }

    public void pushDownloadList(DownloadRequestInfo info) {
        if (mListAdapter != null) {
            mListAdapter.pushDownloadList(info);
        }
    }

    public void notifyDataSetChanged() {
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new ImplementedCursorLoader(getApplicationContext(), mDb);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        mListAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mListAdapter.swapCursor(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mServiceBindHelper.unbindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mServiceBindHelper.bindService(new Intent(getApplicationContext(), DownloadService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listView.setAdapter(null);
    }

    @Override
    public Messenger onBind(IBinder service) {
        Messenger messenger = DownloadService.registerObserver(service, mActivityMessenger);
        DownloadService.getDownloadInfo(messenger, mActivityMessenger);
        return messenger;
    }

    @Override
    public void onUnbind(Messenger service) {
        DownloadService.unregisterObserver(mServiceBindHelper.getBinder(), mActivityMessenger);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.delete_all_history).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(DownloadListActivity.this)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_all_history)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDb.deleteAllHistory();

                                getSupportLoaderManager().restartLoader(0, null, DownloadListActivity.this);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
