package jp.hazuki.yuzubrowser.utils.view.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

/**
 * Created by hazuki on 17/02/26.
 */

public abstract class ArrayRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private List<T> items;
    private OnRecyclerListener recyclerListener;
    private LayoutInflater inflater;

    public ArrayRecyclerAdapter(Context context, List<T> list, OnRecyclerListener listener) {
        items = list;
        recyclerListener = listener;
        inflater = LayoutInflater.from(context);
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

    public OnRecyclerListener getListener() {
        return recyclerListener;
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
                    recyclerListener.onRecyclerClicked(v, holder.getAdapterPosition());
            }
        });
    }
}
