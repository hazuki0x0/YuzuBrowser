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
import android.widget.LinearLayout
import android.widget.TextView
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionController
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionArrayManager
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.toolbar.ButtonToolbarController
import jp.hazuki.yuzubrowser.legacy.utils.view.tab.FullTabLayout
import jp.hazuki.yuzubrowser.legacy.utils.view.tab.ScrollableTabLayout
import jp.hazuki.yuzubrowser.legacy.utils.view.tab.TabLayout
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.ui.theme.ThemeData
import kotlinx.android.synthetic.main.toolbar_tab.view.*

class TabBar(context: Context, controller: ActionController, iconManager: ActionIconManager, request_callback: RequestCallback) : ToolbarBase(context, AppPrefs.toolbar_tab, R.layout.toolbar_tab, request_callback) {
    private val tabSizeX = context.convertDpToPx(AppPrefs.tab_size_x.get())
    private val tabSizeY = context.convertDpToPx(AppPrefs.toolbar_tab.size.get())
    private val tabFontSize = AppPrefs.tab_font_size.get()
    private val mTabLayout: TabLayout
    private val mLeftButtonController: ButtonToolbarController
    private val mRightButtonController: ButtonToolbarController

    init {
        mLeftButtonController = ButtonToolbarController(leftLinearLayout, controller, iconManager, tabSizeY)
        mRightButtonController = ButtonToolbarController(rightLinearLayout, controller, iconManager, tabSizeY)

        mTabLayout = when (AppPrefs.tab_type.get()) {
            TAB_TYPE_SCROLLABLE -> {
                ScrollableTabLayout(context).also {
                    tabLayoutBase.addView(it)
                }
            }
            TAB_TYPE_FULL -> {
                FullTabLayout(context).also {
                    tabLayoutBase.addView(it)
                }
            }
            else -> throw IllegalArgumentException()
        }

        addButtons()
    }

    override fun notifyChangeWebState(data: MainTabData?) {
        super.notifyChangeWebState(data)
        mLeftButtonController.notifyChangeState()
        mRightButtonController.notifyChangeState()
    }

    override fun resetToolBar() {
        mLeftButtonController.resetIcon()
        mRightButtonController.resetIcon()
    }

    override fun applyTheme(themeData: ThemeData?) {
        super.applyTheme(themeData)
        applyTheme(mLeftButtonController)
        applyTheme(mRightButtonController)
        mTabLayout.applyTheme(themeData)
    }

    private fun addButtons() {
        val manager = SoftButtonActionArrayManager.getInstance(context)
        mLeftButtonController.addButtons(manager.btn_tab_left.list)
        mRightButtonController.addButtons(manager.btn_tab_right.list)
        onThemeChanged(ThemeData.getInstance())// TODO
    }

    fun addNewTabView(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.tab_item, null, true)
        view.findViewById<TextView>(R.id.textView).textSize = tabFontSize.toFloat()
        val params = LinearLayout.LayoutParams(tabSizeX, tabSizeY)
        mTabLayout.addTabView(view, params)
        return view
    }

    override fun onPreferenceReset() {
        super.onPreferenceReset()
        addButtons()

        mTabLayout.onPreferenceReset()
        mTabLayout.setSense(AppPrefs.tab_action_sensitivity.get())
    }

    fun removeTab(no: Int) {
        mTabLayout.removeTabAt(no)
    }

    fun addTab(id: Int, view: View) {
        val params = LinearLayout.LayoutParams(tabSizeX, tabSizeY)
        mTabLayout.addTabView(id, view, params)
    }

    fun setOnTabClickListener(l: TabLayout.OnTabClickListener) {
        mTabLayout.setOnTabClickListener(l)
    }

    fun changeCurrentTab(to: Int) {
        mTabLayout.setCurrentTab(to)
    }

    fun fullScrollRight() {
        mTabLayout.fullScrollRight()
    }

    fun fullScrollLeft() {
        mTabLayout.fullScrollLeft()
    }

    fun scrollToPosition(position: Int) {
        mTabLayout.scrollToPosition(position)
    }

    fun swapTab(a: Int, b: Int) {
        mTabLayout.swapTab(a, b)
    }

    fun moveTab(from: Int, to: Int, new_curernt: Int) {
        mTabLayout.moveTab(from, to, new_curernt)
    }

    companion object {
        const val TAB_TYPE_SCROLLABLE = 0
        const val TAB_TYPE_FULL = 1
    }
}
