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

package jp.hazuki.yuzubrowser.legacy.toolbar

import android.content.res.Configuration
import android.content.res.Resources
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.toolbar.main.CustomToolbar
import jp.hazuki.yuzubrowser.legacy.toolbar.main.ProgressToolBar
import jp.hazuki.yuzubrowser.legacy.toolbar.main.TabBar
import jp.hazuki.yuzubrowser.legacy.toolbar.main.UrlBarBase
import jp.hazuki.yuzubrowser.legacy.utils.view.tab.TabLayout
import jp.hazuki.yuzubrowser.ui.theme.ThemeData
import jp.hazuki.yuzubrowser.webview.CustomWebView

interface ToolbarManager {
    val tabBar: TabBar
    val urlBar: UrlBarBase
    val progressBar: ProgressToolBar
    val customBar: CustomToolbar
    val findOnPage: View
    val bottomToolbarAlwaysLayout: LinearLayout
    fun addToolbarView(res: Resources) =
            addToolbarView(res.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)

    fun addToolbarView(isPortrait: Boolean)
    fun changeCurrentTab(to_id: Int, from: MainTabData?, to: MainTabData?)
    fun moveCurrentTabPosition(id: Int)
    fun swapTab(a: Int, b: Int)
    fun moveTab(from: Int, to: Int, newCurrent: Int)
    fun onPreferenceReset()
    fun onThemeChanged(themeData: ThemeData?)
    fun onActivityConfigurationChanged(config: Configuration)
    fun onFullscreenChanged(isFullscreen: Boolean)
    fun onImeChanged(isShown: Boolean)
    fun notifyChangeProgress(data: MainTabData)
    fun notifyChangeWebState() = notifyChangeWebState(null)
    fun notifyChangeWebState(data: MainTabData?)
    fun resetToolBar()
    fun addNewTabView(): View
    fun scrollTabRight()
    fun scrollTabLeft()
    fun scrollTabTo(position: Int)
    fun removeTab(no: Int)
    fun addTab(id: Int, view: View)
    fun setWebViewTitleBar(web: CustomWebView, combine: Boolean)
    fun setOnTabClickListener(l: TabLayout.OnTabClickListener)
    fun showGeolocationPermissionPrompt(view: View)
    fun hideGeolocationPermissionPrompt(view: View)
    fun isContainsWebToolbar(ev: MotionEvent): Boolean
    fun onWebViewScroll(web: CustomWebView, e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float)
    fun onWebViewTapUp()

    companion object {
        const val LOCATION_UNDEFINED = -1
        const val LOCATION_TOP = 0
        const val LOCATION_BOTTOM = 1
        const val LOCATION_WEB = 2
        const val LOCATION_FIXED_WEB = 3
        const val LOCATION_FLOAT_BOTTOM = 4
        const val LOCATION_LEFT = 5
        const val LOCATION_RIGHT = 6
        const val LOCATION_TOP_ALWAYS = 7
        const val LOCATION_BOTTOM_ALWAYS = 8
        const val LOCATION_BOTTOM_OVERLAY_ALWAYS = 9
    }
}