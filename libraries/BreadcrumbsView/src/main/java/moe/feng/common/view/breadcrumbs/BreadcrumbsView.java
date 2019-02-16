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
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;

public class BreadcrumbsView extends FrameLayout {

    /**
     * Internal implement of BreadcrumbsView
     */
    private RecyclerView mRecyclerView;
    private BreadcrumbsAdapter mAdapter;
    protected int currentTextColor;
    protected int defaultTextColor;

    /**
     * Popup Menu Theme Id
     */
    private int mPopupThemeId = -1;

    public BreadcrumbsView(Context context) {
        this(context, null);
    }

    public BreadcrumbsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BreadcrumbsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BreadcrumbsView, defStyleAttr, 0);
            mPopupThemeId = a.getResourceId(R.styleable.BreadcrumbsView_popupTheme, -1);
            currentTextColor = a.getColor(R.styleable.BreadcrumbsView_breadcrumbs_currentTextColor, ViewUtils.getColorFromAttr(context, android.R.attr.textColorPrimary));
            defaultTextColor = a.getColor(R.styleable.BreadcrumbsView_breadcrumbs_defaultTextColor, ViewUtils.getColorFromAttr(context, android.R.attr.textColorSecondary));
            a.recycle();
        } else {
            currentTextColor = ViewUtils.getColorFromAttr(context, android.R.attr.textColorPrimary);
            defaultTextColor = ViewUtils.getColorFromAttr(context, android.R.attr.textColorSecondary);
        }

        init();
    }

    /**
     * Init BreadcrumbsView
     */
    private void init() {
        // Init RecyclerView
        if (mRecyclerView == null) {
            ViewGroup.LayoutParams rvLayoutParams = new ViewGroup.LayoutParams(-1, -1);
            rvLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mRecyclerView = new RecyclerView(getContext());

            // Create Horizontal LinearLayoutManager
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    getContext(), LinearLayoutManager.HORIZONTAL, ViewUtils.isRtlLayout(getContext()));
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setOverScrollMode(OVER_SCROLL_NEVER);

            // Add RecyclerView
            addView(mRecyclerView, rvLayoutParams);
        }
        // Init Adapter
        if (mAdapter == null) {
            mAdapter = new BreadcrumbsAdapter(this);
            if (mPopupThemeId != -1) {
                mAdapter.setPopupThemeId(mPopupThemeId);
            }
        }
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Get breadcrumb items list
     *
     * @return Breadcrumb Items
     */
    public @Nullable
    ArrayList<BreadcrumbItem> getItems() {
        return mAdapter.getItems();
    }

    /**
     * Get current breadcrumb item
     *
     * @return Current item
     */
    public @Nullable
    BreadcrumbItem getCurrentItem() {
        if (mAdapter.getItems().size() <= 0) {
            return null;
        }
        return mAdapter.getItems().get(mAdapter.getItems().size() - 1);
    }

    /**
     * Set breadcrumb items list
     *
     * @param items Target list
     */
    public void setItems(@Nullable ArrayList<BreadcrumbItem> items) {
        mAdapter.setItems(items);
        mAdapter.notifyDataSetChanged();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
        }, 500);
    }

    /**
     * Notify which item has been changed to update text view
     *
     * @param index The item position
     */
    public void notifyItemChanged(int index) {
        mAdapter.notifyItemChanged(index * 2);
    }

    /**
     * Add a new item
     *
     * @param item New item
     */
    public void addItem(@NonNull BreadcrumbItem item) {
        int oldSize = mAdapter.getItemCount();
        mAdapter.getItems().add(item);
        mAdapter.notifyItemRangeInserted(oldSize, 2);
        mAdapter.notifyItemChanged(oldSize - 1);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
        }, 500);
    }

    /**
     * Remove items after a position
     *
     * @param afterPos The first position of the removing range
     */
    public void removeItemAfter(final int afterPos) {
        if (afterPos <= mAdapter.getItems().size() - 1) {
            int oldSize = mAdapter.getItemCount();
            while (mAdapter.getItems().size() > afterPos) {
                mAdapter.getItems().remove(mAdapter.getItems().size() - 1);
            }
            mAdapter.notifyItemRangeRemoved(afterPos * 2 - 1, oldSize - afterPos);
            /* Add delay time to fix animation */
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    int currentPos = afterPos * 2 - 1 - 1;
                    mAdapter.notifyItemChanged(currentPos);
                    mRecyclerView.smoothScrollToPosition(currentPos);
                }
            }, 100);
        }
    }

    /**
     * Remove last item
     */
    public void removeLastItem() {
        removeItemAfter(mAdapter.getItems().size() - 1);
    }

    /**
     * Set BreadcrumbsView callback (Recommend to use DefaultBreadcrumbsCallback)
     *
     * @param callback Callback should be set
     * @see BreadcrumbsCallback
     * @see DefaultBreadcrumbsCallback
     */
    public void setCallback(@Nullable BreadcrumbsCallback callback) {
        mAdapter.setCallback(callback);
    }

    /**
     * Get callback
     *
     * @return Callback
     * @see BreadcrumbsCallback
     */
    public @Nullable
    BreadcrumbsCallback getCallback() {
        return mAdapter.getCallback();
    }

    // Save/Restore View Instance State
    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        State state = new State(super.onSaveInstanceState(), getItems());
        bundle.putParcelable(State.STATE, state);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            State viewState = bundle.getParcelable(State.STATE);
            super.onRestoreInstanceState(viewState.getSuperState());
            setItems(viewState.getItems());
            return;
        }
        super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE);
    }

    protected static class State extends BaseSavedState {

        private static final String STATE = BreadcrumbsView.class.getSimpleName() + ".STATE";

        private final ArrayList<BreadcrumbItem> items;

        State(Parcelable superState, ArrayList<BreadcrumbItem> items) {
            super(superState);
            this.items = items;
        }

        ArrayList<BreadcrumbItem> getItems() {
            return this.items;
        }

    }

}
