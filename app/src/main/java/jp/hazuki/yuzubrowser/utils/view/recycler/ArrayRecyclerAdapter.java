package jp.hazuki.yuzubrowser.utils.view.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;


public abstract class ArrayRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private List<T> items;
    private OnRecyclerListener recyclerListener;
    private LayoutInflater inflater;
    private boolean sortMode;

    public ArrayRecyclerAdapter(Context context, List<T> list, OnRecyclerListener listener) {
        items = list;
        recyclerListener = listener;
        inflater = LayoutInflater.from(context);
        sortMode = false;
    }

    public abstract void onBindViewHolder(VH holder, T item, int position);

    protected abstract VH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType);

    public void move(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int getItemCount() {
        if (items != null)
            return items.size();
        else
            return 0;
    }

    public List<T> getItems() {
        return items;
    }

    public T getItem(int index) {
        return items.get(index);
    }

    public T remove(int index) {
        return items.remove(index);
    }

    public OnRecyclerListener getListener() {
        return recyclerListener;
    }

    public boolean isSortMode() {
        return sortMode;
    }

    public void setSortMode(boolean sort) {
        if (sort != sortMode) {
            sortMode = sort;
            notifyDataSetChanged();
        }
    }

    protected void setRecyclerListener(OnRecyclerListener listener) {
        recyclerListener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return onCreateViewHolder(inflater, parent, viewType);
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        if (items != null && items.size() > position && items.get(position) != null) {
            onBindViewHolder(holder, items.get(position), position);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerListener != null)
                    recyclerListener.onRecyclerItemClicked(v, holder.getAdapterPosition());
            }
        });

        if (sortMode) {
            holder.itemView.setOnLongClickListener(null);
        } else {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return recyclerListener
                            .onRecyclerItemLongClicked(v, holder.getAdapterPosition());
                }
            });
        }
    }
}
