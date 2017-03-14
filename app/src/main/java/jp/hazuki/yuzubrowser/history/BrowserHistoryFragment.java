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

import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.view.AddBookmarkSiteDialog;
import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.browser.openable.OpenUrl;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.ClipboardUtils;
import jp.hazuki.yuzubrowser.utils.WebUtils;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

import static android.app.Activity.RESULT_OK;


public class BrowserHistoryFragment extends Fragment implements OnRecyclerListener {
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
        adapter = new BrowserHistoryAdapter(getActivity(), manager, this);
        StickyHeaderDecoration decoration = new StickyHeaderDecoration(adapter);
        adapter.setDecoration(decoration);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(adapter);
        return v;
    }

    @Override
    public void onRecyclerItemClicked(View v, int position) {
        sendUrl(adapter.getItem(position), AppData.newtab_history.get());
    }

    @Override
    public boolean onRecyclerItemLongClicked(View v, final int position) {
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

                                    adapter.remove(position);
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
                    WebUtils.shareWeb(getActivity(), url, title);
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
        return true;
    }

    private void sendUrl(BrowserHistory history, int target) {
        if (pickMode) {
            sendPicked(history);
        } else {
            sendUrl(history.getUrl(), target);
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

    private void sendPicked(BrowserHistory history) {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TITLE, history.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, history.getUrl());
        intent.putExtra(Intent.EXTRA_STREAM, manager.getFaviconImage(history.getId()));
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
                    adapter.search(query);
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
                adapter.reLoad();
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

                                adapter.reLoad();
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
