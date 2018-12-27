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

package jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.detector

import android.content.Context
import android.view.MotionEvent

class MultiFingerGestureDetector(context: Context, private val gestureListener: OnMultiFingerGestureListener) {

    var isTracking: Boolean = false
        private set
    private var showName: Boolean = false
    private var beforeDuration: Int = 0
    private val analyzer = MfGestureAnalyzer(context)
    private val info = MultiFingerGestureInfo()

    fun onTouchEvent(event: MotionEvent): Boolean {
        var flag = false
        when (event.action and 0xff) {
            MotionEvent.ACTION_DOWN -> startTracking(event)
            MotionEvent.ACTION_POINTER_DOWN -> startTracking(event)
            MotionEvent.ACTION_MOVE -> moveTracking(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                flag = execute()
                stopTracking()
            }
            MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_CANCEL -> stopTracking()
        }
        return flag
    }

    private fun execute(): Boolean {
        if (isTracking) {
            isTracking = false
            return gestureListener.onGesturePerformed(info)
        }
        return false
    }

    private fun moveTracking(motionEvent: MotionEvent) {
        if (analyzer.fingers >= 1 && analyzer.isGesture(motionEvent)) {
            val gestureData = analyzer.getGesture(motionEvent)
            if (gestureData.gestureFlag != 0) {
                if (beforeDuration != gestureData.gestureFlag) {
                    info.fingers = gestureData.gesturePointer
                    info.trace = gestureData.gestureFlag
                    if (showName)
                        gestureListener.onShowGestureName(info)
                }
                beforeDuration = gestureData.gestureFlag
            }
            analyzer.unTrackGesture()
            analyzer.trackGesture(motionEvent)
        }
    }

    private fun startTracking(event: MotionEvent) {
        isTracking = true
        info.clear()
        beforeDuration = 0
        analyzer.trackGesture(event)
    }

    fun stopTracking() {
        isTracking = false
        analyzer.unTrackGesture()
        gestureListener.onDismissGestureName()
    }

    fun setShowName(showName: Boolean) {
        this.showName = showName
    }

    fun setSensitivity(sensitivity: Int) {
        analyzer.setSensitivity(sensitivity)
    }

    interface OnMultiFingerGestureListener {
        fun onGesturePerformed(info: MultiFingerGestureInfo): Boolean

        fun onShowGestureName(info: MultiFingerGestureInfo)

        fun onDismissGestureName()
    }

    companion object {
        const val SWIPE_UNKNOWN = 0
        const val SWIPE_UP = 1
        const val SWIPE_DOWN = 2
        const val SWIPE_LEFT = 3
        const val SWIPE_RIGHT = 4
    }
}
