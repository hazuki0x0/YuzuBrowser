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

package jp.hazuki.yuzubrowser.ui.widget.swipebutton

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

open class SwipeController(context: Context) {

    protected var currentWhatNo = SWIPE_PRESS
        private set
    private var sense: Int = 0

    private val detector: GestureDetector

    private var listener: OnChangeListener? = null

    interface OnChangeListener {
        fun onEventOutSide(): Boolean

        fun onEventCancel(): Boolean

        fun onEventActionUp(whatNo: Int): Boolean

        fun onEventActionDown(): Boolean

        fun onChangeState(whatNo: Int)

        fun onLongPress()
    }

    fun setOnChangeListener(l: OnChangeListener) {
        listener = l
    }

    init {
        detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                val rangeX = (e2.rawX - e1.rawX).toInt()
                val rangeY = (e2.rawY - e1.rawY).toInt()

                var check = 0
                if (rangeX > sense)
                    check = check or SWIPE_RIGHT
                else if (rangeX < -sense)
                    check = check or SWIPE_LEFT
                if (rangeY > sense)
                    check = check or SWIPE_DOWN
                else if (rangeY < -sense)
                    check = check or SWIPE_UP

                if (check == 0) {
                    if (currentWhatNo != SWIPE_CANCEL) {
                        currentWhatNo = SWIPE_CANCEL
                        listener?.onChangeState(SWIPE_CANCEL)
                    }
                } else {
                    when (check and SWIPE_X_Y) {
                        SWIPE_RIGHT -> callListener(SWIPE_RIGHT)
                        SWIPE_LEFT -> callListener(SWIPE_LEFT)
                        SWIPE_DOWN -> callListener(SWIPE_DOWN)
                        SWIPE_UP -> callListener(SWIPE_UP)
                        SWIPE_RIGHT or SWIPE_DOWN -> callListener(if (rangeX > rangeY) SWIPE_RIGHT else SWIPE_DOWN)
                        SWIPE_RIGHT or SWIPE_UP -> callListener(if (rangeX > -rangeY) SWIPE_RIGHT else SWIPE_UP)
                        SWIPE_LEFT or SWIPE_DOWN -> callListener(if (-rangeX > rangeY) SWIPE_LEFT else SWIPE_DOWN)
                        SWIPE_LEFT or SWIPE_UP -> callListener(if (-rangeX > -rangeY) SWIPE_LEFT else SWIPE_UP)
                    }
                }

                return super.onScroll(e1, e2, distanceX, distanceY)
            }

            private fun callListener(what: Int) {
                if (currentWhatNo != what) {
                    currentWhatNo = what
                    listener?.onChangeState(what)
                }
            }

            override fun onLongPress(e: MotionEvent) {
                if (currentWhatNo == SWIPE_PRESS) {
                    currentWhatNo = SWIPE_LPRESS
                    onEventLongPress()
                    listener?.run {
                        onChangeState(currentWhatNo)
                        onLongPress()
                    }
                }
            }
        })
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                onEventActionDown()
                listener?.let { return it.onEventActionDown() }
            }
            MotionEvent.ACTION_UP -> {
                val tmpNo = currentWhatNo
                currentWhatNo = SWIPE_PRESS
                var ret = false
                onEventActionUp(tmpNo)
                listener?.let { ret = it.onEventActionUp(tmpNo) }
                return ret
            }
            MotionEvent.ACTION_CANCEL -> {
                onEventCancel()
                listener?.let { return it.onEventCancel() }
            }
            MotionEvent.ACTION_OUTSIDE -> {
                onEventOutSide()
                listener?.let { return it.onEventOutSide() }
            }
        }
        return false
    }

    fun notifyChangeState() {
        listener?.onChangeState(currentWhatNo)
    }

    fun setToDefault() {
        currentWhatNo = SWIPE_PRESS
        listener?.onChangeState(currentWhatNo)
    }

    fun setSense(sense: Int) {
        this.sense = sense
    }

    protected open fun onEventActionDown() {}

    protected open fun onEventActionUp(whatNo: Int) {}

    protected open fun onEventLongPress() {}

    protected open fun onEventCancel() {}

    protected open fun onEventOutSide() {}

    companion object {
        const val SWIPE_CANCEL = 0x00
        const val SWIPE_RIGHT = 0x01
        const val SWIPE_LEFT = 0x02
        const val SWIPE_UP = 0x04
        const val SWIPE_DOWN = 0x08
        const val SWIPE_X_Y = SWIPE_RIGHT or SWIPE_LEFT or SWIPE_UP or SWIPE_DOWN
        const val SWIPE_PRESS = 0x10
        const val SWIPE_LPRESS = 0x20
    }
}
