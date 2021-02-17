/*
 * Copyright (C) 2017-2021 Hazuki
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

package jp.hazuki.yuzubrowser.ui.app

import android.os.Build
import android.view.*
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi

sealed class SystemUiController(
    protected val window: Window,
) {
    var isLightStatusBar = false
        set(value) {
            val isUpdated = field != value
            field = value

            if (isUpdated) isNeedUpdate = true
        }

    var statusBarColor = window.statusBarColor
        set(value) {
            val isUpdated = field != value
            field = value

            if (isUpdated) isNeedUpdate = true
        }

    open var isLightNavigationBar = false
        set(value) {
            val isUpdated = field != value
            field = value

            if (isUpdated) isNeedUpdate = true
        }

    open var navigationBarColor = window.navigationBarColor
        set(value) {
            val isUpdated = field != value
            field = value

            if (isUpdated) isNeedUpdate = true
        }

    var barState = State.NORMAL
        set(value) {
            val isUpdated = field != value
            field = value

            if (isUpdated) isNeedUpdate = true
        }

    protected var isNeedUpdate = false

    @CallSuper
    open fun updateConfigure() {
        isNeedUpdate = false
    }

    fun updateConfigureIfNeed() {
        if (isNeedUpdate) {
            updateConfigure()
        }
    }

    enum class State {
        NORMAL,
        HIDE_STATUS_BAR,
        HIDE_NAVIGATION_BAR,
        HIDE_ALL_BAR,
    }

    companion object {
        fun create(window: Window): SystemUiController {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> ControllerApi30(window)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> ControllerApi26(window)
                else -> ControllerApi23(window)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private class ControllerApi30(window: Window) : SystemUiController(window) {

        override fun updateConfigure() {
            super.updateConfigure()

            window.also {
                it.statusBarColor = statusBarColor
                it.navigationBarColor = navigationBarColor

                var appearance = 0
                if (isLightStatusBar) {
                    appearance = appearance or WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                }
                if (isLightNavigationBar) {
                    appearance = appearance or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                }

                val hide = when (barState) {
                    State.NORMAL -> 0
                    State.HIDE_STATUS_BAR -> WindowInsets.Type.statusBars()
                    State.HIDE_NAVIGATION_BAR -> WindowInsets.Type.navigationBars()
                    State.HIDE_ALL_BAR -> WindowInsets.Type.statusBars() or
                        WindowInsets.Type.navigationBars()
                }

                val show = when (barState) {
                    State.NORMAL -> WindowInsets.Type.statusBars() or
                        WindowInsets.Type.navigationBars()
                    State.HIDE_STATUS_BAR -> WindowInsets.Type.navigationBars()
                    State.HIDE_NAVIGATION_BAR -> WindowInsets.Type.statusBars()
                    State.HIDE_ALL_BAR -> 0
                }


                it.decorView.windowInsetsController?.run {
                    hide(hide)
                    show(show)
                    if (hide != 0) {
                        systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                    setSystemBarsAppearance(
                        appearance,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("DEPRECATION")
    private class ControllerApi26(window: Window) : ControllerApi23(window) {
        override var navigationBarColor = window.navigationBarColor
            set(value) {
                val isUpdated = field != value
                field = value

                if (isUpdated) isNeedUpdate = true
            }

        override var isLightNavigationBar = false
            set(value) {
                val isUpdated = field != value
                field = value

                if (isUpdated) isNeedUpdate = true
            }

        override fun getFlags(): Int {
            return if (isLightNavigationBar) {
                super.getFlags() or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                super.getFlags()
            }
        }
    }

    @Suppress("DEPRECATION")
    private open class ControllerApi23(window: Window) : SystemUiController(window) {

        override fun updateConfigure() {
            super.updateConfigure()

            window.also {
                it.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                it.statusBarColor = statusBarColor
                it.navigationBarColor = navigationBarColor

                it.decorView.systemUiVisibility = getFlags()
            }
        }

        open fun getFlags(): Int {
            return if (isLightStatusBar) {
                getWindowTypeFlags() or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                getWindowTypeFlags()
            }
        }

        private fun getWindowTypeFlags(): Int {
            return when (barState) {
                State.NORMAL -> 0
                State.HIDE_STATUS_BAR ->
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                State.HIDE_NAVIGATION_BAR ->
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                State.HIDE_ALL_BAR ->
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
        }
    }
}
