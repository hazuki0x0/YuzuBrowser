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

package jp.hazuki.yuzubrowser.legacy.toolbar.main

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.toolbar.AbstractToolbar
import jp.hazuki.yuzubrowser.ui.settings.container.ToolbarContainer

abstract class ToolbarBase(context: Context, preference: ToolbarContainer, layout_id: Int, request_callback: RequestCallback) : AbstractToolbar(context) {
    val toolbarPreferences: ToolbarContainer = preference
    protected val mRequestCallback: RequestCallback = request_callback

    open fun onPreferenceReset() {
        visibility = if (mRequestCallback.shouldShowToolbar(toolbarPreferences.visibility, null)) View.VISIBLE else View.GONE
    }

    open fun onFullscreenChanged(isFullscreen: Boolean) {
        visibility = if (mRequestCallback.shouldShowToolbar(toolbarPreferences.visibility, null)) View.VISIBLE else View.GONE
    }

    open fun notifyChangeWebState(data: MainTabData?) {
        if (data != null)
            visibility = if (mRequestCallback.shouldShowToolbar(toolbarPreferences.visibility, data)) View.VISIBLE else View.GONE
    }

    open fun resetToolBar() {

    }

    fun toggleVisibility() {
        val isVisible = visibility == View.VISIBLE
        toolbarPreferences.visibility.setAlwaysVisible(!isVisible)
        visibility = if (!isVisible) View.VISIBLE else View.GONE
    }

    fun onActivityConfigurationChanged(newConfig: Configuration) {
        visibility = if (mRequestCallback.shouldShowToolbar(toolbarPreferences.visibility, null, newConfig)) View.VISIBLE else View.GONE
    }

    init {
        LayoutInflater.from(context).inflate(layout_id, this)
    }
}
