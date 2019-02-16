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

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView

class OutSideClickableRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    private var click = false
    private var clickTime = 0L
    private var listener: (() -> Unit)? = null
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout()

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (findChildViewUnder(event.x, event.y) == null) {
                click = true
                clickTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_MOVE -> if (!pointInView(event.x, event.y, touchSlop)) click = false
            MotionEvent.ACTION_UP -> if (click && System.currentTimeMillis() - clickTime < longPressTimeout
                    && findChildViewUnder(event.x, event.y) == null) {
                listener?.invoke()
            }
            else -> click = false
        }
        return super.dispatchTouchEvent(event)
    }

    fun setOnOutSideClickListener(listener: (() -> Unit)?) {
        this.listener = listener
    }

    private fun pointInView(localX: Float, localY: Float, slop: Float): Boolean {
        return localX >= -slop && localY >= -slop && localX < right - left + slop &&
                localY < bottom - top + slop
    }
}
