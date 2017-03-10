package jp.hazuki.yuzubrowser.history;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.view.AddBookmarkSiteDialog;
import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.browser.openable.OpenUrl;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.ClipboardUtils;
import jp.hazuki.yuzubrowser.utils.WebUtils;

import static android.app.Activity.RESULT_OK;

/**
 * Created by hazuki on 17/03/10.
 */

public class BrowserHistoryFragment extends Fragment implements BrowserHistoryAdapter.OnHistoryItemListener {
    private static final String PICK_MODE = "pick";

    private boolean pickMode;
    private RecyclerView recyclerView;
    private BrowserHistoryAdapter adapter;
    private BrowserHistoryManager manager;

    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.recycler_view, container, false);
        pickMode = getArguments().getBoolean(PICK_MODE);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new BrowserHistoryScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                adapter.loadMore();
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        manager = new BrowserHistoryManager(getActivity());
        adapter = BrowserHistoryAdapter.create(getActivity(), manager, this);
        recyclerView.setAdapter(adapter);
        return v;
    }

    @Override
    public void onRecyclerClicked(View v, int position) {
        BrowserHistory history = adapter.getItem(position);
        sendUrl(history.getTitle(), history.getUrl(), AppData.newtab_history.get());
    }

    @Override
    public boolean onItemLongClick(View v, final int position) {
        if (!pickMode) {
            BrowserHistory history = adapter.getItem(position);
            final String url = history.getUrl();
            final String title = history.getTitle();

            PopupMenu popupMenu = new PopupMenu(getActivity(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(R.string.open).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    sendUrl(url, BrowserManager.LOAD_URL_TAB_CURRENT);
                    return false;
                }
            });
            menu.add(R.string.open_new).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    sendUrl(url, BrowserManager.LOAD_URL_TAB_NEW);
                    return false;
                }
            });
            menu.add(R.string.open_bg).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    sendUrl(url, BrowserManager.LOAD_URL_TAB_BG);
                    return false;
                }
            });
            menu.add(R.string.open_new_right).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    sendUrl(url, BrowserManager.LOAD_URL_TAB_NEW_RIGHT);
                    return false;
                }
            });
            menu.add(R.string.open_bg_right).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    sendUrl(url, BrowserManager.LOAD_URL_TAB_BG_RIGHT);
                    return false;
                }
            });
            menu.add(R.string.add_bookmark).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    new AddBookmarkSiteDialog(getActivity(), title, url).show();
                    return false;
                }
            });
            menu.add(R.string.delete_history).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.confirm)
                            .setMessage(R.string.confirm_delete_history)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    manager.delete(url);

                                    adapter.getItems().remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    return false;
                }
            });
            menu.add(R.string.share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    WebUtils.shareWeb(getActivity(), url, title, null, null);
                    return false;
                }
            });
            menu.add(R.string.copy_url).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    ClipboardUtils.setClipboardText(getActivity(), url);
                    return false;
                }
            });

            popupMenu.show();
        }
        return false;
    }

    private void sendUrl(String title, String url, int target) {
        if (pickMode) {
            sendPicked(title, url);
        } else {
            sendUrl(url, target);
        }
    }

    private void sendUrl(String url, int target) {
        if (url != null) {
            Intent intent = new Intent();
            intent.putExtra(BrowserManager.EXTRA_OPENABLE, new OpenUrl(url, target));
            getActivity().setResult(RESULT_OK, intent);
        }
        getActivity().finish();
    }

    private void sendPicked(String title, String url) {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    public boolean onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
            return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history, menu);

        MenuItem menuItem = menu.findItem(R.id.search_history);

        searchView = (SearchView) menuItem.getActionView();

        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                if (!TextUtils.isEmpty(query)) {
                    adapter = BrowserHistoryAdapter.create(getActivity(), manager, query, BrowserHistoryFragment.this);
                    recyclerView.setAdapter(adapter);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter = BrowserHistoryAdapter.create(getActivity(), manager, BrowserHistoryFragment.this);
                recyclerView.setAdapter(adapter);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_favicon:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_all_favicon)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                manager.deleteFavicon();
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            case R.id.delete_all_histories:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_all_history)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                manager.deleteAll();

                                adapter = adapter.reCreate(getActivity());
                                recyclerView.setAdapter(adapter);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static BrowserHistoryFragment newInstance(boolean isPickMode) {
        BrowserHistoryFragment fragment = new BrowserHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(PICK_MODE, isPickMode);
        fragment.setArguments(bundle);
        return fragment;
    }
}
