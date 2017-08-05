package jp.hazuki.yuzubrowser.tab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.item.TabListSingleAction;
import jp.hazuki.yuzubrowser.tab.adapter.TabListRecyclerAdapterFactory;
import jp.hazuki.yuzubrowser.tab.adapter.TabListRecyclerBaseAdapter;
import jp.hazuki.yuzubrowser.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.tab.manager.TabManager;
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration;
import jp.hazuki.yuzubrowser.utils.view.templatepreserving.TemplatePreservingSnackBar;

public class TabListLayout extends LinearLayout {
    private TabListRecyclerBaseAdapter mAdapter;
    private TabManager tabManager;
    private Callback mCallback;
    private TemplatePreservingSnackBar snackbar;
    private final LinearLayout bottomBar;
    private boolean reverse = false;
    private boolean horizontal = false;

    private RemovedTab removedTab;

    public TabListLayout(Context context) {
        this(context, null);
    }

    public TabListLayout(Context context, AttributeSet attrs) {
        this(context, attrs, TabListSingleAction.MODE_NORMAL);
    }

    public TabListLayout(Context context, int mode, boolean left) {
        this(context, null, mode, left);
    }

    public TabListLayout(Context context, AttributeSet attrs, int mode) {
        this(context, attrs, mode, false);
    }

    @SuppressLint("RtlHardcoded")
    public TabListLayout(Context context, AttributeSet attrs, int mode, boolean left) {
        super(context.getApplicationContext(), attrs);

        reverse = mode == TabListSingleAction.MODE_REVERSE;
        horizontal = mode == TabListSingleAction.MODE_HORIZONTAL;

        LayoutInflater mLayoutInflater = LayoutInflater.from(context.getApplicationContext());
        if (horizontal) {
            mLayoutInflater.inflate(R.layout.tab_list_horizontal, this);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    close();
                }
            });
        } else if (reverse) {
            mLayoutInflater.inflate(R.layout.tab_list_reverse, this);
        } else {
            mLayoutInflater.inflate(R.layout.tab_list, this);
        }

        bottomBar = findViewById(R.id.bottomBar);

        if (left)
            bottomBar.setGravity(Gravity.LEFT);
    }

    public void setTabManager(final TabManager manager) {
        tabManager = manager;

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        if (horizontal) {
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        } else if (reverse) {
            layoutManager.setStackFromEnd(true);
        }

        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper helper = new ItemTouchHelper(new ListTouch());
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(helper);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        mAdapter = TabListRecyclerAdapterFactory.create(getContext(), tabManager, horizontal, new TabListRecyclerBaseAdapter.OnRecyclerListener() {
            @Override
            public void onRecyclerItemClicked(View v, int position) {
                if (snackbar != null)
                    snackbar.dismiss();

                mCallback.requestSelectTab(position);
                close();
            }

            @Override
            public void onCloseButtonClicked(View v, int position) {
                if (snackbar != null)
                    snackbar.dismiss();

                int size = tabManager.size();
                boolean current = position == tabManager.getCurrentTabNo();
                mCallback.requestRemoveTab(position, true);
                if (size != tabManager.size()) {
                    mAdapter.notifyItemRemoved(position);
                    if (current) {
                        mAdapter.notifyItemChanged(tabManager.getCurrentTabNo());
                    }
                }

            }

            @Override
            public void onHistoryButtonClicked(View v, int position) {
                if (snackbar != null)
                    snackbar.dismiss();

                mCallback.requestShowTabHistory(position);
            }
        });
        recyclerView.setAdapter(mAdapter);

        layoutManager.scrollToPosition(tabManager.getCurrentTabNo());

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        findViewById(R.id.newTabButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.requestAddTab();
                close();
            }
        });
    }

    public void close() {
        if (snackbar != null)
            snackbar.dismiss();
        mCallback.requestTabListClose();
    }

    public void closeSnackBar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    private class ListTouch extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (mAdapter.getItem(viewHolder.getAdapterPosition()).isPinning()) {
                if (horizontal) {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
                } else {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
                }
            } else {
                if (horizontal) {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.DOWN | ItemTouchHelper.UP) |
                            makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
                } else {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) |
                            makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
                }
            }
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
            }
            mCallback.requestMoveTab(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (mAdapter.getItemCount() > 1) {
                if (snackbar != null && snackbar.isShown()) {
                    snackbar.dismiss();
                }
                final int position = viewHolder.getAdapterPosition();

                boolean current = position == tabManager.getCurrentTabNo();

                removedTab = new RemovedTab(position, tabManager.get(position));

                mCallback.requestRemoveTab(position, false);

                mAdapter.notifyItemRemoved(position);

                if (current) {
                    mAdapter.notifyItemChanged(tabManager.getCurrentTabNo());
                }
                snackbar = TemplatePreservingSnackBar.make(bottomBar, getContext().getString(R.string.closed_tab),
                        ((TabListRecyclerBaseAdapter.ViewHolder) viewHolder).getTitle(), Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCallback.requestAddTab(removedTab.getIndex(), removedTab.getData());
                                mAdapter.notifyItemInserted(removedTab.getIndex());
                                removedTab = null;
                            }
                        })
                        .addCallback(new BaseTransientBottomBar.BaseCallback<TemplatePreservingSnackBar>() {
                            @Override
                            public void onDismissed(TemplatePreservingSnackBar transientBottomBar, int event) {
                                if (removedTab != null) {
                                    removedTab.destroy();
                                    removedTab = null;
                                }
                                snackbar = null;
                            }
                        });
                snackbar.show();
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return mAdapter.getItemCount() > 1;
        }
    }

    public interface Callback {
        void requestTabListClose();

        void requestMoveTab(int positionFrom, int positionTo);

        void requestRemoveTab(int no, boolean destroy);

        void requestAddTab();

        void requestSelectTab(int no);

        void requestShowTabHistory(int no);

        void requestAddTab(int index, MainTabData data);
    }

    public void setCallback(Callback l) {
        mCallback = l;
    }

    private static class RemovedTab {
        private final int index;
        private final MainTabData data;

        RemovedTab(int index, MainTabData data) {
            this.index = index;
            this.data = data;
        }

        int getIndex() {
            return index;
        }

        MainTabData getData() {
            return data;
        }

        void destroy() {
            data.mWebView.destroy();
        }
    }
}
