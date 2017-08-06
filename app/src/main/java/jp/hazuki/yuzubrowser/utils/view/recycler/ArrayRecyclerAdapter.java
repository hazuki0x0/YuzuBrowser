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

package jp.hazuki.yuzubrowser.utils.view.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public abstract class ArrayRecyclerAdapter<T, VH extends ArrayRecyclerAdapter.ArrayViewHolder<T>> extends RecyclerView.Adapter<VH> {

    private List<T> items;
    private OnRecyclerListener recyclerListener;
    private LayoutInflater inflater;
    private boolean sortMode;

    private boolean multiSelectMode;
    private SparseBooleanArray itemSelected;

    public ArrayRecyclerAdapter(Context context, List<T> list, OnRecyclerListener listener) {
        itemSelected = new SparseBooleanArray();
        items = list;
        recyclerListener = listener;
        inflater = LayoutInflater.from(context);
        sortMode = false;
    }

    public void onBindViewHolder(VH holder, T item, int position) {
    }

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

    public int size() {
        return items.size();
    }

    public T get(int index) {
        return items.get(index);
    }

    public void add(T item) {
        items.add(item);
    }

    public void add(int index, T item) {
        items.add(index, item);
    }

    public void addAll(Collection<T> collections) {
        items.addAll(collections);
    }

    public T set(int index, T item) {
        return items.set(index, item);
    }

    public T remove(int index) {
        T item = items.remove(index);
        notifyItemRemoved(index);
        return item;
    }

    public boolean remove(T item) {
        int index = items.indexOf(item);
        if (index >= 0) {
            remove(index);
            return true;
        } else {
            return false;
        }
    }

    public void clear() {
        items.clear();
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
        int pos = holder.getAdapterPosition();
        if (items != null && items.size() > pos && items.get(pos) != null) {
            holder.setUp(items.get(pos));
            onBindViewHolder(holder, items.get(pos), pos);
        }
    }

    private void onItemClick(View v, int position, T item) {
        position = searchPosition(position, item);
        if (position < 0) return;
        if (multiSelectMode) {
            toggle(position);
        } else if (recyclerListener != null)
            recyclerListener.onRecyclerItemClicked(v, position);
    }

    private boolean onItemLongClick(View v, int position, T item) {
        position = searchPosition(position, item);
        return position >= 0 && !sortMode && recyclerListener != null && recyclerListener.onRecyclerItemLongClicked(v, position);
    }

    protected int searchPosition(int position, T item) {
        if (position < 0 || position >= getItemCount() || !get(position).equals(item)) {
            if (position > 0 && get(position - 1).equals(item))
                return position - 1;

            position = items.indexOf(item);
            if (position < 0) notifyDataSetChanged();
            return position;
        }
        return position;
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    public void setMultiSelectMode(boolean multiSelect) {
        if (multiSelect != multiSelectMode) {
            multiSelectMode = multiSelect;

            if (!multiSelect) {
                itemSelected.clear();
            }

            notifyDataSetChanged();
        }
    }

    public void toggle(int position) {
        setSelect(position, !itemSelected.get(position, false));
    }

    public void setSelect(int position, boolean isSelect) {
        boolean old = itemSelected.get(position, false);
        itemSelected.put(position, isSelect);

        if (old != isSelect) {
            notifyItemChanged(position);
        }
    }

    public boolean isSelected(int position) {
        return itemSelected.get(position, false);
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>();
        for (int i = 0; itemSelected.size() > i; i++) {
            if (itemSelected.valueAt(i)) {
                items.add(itemSelected.keyAt(i));
            }
        }
        return items;
    }

    public static class ArrayViewHolder<I> extends RecyclerView.ViewHolder {

        private I item;

        public ArrayViewHolder(View itemView, final ArrayRecyclerAdapter<I, ?> adapter) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onItemClick(v, getAdapterPosition(), item);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return adapter.onItemLongClick(v, getAdapterPosition(), item);
                }
            });
        }

        public void setUp(I item) {
            this.item = item;
        }

        protected I getItem() {
            return item;
        }
    }
}
