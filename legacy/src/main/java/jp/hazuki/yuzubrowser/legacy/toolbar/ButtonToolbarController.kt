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

import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionController
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionFile
import jp.hazuki.yuzubrowser.legacy.utils.view.swipebutton.SwipeImageButton
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs

open class ButtonToolbarController(private val linearLayout: ViewGroup, private val controller: ActionController, private val iconManager: ActionIconManager, private val TOOLBAR_SIZE_Y: Int) {
    private val mButtonList: ArrayList<SwipeImageButton> = ArrayList(1)

    fun addButtons(list: List<SoftButtonActionFile>) {
        val size = list.size
        if (mButtonList.size == size) {
            for ((i, btn) in mButtonList.withIndex()) {
                btn.setActionData(list[i], controller, iconManager)
                btn.setSense(AppPrefs.swipebtn_sensitivity.get())
            }
        } else {
            linearLayout.removeAllViews()

            val inflater = LayoutInflater.from(linearLayout.context)

            for (i in 0 until size) {
                val btn = inflateButtonView(inflater, linearLayout)
                mButtonList.add(btn)
                btn.setActionData(list[i], controller, iconManager)
                btn.setSense(AppPrefs.swipebtn_sensitivity.get())
                settingButtonSize(btn, TOOLBAR_SIZE_Y)
            }
        }
    }

    open fun inflateButtonView(inflater: LayoutInflater, parent: ViewGroup): SwipeImageButton {
        val view = inflater.inflate(R.layout.toolbar_custom_button, parent, false)
        parent.addView(view)
        return view.findViewById(R.id.button)
    }

    fun notifyChangeState() {
        for (btn in mButtonList) {
            btn.notifyChangeState()
        }
    }

    fun resetIcon() {
        for (btn in mButtonList) {
            btn.setToDefault()
        }
    }

    fun setColorFilter(cf: ColorFilter?) {
        for (btn in mButtonList)
            btn.colorFilter = cf
    }

    fun setBackgroundDrawable(background: Drawable?) {
        for (btn in mButtonList)
            btn.setImageDrawable(background)
    }

    companion object {

        fun settingButtonSize(view: View, size: Int) {
            val params = view.layoutParams
            params.height = size
            params.width = size
            view.layoutParams = params
        }

        fun settingButtonSizeHeight(view: View, size: Int) {
            val params = view.layoutParams
            params.height = size
            view.layoutParams = params
        }
    }
}
