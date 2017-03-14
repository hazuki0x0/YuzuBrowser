package jp.hazuki.yuzubrowser.bookmark.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.bookmark.BookmarkManager;
import jp.hazuki.yuzubrowser.bookmark.BookmarkSite;
import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.browser.openable.OpenUrl;
import jp.hazuki.yuzubrowser.browser.openable.OpenUrlList;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.ClipboardUtils;
import jp.hazuki.yuzubrowser.utils.WebUtils;
import jp.hazuki.yuzubrowser.utils.view.recycler.OnRecyclerListener;

import static android.app.Activity.RESULT_OK;


public class BookmarkFragment extends Fragment implements OnRecyclerListener {
    private static final String MODE_PICK = "pick";

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

        mManager = new BookmarkManager(BrowserApplication.getInstance());
        setList(mManager.getRoot());

        return rootView;
    }

    private void setList(BookmarkFolder folder) {
        mCurrentFolder = folder;
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(folder.title);
        }

        adapter = new BookmarkItemAdapter(getActivity(), folder.list, this);
        recyclerView.setAdapter(adapter);
    }

    public static Fragment newInstance(boolean pickMode) {
        Fragment fragment = new BookmarkFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(MODE_PICK, pickMode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onRecyclerItemClicked(View v, int position) {
        BookmarkItem item = mCurrentFolder.list.get(position);
        if (item instanceof BookmarkSite) {
            if (pickMode) {
                BookmarkSite site = (BookmarkSite) item;
                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_TITLE, site.title);
                intent.putExtra(Intent.EXTRA_TEXT, site.url);
                getActivity().setResult(RESULT_OK, intent);
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
        if (!pickMode) {
            menu.add(R.string.add_folder).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    new AddBookmarkFolderDialog(getActivity(), mManager, getString(R.string.new_folder_name), mCurrentFolder)
                            .setOnClickListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .show();
                    return false;
                }
            });
            menu.add(R.string.sort).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    boolean next = !adapter.isSortMode();
                    adapter.setSortMode(next);

                    Toast.makeText(getActivity(), (next) ? R.string.start_sort : R.string.end_sort, Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            menu.add(R.string.multi_select).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    boolean next = !adapter.isMultiSelectMode();
                    adapter.setMultiSelectMode(next);
                    return false;
                }
            });
        }
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
        if (adapter.isMultiSelectMode()) {
            inflater.inflate(R.menu.bookmark_multiselect_menu, menu.getMenu());
            bookmarkItem = null;
        } else if (mCurrentFolder.list.get(index) instanceof BookmarkSite) {
            inflater.inflate(R.menu.bookmark_site_menu, menu.getMenu());
            bookmarkItem = adapter.getItem(index);
        } else {
            inflater.inflate(R.menu.bookmark_folder_menu, menu.getMenu());
            bookmarkItem = adapter.getItem(index);
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
                                adapter.setMultiSelectMode(false);

                                mCurrentFolder.list.removeAll(selectedList);
                                mManager.write();
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                break;
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
