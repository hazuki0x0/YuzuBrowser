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
import android.view.ViewGroup
import android.widget.LinearLayout
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionController
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.legacy.action.manager.ToolbarActionManager
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.toolbar.ButtonToolbarController
import jp.hazuki.yuzubrowser.legacy.utils.view.swipebutton.SwipeImageButton
import jp.hazuki.yuzubrowser.ui.settings.container.ToolbarContainer
import jp.hazuki.yuzubrowser.ui.theme.ThemeData

open class CustomToolbarBase(context: Context, toolbarContainer: ToolbarContainer, controller: ActionController, iconManager: ActionIconManager, request_callback: RequestCallback) : ToolbarBase(context, toolbarContainer, R.layout.toolbar_custom, request_callback) {
    private val mButtonController: ButtonToolbarController

    init {
        val toolbarSizeY = context.convertDpToPx(toolbarContainer.size.get())
        mButtonController = object : ButtonToolbarController(findViewById(R.id.linearLayout), controller, iconManager, toolbarSizeY) {
            private val PARAMS = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)

            override fun inflateButtonView(inflater: LayoutInflater, parent: ViewGroup): SwipeImageButton {
                val view = inflater.inflate(R.layout.toolbar_custom_button, parent, false)
                parent.addView(view, PARAMS)
                return view.findViewById(R.id.button)
            }
        }
        addButtons()
    }

    override fun onPreferenceReset() {
        super.onPreferenceReset()
        addButtons()
    }

    override fun applyTheme(themeData: ThemeData?) {
        super.applyTheme(themeData)
        applyTheme(mButtonController)
    }

    private fun addButtons() {
        mButtonController.addButtons(ToolbarActionManager.getInstance(context).custombar1.list)
        onThemeChanged(ThemeData.getInstance())// TODO
    }

    override fun notifyChangeWebState(data: MainTabData?) {
        super.notifyChangeWebState(data)
        mButtonController.notifyChangeState()
    }

    override fun resetToolBar() {
        mButtonController.resetIcon()
    }
}
