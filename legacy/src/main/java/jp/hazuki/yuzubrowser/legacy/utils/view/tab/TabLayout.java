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

import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.ui.theme.ThemeData;

public interface TabLayout {
    interface OnTabClickListener {
        void onTabDoubleClick(int id);

        void onTabChangeClick(int from, int to);

        void onTabLongClick(int id);

        boolean onTabTouch(View v, MotionEvent ev, int id, boolean selected);

        void onChangeCurrentTab(int from, int to);

        void onTabSwipeUp(int id);

        void onTabSwipeDown(int id);
    }

    void setOnTabClickListener(OnTabClickListener l);

    void addTabView(View view, LinearLayout.LayoutParams params);

    void addTabView(int id, View view, LinearLayout.LayoutParams params);

    void setCurrentTab(int id);

    void removeTabAt(int id);

    void setSense(int sense);

    void fullScrollRight();

    void fullScrollLeft();

    void scrollToPosition(int position);

    void swapTab(int a, int b);

    void moveTab(int from, int to, int new_curernt);

    void onPreferenceReset();

    void applyTheme(ThemeData themedata);
}
