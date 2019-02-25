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

package jp.hazuki.yuzubrowser.browser.view

import android.content.Context
import android.gesture.GestureOverlayView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.appbar.AppBarLayout
import jp.hazuki.yuzubrowser.browser.R

class GestureFrameLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private var viewOffset = 0
    private val gestureOverlay: CustomGestureOverlayView by bind(R.id.webGestureOverlayViewInner)

    fun setWebFrame(appBarLayout: AppBarLayout) {
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            viewOffset = verticalOffset + appBarLayout.totalScrollRange
            gestureOverlay.translationY = -viewOffset.toFloat()
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val event = MotionEvent.obtain(ev)
        val offsetX = scrollX - gestureOverlay.left
        val offsetY = scrollY - gestureOverlay.top + viewOffset
        event.offsetLocation(offsetX.toFloat(), offsetY.toFloat())
        return gestureOverlay.preDispatchTouchEvent(event) or super.dispatchTouchEvent(ev)
    }

    fun setGestureVisible(visible: Boolean) {
        gestureOverlay.isGestureVisible = visible
    }

    fun removeAllOnGestureListeners() {
        gestureOverlay.removeAllOnGestureListeners()
    }

    fun removeAllOnGesturePerformedListeners() {
        gestureOverlay.removeAllOnGesturePerformedListeners()
    }

    fun addOnGestureListener(listener: GestureOverlayView.OnGestureListener?) {
        gestureOverlay.addOnGestureListener(listener)
    }

    fun addOnGesturePerformedListener(listener: GestureOverlayView.OnGesturePerformedListener?) {
        gestureOverlay.addOnGesturePerformedListener(listener)
    }

    override fun setOnTouchListener(l: View.OnTouchListener?) {
        gestureOverlay.setOnTouchListener(l)
    }

    override fun isEnabled(): Boolean {
        return gestureOverlay.isEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        gestureOverlay.isEnabled = enabled
        super.setEnabled(enabled)
    }

    private fun <T : View> bind(idRes: Int): Lazy<T> {
        @Suppress("UNCHECKED_CAST")
        return lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(idRes) }
    }
}
