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
import android.view.LayoutInflater
import android.view.View
import androidx.core.graphics.BlendModeColorFilterCompat.createBlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.legacy.databinding.ToolbarProgressBinding
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.toolbar.ButtonToolbarController
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.ui.theme.ThemeData

class ProgressToolBar(context: Context, request_callback: RequestCallback) : ToolbarBase(context, AppPrefs.toolbar_progress, request_callback) {
    private val binding = ToolbarProgressBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val toolbarSizeY = context.convertDpToPx(AppPrefs.toolbar_progress.size.get())

        ButtonToolbarController.settingButtonSizeHeight(binding.progressBar, toolbarSizeY)
    }

    override fun applyTheme(themeData: ThemeData?) {
        super.applyTheme(themeData)

        if (themeData != null && themeData.progressIndeterminateColor != 0) {
            binding.progressBar.indeterminateDrawable.colorFilter =
                createBlendModeColorFilterCompat(themeData.progressIndeterminateColor, BlendModeCompat.SRC_ATOP)
        } else {
            binding.progressBar.indeterminateDrawable.colorFilter = null
        }

        if (themeData != null && themeData.progressColor != 0) {
            binding.progressBar.progressDrawable.colorFilter =
                createBlendModeColorFilterCompat(themeData.progressColor, BlendModeCompat.SRC_ATOP)
        } else {
            binding.progressBar.progressDrawable.colorFilter = null
        }
    }

    override fun notifyChangeWebState(data: MainTabData?) {
        super.notifyChangeWebState(data)
        if (data != null)
            changeProgress(data)
    }

    fun changeProgress(data: MainTabData) {
        val p = data.mProgress

        val progressBar = binding.progressBar
        if (p == 100 || !data.isInPageLoad) {
            progressBar.visibility = View.INVISIBLE
            if (progressBar.isIndeterminate) progressBar.isIndeterminate = false//need this?
        } else if (p <= 0) {
            progressBar.visibility = View.VISIBLE
            if (!progressBar.isIndeterminate) progressBar.isIndeterminate = true
        } else {
            progressBar.visibility = View.VISIBLE
            if (progressBar.isIndeterminate) progressBar.isIndeterminate = false
            progressBar.progress = p
        }
    }
}
