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

package jp.hazuki.yuzubrowser.utils.view.swipebutton

import android.content.Context
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import android.view.MotionEvent

import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.manager.ActionController
import jp.hazuki.yuzubrowser.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionFile
import jp.hazuki.yuzubrowser.theme.ThemeData

class SwipeImageButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatImageButton(context, attrs), SwipeController.OnChangeListener {
    private val mController = SwipeSoftButtonController(getContext().applicationContext)

    init {
        scaleType = ScaleType.CENTER_INSIDE
    }

    fun setActionData(action_list: SoftButtonActionFile, controller: ActionController, iconManager: ActionIconManager) {
        mController.setActionData(action_list, controller, iconManager)
        mController.setOnChangeListener(this)
        setImageDrawable(mController.defaultIcon)
        setBackgroundResource(R.drawable.swipebtn_image_background_normal)
    }

    fun setToDefault() {
        mController.setToDefault()
    }

    fun notifyChangeState() {
        mController.notifyChangeState()
    }

    fun setSense(sense: Int) {
        mController.setSense(sense)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mController.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onLongPress() {}

    override fun onEventOutSide(): Boolean {
        setImageDrawable(mController.defaultIcon)
        setBackgroundResource(R.drawable.swipebtn_image_background_normal)
        return false
    }

    override fun onEventCancel(): Boolean {
        setImageDrawable(mController.defaultIcon)
        setBackgroundResource(R.drawable.swipebtn_image_background_normal)
        return false
    }

    override fun onEventActionUp(whatNo: Int): Boolean {
        setImageDrawable(mController.defaultIcon)
        setBackgroundResource(R.drawable.swipebtn_image_background_normal)
        return false
    }

    override fun onEventActionDown(): Boolean {
        if (ThemeData.isEnabled() && ThemeData.getInstance().toolbarButtonBackgroundPress != null)
            background = ThemeData.getInstance().toolbarButtonBackgroundPress
        else
            setBackgroundResource(R.drawable.swipebtn_image_background_pressed)
        return false
    }

    override fun onChangeState(whatNo: Int) {
        setImageDrawable(mController.icon)
    }
}
