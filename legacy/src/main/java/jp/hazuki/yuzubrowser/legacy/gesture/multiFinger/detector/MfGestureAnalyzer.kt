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

package jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.detector

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs

internal class MfGestureAnalyzer(context: Context) {
    private var sensitivity: Int = context.convertDpToPx(AppPrefs.multi_finger_gesture_sensitivity.get())

    private val dX = DoubleArray(5)
    private val dY = DoubleArray(5)
    private val iX = DoubleArray(5)
    private val iY = DoubleArray(5)

    private var startTime: Long = 0
    var fingers = 0
        private set

    private val gestureFlag: Int
        get() {
            return when (fingers) {
                1 -> {
                    when {
                        -dY[0] > Math.abs(dX[0]) * 2.0 -> MultiFingerGestureDetector.SWIPE_UP
                        dY[0] > Math.abs(dX[0]) * 2.0 -> MultiFingerGestureDetector.SWIPE_DOWN
                        -dX[0] > Math.abs(dY[0]) * 2.0 -> MultiFingerGestureDetector.SWIPE_LEFT
                        dX[0] > Math.abs(dY[0]) * 2.0 -> MultiFingerGestureDetector.SWIPE_RIGHT
                        else -> MultiFingerGestureDetector.SWIPE_UNKNOWN
                    }
                }
                2 -> {
                    when {
                        -dY[0] > Math.abs(dX[0]) * 2.0 && -dY[1] > Math.abs(dX[1]) * 2.0 -> MultiFingerGestureDetector.SWIPE_UP
                        dY[0] > Math.abs(dX[0]) * 2.0 && dY[1] > Math.abs(dX[1]) * 2.0 -> MultiFingerGestureDetector.SWIPE_DOWN
                        -dX[0] > Math.abs(dY[0]) * 2.0 && -dX[1] > Math.abs(dY[1]) * 2.0 -> MultiFingerGestureDetector.SWIPE_LEFT
                        dX[0] > Math.abs(dY[0]) * 2.0 && dX[1] > Math.abs(dY[1]) * 2.0 -> MultiFingerGestureDetector.SWIPE_RIGHT
                        else -> MultiFingerGestureDetector.SWIPE_UNKNOWN
                    }
                }
                3 -> {
                    when {
                        -dY[0] > Math.abs(dX[0]) * 2.0 && -dY[1] > Math.abs(dX[1]) * 2.0
                                && -dY[2] > Math.abs(dX[2]) * 2.0 -> MultiFingerGestureDetector.SWIPE_UP
                        dY[0] > Math.abs(dX[0]) * 2.0 && dY[1] > Math.abs(dX[1]) * 2.0
                                && dY[2] > Math.abs(dX[2]) * 2.0 -> MultiFingerGestureDetector.SWIPE_DOWN
                        -dX[0] > Math.abs(dY[0]) * 2.0 && -dX[1] > Math.abs(dY[1]) * 2.0
                                && -dX[2] > Math.abs(dY[2]) * 2.0 -> MultiFingerGestureDetector.SWIPE_LEFT
                        dX[0] > Math.abs(dY[0]) * 2.0 && dX[1] > Math.abs(dY[1]) * 2.0
                                && dX[2] > Math.abs(dY[2]) * 2.0 -> MultiFingerGestureDetector.SWIPE_RIGHT
                        else -> MultiFingerGestureDetector.SWIPE_UNKNOWN
                    }
                }
                4 -> {
                    when {
                        -dY[0] > Math.abs(dX[0]) * 2.0 && -dY[1] > Math.abs(dX[1]) * 2.0
                                && -dY[2] > Math.abs(dX[2]) * 2.0 && -dY[3] > Math.abs(dX[3]) * 2.0 -> MultiFingerGestureDetector.SWIPE_UP
                        dY[0] > Math.abs(dX[0]) * 2.0 && dY[1] > Math.abs(dX[1]) * 2.0
                                && dY[2] > Math.abs(dX[2]) * 2.0 && dY[3] > Math.abs(dX[3]) * 2.0 -> MultiFingerGestureDetector.SWIPE_DOWN
                        -dX[0] > Math.abs(dY[0]) * 2.0 && -dX[1] > Math.abs(dY[1]) * 2.0
                                && -dX[2] > Math.abs(dY[2]) * 2.0 && -dX[3] > Math.abs(dY[3]) * 2.0 -> MultiFingerGestureDetector.SWIPE_LEFT
                        dX[0] > Math.abs(dY[0]) * 2.0 && dX[1] > Math.abs(dY[1]) * 2.0
                                && dX[2] > Math.abs(dY[2]) * 2.0 && dX[3] > Math.abs(dY[3]) * 2.0 -> MultiFingerGestureDetector.SWIPE_RIGHT
                        else -> MultiFingerGestureDetector.SWIPE_UNKNOWN
                    }
                }
                else -> MultiFingerGestureDetector.SWIPE_UNKNOWN
            }
        }

    fun getGesture(paramMotionEvent: MotionEvent): GestureData {
        for (i in 0 until fingers) {
            dX[i] = paramMotionEvent.getX(i) - iX[i]
            dY[i] = paramMotionEvent.getY(i) - iY[i]
        }

        val type = GestureData()
        type.gestureFlag = gestureFlag
        type.gestureDuration = SystemClock.uptimeMillis() - startTime
        type.gesturePointer = fingers
        return type
    }

    fun isGesture(paramMotionEvent: MotionEvent): Boolean {
        for (i in 0 until fingers) {
            if (Math.abs(paramMotionEvent.getX(i) - iX[i]) >= sensitivity || Math.abs(paramMotionEvent.getY(i) - iY[i]) >= sensitivity) {
                return true
            }
        }
        return false
    }

    fun trackGesture(paramMotionEvent: MotionEvent) {
        var pointers = paramMotionEvent.pointerCount
        if (pointers > 5)
            pointers = 5

        var i = 0
        while (pointers > i) {
            iX[i] = paramMotionEvent.getX(i).toDouble()
            iY[i] = paramMotionEvent.getY(i).toDouble()
            i++
        }
        fingers = pointers
        startTime = SystemClock.uptimeMillis()
    }

    fun unTrackGesture() {
        fingers = 0
    }

    fun setSensitivity(sensitivity: Int) {
        this.sensitivity = sensitivity
    }

    internal class GestureData {
        var gestureDuration: Long = 0
        var gestureFlag: Int = 0
        var gesturePointer: Int = 0
    }
}
