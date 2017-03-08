package jp.hazuki.yuzubrowser.history;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.view.AddBookmarkSiteDialog;
import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.browser.openable.OpenUrl;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.ClipboardUtils;
import jp.hazuki.yuzubrowser.utils.WebUtils;
import jp.hazuki.yuzubrowser.utils.database.ImplementedCursorLoader;
import jp.hazuki.yuzubrowser.utils.view.ExtendedExpandableListView;
import jp.hazuki.yuzubrowser.utils.view.ExtendedExpandableListView.OnChildLongClickListener;

public class BrowserHistoryActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private ExtendedExpandableListView listView;
    private BrowserHistoryAdapter mAdapter;
    private BrowserHistoryManager mManager;

    private boolean pickMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && Intent.ACTION_PICK.equals(getIntent().getAction())) {
            pickMode = true;
        }
        setContentView(R.layout.history_activity);
        listView = (ExtendedExpandableListView) findViewById(R.id.expandableListView);

        mManager = new BrowserHistoryManager(getApplicationContext());

        listView.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                sendUrl(v, AppData.newtab_history.get());
                return false;
            }
        });

        if (!pickMode) {
            listView.setOnChildLongClickListener(new OnChildLongClickListener() {
                @Override
                public boolean onChildLongClick(ExpandableListView parent, ContextMenu menu, View v, int groupPosition, int childPosition, long id) {
                    final String url = BrowserHistoryAdapter.convertViewToUrl(v);
                    final String title = BrowserHistoryAdapter.convertViewToTitle(v);

                    menu.add(R.string.open).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            sendUrl(url, BrowserManager.LOAD_URL_TAB_CURRENT);
                            return false;
                        }
                    });
                    menu.add(R.string.open_new).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            sendUrl(url, BrowserManager.LOAD_URL_TAB_NEW);
                            return false;
                        }
                    });
                    menu.add(R.string.open_bg).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            sendUrl(url, BrowserManager.LOAD_URL_TAB_BG);
                            return false;
                        }
                    });
                    menu.add(R.string.open_new_right).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            sendUrl(url, BrowserManager.LOAD_URL_TAB_NEW_RIGHT);
                            return false;
                        }
                    });
                    menu.add(R.string.open_bg_right).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            sendUrl(url, BrowserManager.LOAD_URL_TAB_BG_RIGHT);
                            return false;
                        }
                    });
                    menu.add(R.string.add_bookmark).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            new AddBookmarkSiteDialog(BrowserHistoryActivity.this, title, url).show();
                            return false;
                        }
                    });
                    menu.add(R.string.delete_history).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            new AlertDialog.Builder(BrowserHistoryActivity.this)
                                    .setTitle(R.string.confirm)
                                    .setMessage(R.string.confirm_delete_history)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mManager.delete(url);

                                            getSupportLoaderManager().restartLoader(0, null, BrowserHistoryActivity.this);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .show();
                            return false;
                        }
                    });
                    menu.add(R.string.share).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            WebUtils.shareWeb(BrowserHistoryActivity.this, url, title, null, null);
                            return false;
                        }
                    });
                    menu.add(R.string.copy_url).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            ClipboardUtils.setClipboardText(getApplicationContext(), url);
                            return false;
                        }
                    });

                    return false;
                }
            });
        }

        mAdapter = new BrowserHistoryAdapter(this, null);
        listView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void sendUrl(View v, int target) {
        if (pickMode) {
            sendPicked(BrowserHistoryAdapter.convertViewToTitle(v), BrowserHistoryAdapter.convertViewToUrl(v));
        } else {
            sendUrl(BrowserHistoryAdapter.convertViewToUrl(v), target);
        }
    }

    private void sendUrl(String url, int target) {
        if (url != null) {
            Intent intent = new Intent();
            intent.putExtra(BrowserManager.EXTRA_OPENABLE, new OpenUrl(url, target));
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private void sendPicked(String title, String url) {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new ImplementedCursorLoader(getApplicationContext(), mManager);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        mAdapter.swapCursor(c);
        if (!mAdapter.isEmpty()) {
            listView.expandGroup(0);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.delete_all_history).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(BrowserHistoryActivity.this)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_all_history)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mManager.deleteAll();

                                getSupportLoaderManager().restartLoader(0, null, BrowserHistoryActivity.this);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return false;
            }
        });
        menu.add(R.string.delete_all_favicon).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(BrowserHistoryActivity.this)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_all_favicon)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mManager.deleteFavicon();

                                getSupportLoaderManager().restartLoader(0, null, BrowserHistoryActivity.this);
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
