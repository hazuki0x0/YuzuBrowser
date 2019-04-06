/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.utils.view.tab;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.legacy.utils.graphics.DividerDrawable;
import jp.hazuki.yuzubrowser.ui.theme.ThemeData;

public class ScrollableTabLayout extends HorizontalScrollView implements TabLayout {
    private final TabController mController = new TabController() {
        @Override
        public void requestAddView(View view, int index) {
            mLayout.addView(view, index);
        }

        @Override
        public void requestRemoveViewAt(int id) {
            mLayout.removeViewAt(id);
        }

        @Override
        public DividerDrawable newDividerInstance() {
            return new DividerDrawable(getContext());
        }
    };
    private final LinearLayout mLayout;

    private Handler handler = new Handler();

    public ScrollableTabLayout(Context context) {
        this(context, null);
    }

    public ScrollableTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        mLayout = new LinearLayout(context);
        mLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        addView(mLayout);
    }

    @Override
    public void setOnTabClickListener(OnTabClickListener l) {
        mController.setOnTabClickListener(l);
    }

    @Override
    public void addTabView(View view, LinearLayout.LayoutParams params) {
        mController.addTabView(view);
        mLayout.addView(view, params);
    }

    @Override
    public void addTabView(int id, View view, LinearLayout.LayoutParams params) {
        mController.addTabView(id, view);
        mLayout.addView(view, id, params);
    }

    @Override
    public void setCurrentTab(int id) {
        mController.setCurrentTab(id);
    }

    @Override
    public void removeTabAt(final int id) {
        final int x = getScrollX();
        mController.removeTabAt(id);
        scrollTo(x, 0);
    }

    @Override
    public void setSense(int sense) {
        mController.setSense(sense);
    }

    private final Runnable mFullScrollRightRunnable = new Runnable() {
        @Override
        public void run() {
            fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        }
    };

    private final Runnable mFullScrollLeftRunnable = new Runnable() {
        @Override
        public void run() {
            fullScroll(HorizontalScrollView.FOCUS_LEFT);
        }
    };

    @Override
    public void fullScrollRight() {
        handler.post(mFullScrollRightRunnable);
    }

    @Override
    public void fullScrollLeft() {
        handler.post(mFullScrollLeftRunnable);
    }

    @Override
    public void scrollToPosition(final int position) {
        post(new Runnable() {
            @Override
            public void run() {
                int scrollX = getScrollX();
                View view = mLayout.getChildAt(position);
                if (view == null) {
                    if (mLayout.getChildCount() > 1)
                        view = mLayout.getChildAt(mLayout.getChildCount() - 1);
                    else
                        return;
                }
                int left = view.getLeft();
                int right = view.getRight();
                if (right <= scrollX || left >= getWidth() + scrollX)
                    smoothScrollTo(left, 0);
            }
        });
    }

    @Override
    public void swapTab(int a, int b) {
        mController.swapTab(a, b);
    }

    @Override
    public void moveTab(int from, int to, int new_curernt) {
        mController.moveTab(from, to, new_curernt);
    }

    @Override
    public void onPreferenceReset() {
        applyTheme(ThemeData.getInstance());
    }

    @Override
    public void applyTheme(ThemeData themedata) {
        mController.applyTheme(mLayout, themedata);
    }
}
