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

package jp.hazuki.yuzubrowser.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout

import jp.hazuki.yuzubrowser.core.utility.extensions.getBitmap
import jp.hazuki.yuzubrowser.ui.R

class PointerView(context: Context) : RelativeLayout(context) {
    //private static final String TAG = "PointerView";
    private val cursor: ImageView = ImageView(context)
    private var view: View? = null
    private var px = 0f
    private var py = 0f
    var backFinish = true
    private val mGestureDetector: MultiTouchGestureDetector

    private inner class MyMultiTouchGestureListener : MultiTouchGestureDetector.OnMultiTouchGestureListener {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            view?.run {
                val evDown = MotionEvent.obtain(e.downTime, e.eventTime, MotionEvent.ACTION_DOWN, px, py, 0)
                val evUp = MotionEvent.obtain(e.downTime, e.eventTime, MotionEvent.ACTION_UP, px, py, 0)

                dispatchTouchEvent(evDown)
                dispatchTouchEvent(evUp)
            }
            return false
        }

        override fun onShowPress(e: MotionEvent) {}

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (e2.pointerCount <= 1) {
                px -= distanceX
                py -= distanceY
                if (px < 0) {
                    px = 0f
                } else if (px > measuredWidth) {
                    px = measuredWidth.toFloat()
                }
                if (py < 0) {
                    py = 0f
                } else if (py > measuredHeight) {
                    py = measuredHeight.toFloat()
                }
                cursor.layout(px.toInt(), py.toInt(), px.toInt() + cursor.measuredWidth, py.toInt() + cursor.measuredHeight)
            } else {
                view?.dispatchTouchEvent(MotionEvent.obtain(e2.downTime, e2.eventTime, MotionEvent.ACTION_MOVE, e2.x, e2.y, 0))
            }
            return false
        }

        override fun onPointerUp(e: MotionEvent): Boolean {
            view?.run {
                if (e.pointerCount == 2)
                    dispatchTouchEvent(MotionEvent.obtain(e.downTime, e.eventTime, MotionEvent.ACTION_UP, e.x, e.y, 0))
            }
            return false
        }

        override fun onPointerDown(e: MotionEvent): Boolean {
            view?.run {
                if (e.pointerCount == 2)
                    dispatchTouchEvent(MotionEvent.obtain(e.downTime, e.eventTime, MotionEvent.ACTION_DOWN, e.x, e.y, 0))
            }
            return false
        }

        override fun onLongPress(e: MotionEvent) {
            view?.run {
                val time = SystemClock.uptimeMillis()
                val evDown = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, px, py, 0)

                dispatchTouchEvent(evDown)
            }
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) = false

        override fun onUp(e: MotionEvent) {}

        override fun onDown(e: MotionEvent) = false
    }

    init {
        addView(cursor)

        mGestureDetector = MultiTouchGestureDetector(context, MyMultiTouchGestureListener())

        val bitmap = context.getBitmap(R.drawable.ic_mouse_cursor)
        val matrix = Matrix()
        val dimen = resources.getDimension(R.dimen.dimen_cursor)
        matrix.postScale(dimen / bitmap.height, dimen / bitmap.height)
        cursor.setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true))
        bitmap.recycle()
        cursor.visibility = View.INVISIBLE

        post {
            px = (width / 2).toFloat()
            py = (height / 2).toFloat()
            requestLayout()
            cursor.visibility = View.VISIBLE
        }
    }

    public override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        cursor.layout(px.toInt(), py.toInt(), px.toInt() + cursor.measuredWidth, py.toInt() + cursor.measuredHeight)
    }

    fun setView(view: View?) {
        this.view = view
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mGestureDetector.onTouchEvent(event)
        return true
    }
}
