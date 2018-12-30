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

package jp.hazuki.yuzubrowser.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

class CustomCoordinatorLayout : androidx.coordinatorlayout.widget.CoordinatorLayout {

    private var toolbarHeight = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setToolbarHeight(toolbarHeight: Int) {
        this.toolbarHeight = toolbarHeight
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (toolbarHeight > 0) {
            ev.y > toolbarHeight && super.onTouchEvent(ev)
        } else super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        super.onInterceptTouchEvent(ev)
        return false
    }
}
