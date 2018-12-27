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

package jp.hazuki.yuzubrowser.legacy.webkit

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.widget.FrameLayout

class WebCustomViewHandler(private val fullscreenLayout: ViewGroup) {
    private var customView: View? = null
    private lateinit var customViewCallback: WebChromeClient.CustomViewCallback
    private var oldOrientation: Int = 0
    private var oldFlag: Int = 0
    private var oldUiVisibility: Int = 0

    val isCustomViewShowing: Boolean
        get() = customView != null

    fun showCustomView(activity: Activity, view: View, orientation: Int, callback: WebChromeClient.CustomViewCallback) {
        if (customView != null) {
            callback.onCustomViewHidden()
            return
        }

        oldOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation

        val window = activity.window
        oldUiVisibility = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

        val windowParams = window.attributes
        oldFlag = windowParams.flags
        windowParams.flags = windowParams.flags or ADD_FLAGS
        window.attributes = windowParams

        fullscreenLayout.setBackgroundColor(Color.BLACK)
        fullscreenLayout.visibility = View.VISIBLE
        fullscreenLayout.addView(view, FULLSCREEN_LP)
        fullscreenLayout.bringToFront()


        customView = view
        customViewCallback = callback
    }

    @SuppressLint("WrongConstant")
    fun hideCustomView(activity: Activity) {
        if (customView == null) return

        activity.requestedOrientation = oldOrientation

        val window = activity.window
        window.decorView.systemUiVisibility = oldUiVisibility

        val windowParams = window.attributes
        windowParams.flags = oldFlag
        window.attributes = windowParams

        fullscreenLayout.visibility = View.GONE
        fullscreenLayout.removeView(customView)

        customView = null
        customViewCallback.onCustomViewHidden()
    }

    companion object {
        private val FULLSCREEN_LP = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        private const val ADD_FLAGS = WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
    }
}
