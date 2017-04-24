package jp.hazuki.yuzubrowser.tab;

import android.content.Context;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.item.TabListSingleAction;
import jp.hazuki.yuzubrowser.tab.manager.TabIndexData;
import jp.hazuki.yuzubrowser.tab.manager.TabManager;
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration;

public class TabListLayout extends LinearLayout {
    private TabListRecyclerAdapter mAdapter;
    private TabManager tabManager;
    private Callback mCallback;
    private Snackbar snackbar;
    private final View bottomBar;
    private boolean reverse = false;
    private boolean horizontal = false;

    private boolean changeCurrent;
    private int oldCurrent;

    public TabListLayout(Context context) {
        this(context, null);
    }

    public TabListLayout(Context context, AttributeSet attrs) {
        this(context, attrs, TabListSingleAction.MODE_NORMAL);
    }

    public TabListLayout(Context context, int mode) {
        this(context, null, mode);
    }

    public TabListLayout(Context context, AttributeSet attrs, int mode) {
        super(context, attrs);

        reverse = mode == TabListSingleAction.MODE_REVERSE;
        horizontal = mode == TabListSingleAction.MODE_HORIZONTAL;

        LayoutInflater mLayoutInflater = LayoutInflater.from(context);
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
    }

    public void setTabManager(final TabManager manager) {
        tabManager = manager;

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

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

        mAdapter = new TabListRecyclerAdapter(getContext(), manager, horizontal, new TabListRecyclerAdapter.OnRecyclerListener() {
            @Override
            public void onRecyclerItemClicked(View v, int position) {
                mCallback.requestSelectTab(position);
                close();
            }

            @Override
            public void onCloseButtonClicked(View v, int position) {
                mCallback.requestRemoveTab(position);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onHistoryButtonClicked(View v, int position) {
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

    private void deleteHideItem() {
        TabIndexData data = tabManager.unHideItem();
        if (changeCurrent)
            tabManager.setCurrentTab(oldCurrent);
        changeCurrent = false;
        if (data != null)
            mCallback.requestRemoveTab(tabManager.indexOf(data.getId()));
    }

    private class ListTouch extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (horizontal) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.DOWN | ItemTouchHelper.UP) |
                        makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            } else {
                return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) |
                        makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if (snackbar != null && snackbar.isShown()) {
                deleteHideItem();
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
                    deleteHideItem();
                    snackbar.dismiss();
                }
                int position = viewHolder.getAdapterPosition();

                if (tabManager.getCurrentTabNo() == position) {
                    mCallback.requestSelectTab(position == tabManager.size() - 1 ? position - 1 : position + 1);
                }

                oldCurrent = tabManager.getCurrentTabNo();

                if ((position < oldCurrent && oldCurrent > 0)) {
                    tabManager.setCurrentTab(oldCurrent - 1);
                    changeCurrent = true;
                } else {
                    changeCurrent = false;
                }

                if (!tabManager.hideItem(position) && changeCurrent) {
                    tabManager.setCurrentTab(oldCurrent);
                }
                mAdapter.notifyDataSetChanged();
                snackbar = Snackbar.make(bottomBar, getContext().getString(R.string.closed_tab,
                        ((TabListRecyclerAdapter.ViewHolder) viewHolder).title.getText()), Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                tabManager.unHideItem();
                                if (changeCurrent)
                                    tabManager.setCurrentTab(oldCurrent);
                                changeCurrent = false;
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                if (event != DISMISS_EVENT_ACTION && tabManager.isHideItem()) {
                                    deleteHideItem();
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

        void requestRemoveTab(int no);

        void requestAddTab();

        void requestSelectTab(int no);

        void requestShowTabHistory(int no);
    }

    public void setCallback(Callback l) {
        mCallback = l;
    }
}
