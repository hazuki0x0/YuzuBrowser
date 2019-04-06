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
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.legacy.utils.graphics.DividerDrawable;
import jp.hazuki.yuzubrowser.ui.theme.ThemeData;

public class FullTabLayout extends LinearLayout implements TabLayout {
    private final TabController mController = new TabController() {
        @Override
        public void requestAddView(View view, int index) {
            addView(view, index);
        }

        @Override
        public void requestRemoveViewAt(int id) {
            removeViewAt(id);
        }

        @Override
        public DividerDrawable newDividerInstance() {
            return new DividerDrawable(getContext());
        }
    };

    public FullTabLayout(Context context) {
        this(context, null);
    }

    public FullTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
    }

    @Override
    public void setOnTabClickListener(OnTabClickListener l) {
        mController.setOnTabClickListener(l);
    }

    @Override
    public void addTabView(View view, LayoutParams params) {
        mController.addTabView(view);
        params.weight = 1.0f;
        addView(view, params);
    }

    @Override
    public void addTabView(int id, View view, LayoutParams params) {
        mController.addTabView(id, view);
        addView(view, id, params);
    }

    @Override
    public void setCurrentTab(int id) {
        mController.setCurrentTab(id);
    }

    @Override
    public void removeTabAt(int id) {
        mController.removeTabAt(id);
    }

    @Override
    public void setSense(int sense) {
        mController.setSense(sense);
    }

    @Override
    public void fullScrollRight() {
        //do nothing
    }

    @Override
    public void fullScrollLeft() {
        //do nothing
    }

    @Override
    public void scrollToPosition(int position) {
        //do nothing
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
        mController.applyTheme(this, themedata);
    }
}
