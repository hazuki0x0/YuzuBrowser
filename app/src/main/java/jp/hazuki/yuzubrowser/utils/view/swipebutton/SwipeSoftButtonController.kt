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
import android.graphics.drawable.Drawable
import jp.hazuki.yuzubrowser.action.manager.ActionController
import jp.hazuki.yuzubrowser.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionFile

class SwipeSoftButtonController
//private Drawable mBackgroundDrawable;

(context: Context) : SwipeController(context) {
    private var mActionList: SoftButtonActionFile? = null
    private var controller: ActionController? = null
    private var iconManager: ActionIconManager? = null

    val icon: Drawable?
        get() = getIcon(currentWhatNo)

    val defaultIcon: Drawable?
        get() = if (iconManager == null) null else iconManager!![mActionList!!.press]

    fun setActionData(actionlist: SoftButtonActionFile, controller: ActionController, iconManager: ActionIconManager) {
        mActionList = actionlist

        this.controller = controller
        this.iconManager = iconManager
    }

    fun getIcon(whatNo: Int): Drawable? {
        return iconManager?.let {
            when (whatNo) {
                SwipeController.SWIPE_CANCEL, SwipeController.SWIPE_PRESS -> return it[mActionList!!.press]
                SwipeController.SWIPE_LPRESS -> return it[mActionList!!.lpress]
                SwipeController.SWIPE_UP -> return it[mActionList!!.up]
                SwipeController.SWIPE_DOWN -> return it[mActionList!!.down]
                SwipeController.SWIPE_LEFT -> return it[mActionList!!.left]
                SwipeController.SWIPE_RIGHT -> return it[mActionList!!.right]
                else -> null
            }
        }
    }

    override fun onEventActionUp(whatNo: Int) {
        //mBackgroundDrawable.setState(STATE_NOTHING);

        if (mActionList != null && controller != null) {
            controller!!.run {
                when (whatNo) {
                    SwipeController.SWIPE_PRESS -> run(mActionList!!.press)
                    SwipeController.SWIPE_LPRESS -> {
                    }
                    SwipeController.SWIPE_UP -> run(mActionList!!.up)
                    SwipeController.SWIPE_DOWN -> run(mActionList!!.down)
                    SwipeController.SWIPE_LEFT -> run(mActionList!!.left)
                    SwipeController.SWIPE_RIGHT -> run(mActionList!!.right)
                    else -> Unit
                }
            }
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
