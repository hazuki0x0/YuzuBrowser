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

package jp.hazuki.yuzubrowser.browser.ui

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.view.ViewGroup
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.Action
import jp.hazuki.yuzubrowser.action.manager.ActionController
import jp.hazuki.yuzubrowser.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.action.manager.QuickControlActionManager
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.theme.ThemeData
import jp.hazuki.yuzubrowser.utils.DisplayUtils
import jp.hazuki.yuzubrowser.utils.view.pie.PieItem
import jp.hazuki.yuzubrowser.utils.view.pie.PieMenu

class PieManager(private val context: Context, private val actionController: ActionController, private val iconManager: ActionIconManager) : PieMenu.PieController {

    private val itemSize = context.resources.getDimension(R.dimen.qc_item_size).toInt()
    private val pie = PieMenu(context).apply {
        setController(this@PieManager)
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
    val isOpen: Boolean
        get() = pie.isOpen

    fun attachToLayout(layout: ViewGroup) {
        layout.addView(pie)
        makeItems()
    }

    fun detachFromLayout(layout: ViewGroup) {
        layout.removeView(pie)
        pie.clearItems()
    }

    private fun makeItems() {
        val manager = QuickControlActionManager.getInstance(context)

        for (action in manager.level1.list)
            addItem(action, 1)
        for (action in manager.level2.list)
            addItem(action, 2)
        for (action in manager.level3.list)
            addItem(action, 3)
    }

    private fun addItem(action: Action, l: Int): PieItem {
        val item = PieItem(context, itemSize, action, actionController, iconManager, l)
        pie.addItem(item)
        return item
    }

    override fun onOpen(): Boolean {
        pie.notifyChangeState()
        return true
    }

    fun onPreferenceReset() {
        val density = DisplayUtils.getDensity(context)
        pie.setRadiusStart((AppData.qc_rad_start.get() * density + 0.5f).toInt())
        pie.setRadiusIncrement((AppData.qc_rad_inc.get() * density + 0.5f).toInt())
        pie.setSlop((AppData.qc_slop.get() * density + 0.5f).toInt())
        pie.setPosition(AppData.qc_position.get())
    }

    fun onThemeChanged(themeData: ThemeData?) {
        if (themeData != null && themeData.qcItemBackgroundColorNormal != 0)
            pie.setNormalColor(themeData.qcItemBackgroundColorNormal)
        else
            pie.setNormalColor(ResourcesCompat.getColor(context.resources, R.color.qc_normal, context.theme))

        if (themeData != null && themeData.qcItemBackgroundColorSelect != 0)
            pie.setSelectedColor(themeData.qcItemBackgroundColorSelect)
        else
            pie.setSelectedColor(ResourcesCompat.getColor(context.resources, R.color.qc_selected, context.theme))

        if (themeData != null && themeData.qcItemColor != 0)
            pie.setColorFilterToItems(themeData.qcItemColor)
        else
            pie.setColorFilterToItems(0)
    }
}