package jp.hazuki.yuzubrowser.utils.view.recycler;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.hazuki.yuzubrowser.R;

/**
 * Created by hazuki on 17/02/26.
 */

public abstract class RecyclerFabFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.recycler_with_fab, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddButtonClick();
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onAddButtonLongClick();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemTouchHelper helper = new ItemTouchHelper(new ListTouch());
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(helper);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));


        return rootView;
    }

    protected void onAddButtonClick() {

    }

    protected boolean onAddButtonLongClick() {
        return false;
    }

    public FloatingActionButton getFab() {
        return fab;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    protected void setRecyclerViewAdapter(RecyclerView.Adapter<?> adapter) {
        recyclerView.setAdapter(adapter);
    }

    public View getRootView() {
        return rootView;
    }

    public abstract boolean onMove(RecyclerView recyclerView, int fromIndex, int toIndex);

    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
    }

    public abstract void onSwiped(RecyclerView.ViewHolder viewHolder, int index);

    public boolean isLongPressDragEnabled() {
        return true;
    }

    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    public void setAddButtonEnabled(boolean enabled) {
        fab.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private class ListTouch extends ItemTouchHelper.Callback {


        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) |
                    makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return RecyclerFabFragment.this.onMove(recyclerView, viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }

        @Override
        public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
            RecyclerFabFragment.this.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            RecyclerFabFragment.this.onSwiped(viewHolder, viewHolder.getAdapterPosition());
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return RecyclerFabFragment.this.isLongPressDragEnabled();
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return RecyclerFabFragment.this.isItemViewSwipeEnabled();
        }
    }
}
