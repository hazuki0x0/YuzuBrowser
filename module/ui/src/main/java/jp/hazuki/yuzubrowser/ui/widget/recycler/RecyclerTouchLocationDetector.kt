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

package jp.hazuki.yuzubrowser.ui.widget.recycler

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class RecyclerTouchLocationDetector : RecyclerView.OnItemTouchListener {
    private var location: RecyclerTouchLocation = RecyclerTouchLocation.NONE

    val gravity: Int
        @SuppressLint("RtlHardcoded")
        get() {
            return when (location) {
                RecyclerTouchLocationDetector.RecyclerTouchLocation.LEFT -> Gravity.LEFT
                RecyclerTouchLocationDetector.RecyclerTouchLocation.RIGHT -> Gravity.RIGHT
                else -> Gravity.LEFT
            }
        }

    override fun onInterceptTouchEvent(rv: androidx.recyclerview.widget.RecyclerView, e: MotionEvent): Boolean {
        if (e.actionMasked == MotionEvent.ACTION_DOWN) {
            val half = rv.width / 2
            val x = e.x
            location = if (x <= half) RecyclerTouchLocation.LEFT else RecyclerTouchLocation.RIGHT
        }
        return false
    }

    override fun onTouchEvent(rv: androidx.recyclerview.widget.RecyclerView, e: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    private enum class RecyclerTouchLocation {
        NONE,
        LEFT,
        RIGHT
    }
}
