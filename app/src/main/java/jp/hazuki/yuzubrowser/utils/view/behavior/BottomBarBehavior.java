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

package jp.hazuki.yuzubrowser.utils.view.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.R;


public class BottomBarBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    private View topToolbar;
    private View bottomToolbar;

    public BottomBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout bottomBar, View dependency) {
        if (dependency instanceof AppBarLayout) {
            topToolbar = dependency.findViewById(R.id.topToolbarLayout);
            bottomToolbar = bottomBar.findViewById(R.id.bottomToolbarLayout);
            return true;
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout bottomBar, View dependency) {
        int bottomBarHeight = bottomToolbar.getHeight();

        if (topToolbar.getHeight() != 0) {
            int height = -dependency.getTop() * bottomBarHeight / dependency.getHeight();

            bottomBar.setTranslationY(Math.min(height, bottomBarHeight));
        }
        return true;
    }
}
