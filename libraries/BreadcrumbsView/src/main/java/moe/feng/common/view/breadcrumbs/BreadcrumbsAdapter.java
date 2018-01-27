/*
 * Copyright (C) 2017-2018 Hazuki
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

package moe.feng.common.view.breadcrumbs;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;

class BreadcrumbsAdapter extends RecyclerView.Adapter<BreadcrumbsAdapter.ItemHolder> {

    private final int DROPDOWN_OFFSET_Y_FIX;

    private ArrayList<BreadcrumbItem> items;
    private BreadcrumbsCallback callback;

    private BreadcrumbsView parent;

    private int mPopupThemeId = -1;

    public BreadcrumbsAdapter(BreadcrumbsView parent) {
        this(parent, new ArrayList<BreadcrumbItem>());
    }

    public BreadcrumbsAdapter(BreadcrumbsView parent, ArrayList<BreadcrumbItem> items) {
        this.parent = parent;
        this.items = items;
        DROPDOWN_OFFSET_Y_FIX = parent.getResources().getDimensionPixelOffset(R.dimen.dropdown_offset_y_fix_value);
    }

    public @NonNull
    ArrayList<BreadcrumbItem> getItems() {
        return this.items;
    }

    public void setItems(@NonNull ArrayList<BreadcrumbItem> items) {
        this.items = items;
    }

    public void setCallback(@Nullable BreadcrumbsCallback callback) {
        this.callback = callback;
    }

    public @Nullable
    BreadcrumbsCallback getCallback() {
        return this.callback;
    }

    public void setPopupThemeId(@IdRes int popupThemeId) {
        this.mPopupThemeId = popupThemeId;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == R.layout.breadcrumbs_view_item_arrow) {
            return new ArrowIconHolder(inflater.inflate(viewType, parent, false));
        } else if (viewType == R.layout.breadcrumbs_view_item_text) {
            return new BreadcrumbItemHolder(inflater.inflate(viewType, parent, false));
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        int viewType = getItemViewType(position);
        int truePos = viewType == R.layout.breadcrumbs_view_item_arrow ? ((position - 1) / 2) + 1 : position / 2;
        holder.setItem(items.get(truePos));
    }

    @Override
    public int getItemCount() {
        return (items != null && !items.isEmpty()) ? (items.size() * 2 - 1) : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 == 1 ? R.layout.breadcrumbs_view_item_arrow : R.layout.breadcrumbs_view_item_text;
    }

    class BreadcrumbItemHolder extends ItemHolder<BreadcrumbItem> {

        Button button;

        BreadcrumbItemHolder(View itemView) {
            super(itemView);
            button = (Button) itemView;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) {
                        callback.onItemClick(parent, getAdapterPosition() / 2);
                    }
                }
            });
        }

        @Override
        public void setItem(@NonNull BreadcrumbItem item) {
            super.setItem(item);
            button.setText(item.getSelectedItem());
            button.setTextColor(
                    getAdapterPosition() == getItemCount() - 1
                            ? parent.currentTextColor : parent.defaultTextColor
            );
        }

    }

    class ArrowIconHolder extends ItemHolder<BreadcrumbItem> {

        ImageButton imageButton;
        ListPopupWindow popupWindow;

        ArrowIconHolder(View itemView) {
            super(itemView);
            imageButton = (ImageButton) itemView;
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.hasMoreSelect()) {
                        try {
                            popupWindow.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            imageButton.setColorFilter(parent.defaultTextColor);

            createPopupWindow();
        }

        @Override
        public void setItem(@NonNull BreadcrumbItem item) {
            super.setItem(item);
            imageButton.setClickable(item.hasMoreSelect());
            if (item.hasMoreSelect()) {
                List<Map<String, String>> list = new ArrayList<>();
                for (Object obj : item.getItems()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("text", obj.toString());
                    list.add(map);
                }
                // Kotlin: item.getItems().map { "text" to it.toString() }
                ListAdapter adapter = new SimpleAdapter(getPopupThemedContext(), list, R.layout.breadcrumbs_view_dropdown_item, new String[]{"text"}, new int[]{android.R.id.text1});
                popupWindow.setAdapter(adapter);
                popupWindow.setWidth(ViewUtils.measureContentWidth(getPopupThemedContext(), adapter));
                imageButton.setOnTouchListener(popupWindow.createDragToOpenListener(imageButton));
            } else {
                imageButton.setOnTouchListener(null);
            }
        }

        private void createPopupWindow() {
            popupWindow = new ListPopupWindow(getPopupThemedContext());
            popupWindow.setAnchorView(imageButton);
            popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (callback != null) {
                        callback.onItemChange(parent, getAdapterPosition() / 2, getItems().get(getAdapterPosition() / 2 + 1).getItems().get(i));
                        popupWindow.dismiss();
                    }
                }
            });
            imageButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    popupWindow.setVerticalOffset(-imageButton.getMeasuredHeight() + DROPDOWN_OFFSET_Y_FIX);
                    imageButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

    }

    class ItemHolder<T> extends RecyclerView.ViewHolder {

        T item;

        ItemHolder(View itemView) {
            super(itemView);
        }

        public void setItem(@NonNull T item) {
            this.item = item;
        }

        Context getContext() {
            return itemView.getContext();
        }

        Context getPopupThemedContext() {
            return mPopupThemeId != -1 ? new ContextThemeWrapper(getContext(), mPopupThemeId) : getContext();
        }

    }

}
