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

package jp.hazuki.yuzubrowser.legacy.utils.view.swipebutton

import android.content.Context
import android.graphics.drawable.Drawable
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionController
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionFile
import jp.hazuki.yuzubrowser.ui.widget.swipebutton.SwipeController

class SwipeSoftButtonController
//private Drawable mBackgroundDrawable;

(context: Context) : SwipeController(context) {
    private var mActionList: SoftButtonActionFile? = null
    private var controller: ActionController? = null
    private var iconManager: ActionIconManager? = null

    val icon: Drawable?
        get() = getIcon(currentWhatNo)

    val defaultIcon: Drawable?
        get() {
            val actionList = mActionList ?: return null
            val iconManager = iconManager ?: return null

            return iconManager[actionList.press]
        }

    fun setActionData(actionlist: SoftButtonActionFile, controller: ActionController, iconManager: ActionIconManager) {
        mActionList = actionlist

        this.controller = controller
        this.iconManager = iconManager
    }

    fun getIcon(whatNo: Int): Drawable? {
        val actionList = mActionList ?: return null
        return iconManager?.let {
            when (whatNo) {
                SWIPE_CANCEL, SWIPE_PRESS -> it[actionList.press]
                SWIPE_LPRESS -> it[actionList.lpress]
                SWIPE_UP -> it[actionList.up]
                SWIPE_DOWN -> it[actionList.down]
                SWIPE_LEFT -> it[actionList.left]
                SWIPE_RIGHT -> it[actionList.right]
                else -> null
            }
        }
    }

    override fun onEventActionUp(whatNo: Int) {
        //mBackgroundDrawable.setState(STATE_NOTHING);
        val actionList = mActionList ?: return
        val controller = controller ?: return

        when (whatNo) {
            SWIPE_PRESS -> controller.run(actionList.press)
        //SwipeController.SWIPE_LPRESS -> Nothing
            SWIPE_UP -> controller.run(actionList.up)
            SWIPE_DOWN -> controller.run(actionList.down)
            SWIPE_LEFT -> controller.run(actionList.left)
            SWIPE_RIGHT -> controller.run(actionList.right)
        }
    }

    override fun onEventActionDown() {
        //mBackgroundDrawable.setState(STATE_PRESSED);
    }

    override fun onEventCancel() {
        //mBackgroundDrawable.setState(STATE_NOTHING);
    }

    override fun onEventOutSide() {
        //mBackgroundDrawable.setState(STATE_NOTHING);
    }

    override fun onEventLongPress() {
        if (mActionList != null && controller != null) {
            controller!!.run(mActionList!!.lpress)
        }
    }

    fun shouldShow(): Boolean {
        return !mActionList!!.press.isEmpty()
    }
}
