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

package com.google.android.material.appbar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat

class AppBarLayoutFixed @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppBarLayout(context, attrs), CoordinatorLayout.AttachedBehavior {
    private val behavior = Behavior(context, attrs)


    private var scrollRange: Int = -1

    private fun invalidateScrollRanges() {
        scrollRange = -1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        invalidateScrollRanges()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        invalidateScrollRanges()
    }

    override fun onWindowInsetChanged(insets: WindowInsetsCompat): WindowInsetsCompat {
        invalidateScrollRanges()
        return super.onWindowInsetChanged(insets)
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return behavior
    }

    override fun hasScrollableChildren(): Boolean {
        return totalScrollRangeFixed != 0
    }

    override fun getUpNestedPreScrollRange(): Int {
        return this.totalScrollRangeFixed
    }

    val totalScrollRangeFixed: Int
        @SuppressLint("VisibleForTests")
        get() {
            if (scrollRange != -1) {
                return scrollRange
            }

            var range = 0

            val z = this.childCount
            for (i in 0 until z) {
                val child = this.getChildAt(i)
                if (child.visibility != View.VISIBLE) {
                    continue
                }

                val lp = child.layoutParams as AppBarLayout.LayoutParams
                val childHeight = child.measuredHeight
                val flags = lp.scrollFlags
                if (flags and 1 == 0) {
                    break
                }

                range += childHeight + lp.topMargin + lp.bottomMargin
                if (flags and 2 != 0) {
                    range -= child.minimumHeight
                    break
                }
            }

            scrollRange = Math.max(0, range - topInset)

            return scrollRange
        }
}