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
import jp.hazuki.yuzubrowser.tab.manager.TabIndexData;
import jp.hazuki.yuzubrowser.tab.manager.TabManager;
import jp.hazuki.yuzubrowser.utils.view.recycler.DividerItemDecoration;

public class TabListLayout extends LinearLayout {
    private TabListRecyclerAdapter mAdapter;
    private Callback mCallback;
    private Snackbar snackbar;
    private final View bottomBar;
    private boolean reverse = false;

    public TabListLayout(Context context) {
        this(context, null);
    }

    public TabListLayout(Context context, AttributeSet attrs) {
        this(context, attrs, false);
    }

    public TabListLayout(Context context, boolean shouldReverse) {
        this(context, null, shouldReverse);
    }

    public TabListLayout(Context context, AttributeSet attrs, boolean shouldReverse) {
        super(context, attrs);

        reverse = shouldReverse;

        LayoutInflater mLayoutInflater = LayoutInflater.from(context);
        if (reverse) {
            mLayoutInflater.inflate(R.layout.tab_list_reverse, this);
        } else {
            mLayoutInflater.inflate(R.layout.tab_list, this);
        }

        setBackgroundColor(0xcc222222);
        bottomBar = findViewById(R.id.bottomBar);
    }

    public void setTabManager(final TabManager list) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        if (reverse) {
            layoutManager.setStackFromEnd(true);
        }

        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper helper = new ItemTouchHelper(new ListTouch());
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(helper);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        mAdapter = new TabListRecyclerAdapter(getContext(), list, new TabListRecyclerAdapter.OnRecyclerListener() {
            @Override
            public void onRecyclerItemClicked(View v, int position) {
                mCallback.requestSelectTab(position);
                close();
            }

            @Override
            public void onCloseButtonClicked(View v, int position) {
                mCallback.requestRemoveTab(position);
                mAdapter.remove(position);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onHistoryButtonClicked(View v, int position) {
                mCallback.requestShowTabHistory(position);
            }
        });
        recyclerView.setAdapter(mAdapter);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.requestTabListClose();
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

    private class ListTouch extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) |
                    makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mCallback.requestMoveTab(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            mAdapter.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (mAdapter.getItemCount() > 1) {
                final int position = viewHolder.getAdapterPosition();
                final TabIndexData data = mAdapter.remove(position);
                mAdapter.notifyDataSetChanged();
                snackbar = Snackbar.make(bottomBar, getContext().getString(R.string.closed_tab,
                        ((TabListRecyclerAdapter.ViewHolder) viewHolder).title.getText()), Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAdapter.add(position, data);
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                if (event != DISMISS_EVENT_ACTION) {
                                    mCallback.requestRemoveTab(data);
                                }
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

        void requestRemoveTab(TabIndexData data);

        void requestAddTab();

        void requestSelectTab(int no);

        void requestShowTabHistory(int no);
    }

    public void setCallback(Callback l) {
        mCallback = l;
    }
}
