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

package jp.hazuki.yuzubrowser.bookmark.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.bookmark.BookmarkManager;
import jp.hazuki.yuzubrowser.bookmark.BookmarkSite;
import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.browser.openable.OpenUrl;
import jp.hazuki.yuzubrowser.browser.openable.OpenUrlList;
import jp.hazuki.yuzubrowser.favicon.FaviconManager;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.ClipboardUtils;
import jp.hazuki.yuzubrowser.utils.PackageUtils;
import jp.hazuki.yuzubrowser.utils.WebUtils;
import jp.hazuki.yuzubrowser.utils.app.LongPressFixActivity;

import static android.app.Activity.RESULT_OK;


public class BookmarkFragment extends Fragment implements BookmarkItemAdapter.OnBookmarkRecyclerListener, ActionMode.Callback {
    private static final String MODE_PICK = "pick";
    private static final String ITEM_ID = "id";

    private boolean pickMode;

    private BookmarkItemAdapter adapter;
    private RecyclerView recyclerView;
    private BookmarkManager mManager;
    private BookmarkFolder mCurrentFolder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragent_bookark, container, false);
        setHasOptionsMenu(true);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemTouchHelper helper = new ItemTouchHelper(new Touch());
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(helper);

        pickMode = getArguments().getBoolean(MODE_PICK);

        mManager = new BookmarkManager(getContext());

        setList(getRoot());

        return rootView;
    }

    private BookmarkFolder getRoot() {
        long id = getArguments().getLong(ITEM_ID);
        if (AppData.save_bookmark_folder.get() || id > 0) {
            if (id < 1) {
                id = AppData.save_bookmark_folder_id.get();
            }
            BookmarkItem item = mManager.get(id);
            if (item instanceof BookmarkFolder) {
                return (BookmarkFolder) item;
            }
        }
        return mManager.getRoot();
    }

    private void setList(BookmarkFolder folder) {
        mCurrentFolder = folder;
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(folder.title);
        }

        adapter = new BookmarkItemAdapter(getActivity(), folder.list, pickMode, AppData.open_bookmark_new_tab.get(), this);
        recyclerView.setAdapter(adapter);
    }

    public static Fragment newInstance(boolean pickMode, long id) {
        Fragment fragment = new BookmarkFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(MODE_PICK, pickMode);
        bundle.putLong(ITEM_ID, id);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onRecyclerItemClicked(View v, int position) {
        BookmarkItem item = mCurrentFolder.list.get(position);
        if (item instanceof BookmarkSite) {
            if (pickMode) {
                pickBookmark((BookmarkSite) item);
            } else {
                sendUrl(((BookmarkSite) item).url, AppData.newtab_bookmark.get());
            }
            getActivity().finish();
        } else if (item instanceof BookmarkFolder) {
            setList((BookmarkFolder) item);
        } else {
            throw new IllegalStateException("Unknown BookmarkItem type");
        }
    }

    @Override
    public boolean onRecyclerItemLongClicked(View v, int position) {
        if (!adapter.isSortMode()) {
            showContextMenu(v, position);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onIconClick(View v, int position) {
        BookmarkItem item = adapter.get(position);
        if (item instanceof BookmarkSite) {
            sendUrl(((BookmarkSite) item).url, AppData.open_bookmark_icon_action.get());
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

    private void sendUrl(Collection<BookmarkItem> list, int target) {
        if (list != null) {
            ArrayList<String> urllist = new ArrayList<>();
            for (BookmarkItem item : list) {
                if (item instanceof BookmarkSite)
                    urllist.add(((BookmarkSite) item).url);
            }

            if (urllist.isEmpty())
                return;

            Intent intent = new Intent();
            intent.putExtra(BrowserManager.EXTRA_OPENABLE, new OpenUrlList(urllist, target));
            getActivity().setResult(RESULT_OK, intent);
        }
        getActivity().finish();
    }

    public boolean onBack() {
        if (adapter.isSortMode()) {
            adapter.setSortMode(false);
            Toast.makeText(getActivity(), R.string.end_sort, Toast.LENGTH_SHORT).show();
            return false;
        } else if (adapter.isMultiSelectMode()) {
            adapter.setMultiSelectMode(false);
            return false;
        }
        if (mCurrentFolder.parent != null) {
            setList(mCurrentFolder.parent);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!pickMode) inflater.inflate(R.menu.bookmark, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addFolder:
                new AddBookmarkFolderDialog(getActivity(), mManager, getString(R.string.new_folder_name), mCurrentFolder)
                        .setOnClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .show();
                return true;
            case R.id.sort: {
                boolean next = !adapter.isSortMode();
                adapter.setSortMode(next);

                Toast.makeText(getActivity(), (next) ? R.string.start_sort : R.string.end_sort, Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.multiSelect: {
                boolean next = !adapter.isMultiSelectMode();
                adapter.setMultiSelectMode(next);
                ((AppCompatActivity) getActivity()).startSupportActionMode(this);
                return true;
            }
        }
        return false;
    }

    private List<BookmarkItem> getSelectedBookmark(List<Integer> items) {
        List<BookmarkItem> bookmarkItems = new ArrayList<>();
        for (Integer item : items) {
            bookmarkItems.add(mCurrentFolder.list.get(item));
        }
        return bookmarkItems;
    }

    private void showContextMenu(View v, final int index) {
        PopupMenu menu = new PopupMenu(getActivity(), v);
        MenuInflater inflater = menu.getMenuInflater();
        final BookmarkItem bookmarkItem;
        if (pickMode) {
            bookmarkItem = adapter.get(index);
            menu.getMenu().add(R.string.select_this_item).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (bookmarkItem instanceof BookmarkSite) {
                        pickBookmark((BookmarkSite) bookmarkItem);
                    } else {
                        Intent sender = new Intent(getActivity(), BookmarkActivity.class);
                        sender.putExtra("id", bookmarkItem.getId());

                        Intent intent = new Intent();
                        intent.putExtra(Intent.EXTRA_TITLE, bookmarkItem.title);
                        intent.putExtra(Intent.EXTRA_TEXT, sender.toUri(Intent.URI_INTENT_SCHEME));
                        getActivity().setResult(RESULT_OK, intent);
                    }
                    getActivity().finish();
                    return false;
                }
            });
        } else {
            if (adapter.isMultiSelectMode()) {
                inflater.inflate(R.menu.bookmark_multiselect_menu, menu.getMenu());
                bookmarkItem = null;
            } else if (mCurrentFolder.list.get(index) instanceof BookmarkSite) {
                inflater.inflate(R.menu.bookmark_site_menu, menu.getMenu());
                bookmarkItem = adapter.get(index);
            } else {
                inflater.inflate(R.menu.bookmark_folder_menu, menu.getMenu());
                bookmarkItem = adapter.get(index);
            }
        }

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onContextMenuClick(item.getItemId(), bookmarkItem, index);
                return true;
            }
        });
        menu.show();
    }

    private void pickBookmark(BookmarkSite site) {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TITLE, site.title);
        intent.putExtra(Intent.EXTRA_TEXT, site.url);

        byte[] icon = adapter.getFavicon(site);
        if (icon != null) {
            intent.putExtra(Intent.EXTRA_STREAM, icon);
        }

        getActivity().setResult(RESULT_OK, intent);
    }

    private void onContextMenuClick(int id, final BookmarkItem item, final int index) {
        switch (id) {
            case R.id.open:
                sendUrl(((BookmarkSite) item).url, BrowserManager.LOAD_URL_TAB_CURRENT);
                break;
            case R.id.openNew:
                sendUrl(((BookmarkSite) item).url, BrowserManager.LOAD_URL_TAB_NEW);
                break;
            case R.id.openBg:
                sendUrl(((BookmarkSite) item).url, BrowserManager.LOAD_URL_TAB_BG);
                break;
            case R.id.openNewRight:
                sendUrl(((BookmarkSite) item).url, BrowserManager.LOAD_URL_TAB_NEW_RIGHT);
                break;
            case R.id.openBgRight:
                sendUrl(((BookmarkSite) item).url, BrowserManager.LOAD_URL_TAB_BG_RIGHT);
                break;
            case R.id.share:
                BookmarkSite site = (BookmarkSite) item;
                WebUtils.shareWeb(getActivity(), site.url, site.title);
                break;
            case R.id.copyUrl:
                ClipboardUtils.setClipboardText(getActivity(), ((BookmarkSite) item).url);
                break;
            case R.id.addToHome: {
                String url = ((BookmarkSite) item).url;
                Bitmap bitmap = FaviconManager.getInstance(getActivity()).get(url);
                Intent intent = PackageUtils.createShortCutIntent(getActivity(), item.title, url, bitmap);
                getActivity().sendBroadcast(intent);
                break;
            }
            case R.id.editBookmark:
                if (item instanceof BookmarkSite) {
                    new AddBookmarkSiteDialog(getActivity(), mManager, (BookmarkSite) item)
                            .setOnClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .show();
                } else if (item instanceof BookmarkFolder) {
                    new AddBookmarkFolderDialog(getActivity(), mManager, (BookmarkFolder) item)
                            .setOnClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .show();
                }
                break;
            case R.id.moveBookmark:
                new BookmarkFoldersDialog(getActivity(), mManager)
                        .setTitle(R.string.move_bookmark)
                        .setCurrentFolder(mCurrentFolder, item)
                        .setOnFolderSelectedListener(new BookmarkFoldersDialog.OnFolderSelectedListener() {
                            @Override
                            public boolean onFolderSelected(DialogInterface dialog, BookmarkFolder folder) {
                                mCurrentFolder.list.remove(index);
                                folder.add(item);

                                mManager.write();
                                adapter.notifyDataSetChanged();
                                return false;
                            }
                        })
                        .show();
                break;
            case R.id.moveUp:
                if (index > 0) {
                    Collections.swap(mCurrentFolder.list, index - 1, index);
                    mManager.write();
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.moveDown:
                if (index < mCurrentFolder.list.size() - 1) {
                    Collections.swap(mCurrentFolder.list, index + 1, index);
                    mManager.write();
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.deleteBookmark:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_bookmark)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCurrentFolder.list.remove(index);
                                mManager.write();
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                break;
            case R.id.openAllNew: {
                List<BookmarkItem> items;
                if (item instanceof BookmarkFolder) {
                    items = ((BookmarkFolder) item).list;
                } else {
                    items = getSelectedBookmark(adapter.getSelectedItems());
                }
                sendUrl(items, BrowserManager.LOAD_URL_TAB_NEW);
                break;
            }
            case R.id.openAllBg: {
                List<BookmarkItem> items;
                if (item instanceof BookmarkFolder) {
                    items = ((BookmarkFolder) item).list;
                } else {
                    items = getSelectedBookmark(adapter.getSelectedItems());
                }
                sendUrl(items, BrowserManager.LOAD_URL_TAB_BG);
                break;
            }
            case R.id.moveAllBookmark:
                final List<BookmarkItem> bookmarkItems = getSelectedBookmark(adapter.getSelectedItems());
                adapter.setMultiSelectMode(false);
                new BookmarkFoldersDialog(getActivity(), mManager)
                        .setTitle(R.string.move_bookmark)
                        .setCurrentFolder(mCurrentFolder, bookmarkItems)
                        .setOnFolderSelectedListener(new BookmarkFoldersDialog.OnFolderSelectedListener() {
                            @Override
                            public boolean onFolderSelected(DialogInterface dialog, BookmarkFolder folder) {
                                mCurrentFolder.list.removeAll(bookmarkItems);
                                folder.addAll(bookmarkItems);

                                mManager.write();
                                adapter.notifyDataSetChanged();
                                return false;
                            }
                        })
                        .show();
                break;
            case R.id.deleteAllBookmark:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_bookmark)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<BookmarkItem> selectedList = getSelectedBookmark(adapter.getSelectedItems());

                                mCurrentFolder.list.removeAll(selectedList);
                                mManager.write();

                                adapter.setMultiSelectMode(false);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                break;
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.bookmark_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.openAllNew: {
                List<BookmarkItem> items;
                if (item instanceof BookmarkFolder) {
                    items = ((BookmarkFolder) item).list;
                } else {
                    items = getSelectedBookmark(adapter.getSelectedItems());
                }
                sendUrl(items, BrowserManager.LOAD_URL_TAB_NEW);
                return true;
            }
            case R.id.openAllBg: {
                List<BookmarkItem> items;
                if (item instanceof BookmarkFolder) {
                    items = ((BookmarkFolder) item).list;
                } else {
                    items = getSelectedBookmark(adapter.getSelectedItems());
                }
                sendUrl(items, BrowserManager.LOAD_URL_TAB_BG);
                return true;
            }
            case R.id.selectAll:
                for (int i = 0; adapter.getItemCount() > i; i++) {
                    adapter.setSelect(i, true);
                }
                return true;
            case R.id.moveAllBookmark:
                final List<BookmarkItem> bookmarkItems = getSelectedBookmark(adapter.getSelectedItems());
                new BookmarkFoldersDialog(getActivity(), mManager)
                        .setTitle(R.string.move_bookmark)
                        .setCurrentFolder(mCurrentFolder, bookmarkItems)
                        .setOnFolderSelectedListener(new BookmarkFoldersDialog.OnFolderSelectedListener() {
                            @Override
                            public boolean onFolderSelected(DialogInterface dialog, BookmarkFolder folder) {
                                mCurrentFolder.list.removeAll(bookmarkItems);
                                folder.addAll(bookmarkItems);

                                mManager.write();
                                mode.finish();
                                return false;
                            }
                        })
                        .show();
                return true;
            case R.id.deleteAllBookmark:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_delete_bookmark)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<BookmarkItem> selectedList = getSelectedBookmark(adapter.getSelectedItems());

                                mCurrentFolder.list.removeAll(selectedList);
                                mManager.write();

                                mode.finish();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        ((LongPressFixActivity) getActivity()).onDestroyActionMode();
        adapter.setMultiSelectMode(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (AppData.save_bookmark_folder.get()) {
            AppData.save_bookmark_folder_id.set(mCurrentFolder.getId());
            AppData.commit(getActivity(), AppData.save_bookmark_folder_id);
        }
    }

    private class Touch extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            adapter.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
            mManager.write();
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return adapter.isSortMode();
        }
    }
}
