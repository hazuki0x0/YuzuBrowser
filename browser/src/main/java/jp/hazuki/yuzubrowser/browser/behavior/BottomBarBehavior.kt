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

package jp.hazuki.yuzubrowser.browser.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.appbar.AppBarLayout
import jp.hazuki.yuzubrowser.browser.R


class BottomBarBehavior(context: Context, attrs: AttributeSet) : androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior<LinearLayout>(context, attrs) {

    private var isInitialized = false
    private lateinit var topToolbar: View
    private lateinit var bottomToolbar: View
    private lateinit var bottomBar: View

    override fun layoutDependsOn(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, bottomBar: LinearLayout, dependency: View): Boolean {
        if (dependency is AppBarLayout) {
            this.bottomBar = bottomBar
            topToolbar = dependency.findViewById(R.id.topToolbar)
            bottomToolbar = bottomBar.findViewById(R.id.bottomOverlayToolbar)
            isInitialized = true
            return true
        }
        return false
    }

    override fun onDependentViewChanged(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, bottomBar: LinearLayout, dependency: View): Boolean {
        val bottomBarHeight = bottomToolbar.height

        if (topToolbar.height != 0) {
            val height = -dependency.top * bottomBarHeight / topToolbar.height

            bottomBar.translationY = Math.min(height, bottomBarHeight).toFloat()
        }
        return true
    }

    fun setExpanded(expanded: Boolean) {
        if (isInitialized && expanded) {
            bottomBar.translationY = 0f
        }
    }
}
