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
import jp.hazuki.yuzubrowser.tab.manager.TabManager;
import jp.hazuki.yuzubrowser.tab.manager.ThumbnailManager;
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

        setBackgroundColor(0xBB000000);
        bottomBar = findViewById(R.id.bottomBar);
    }

    public void setTabManager(final TabManager list, ThumbnailManager manager) {
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

        mAdapter = new TabListRecyclerAdapter(getContext(), list, manager, new TabListRecyclerAdapter.OnRecyclerListener() {
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
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mCallback.requestMoveTab(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            if (mAdapter.getItemCount() > 1) {
                viewHolder.itemView.setVisibility(GONE);
                mAdapter.notifyDataSetChanged();
                snackbar = Snackbar.make(bottomBar, ((TabListRecyclerAdapter.ViewHolder) viewHolder).title.getText(), Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewHolder.itemView.setVisibility(VISIBLE);
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                if (viewHolder.itemView.getVisibility() == GONE) {
                                    viewHolder.itemView.setVisibility(VISIBLE);
                                    mCallback.requestRemoveTab(viewHolder.getAdapterPosition());
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                snackbar.show();
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public interface Callback {
        void requestTabListClose();

        void requestMoveTab(int positionFrom, int positionTo);

        void requestRemoveTab(int no);

        void requestRemoveAllTab();

        void requestAddTab();

        void requestSelectTab(int no);

        void requestShowTabHistory(int no);
    }

    public void setCallback(Callback l) {
        mCallback = l;
    }
}
