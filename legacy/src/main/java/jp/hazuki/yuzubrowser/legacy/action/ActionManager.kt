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

package jp.hazuki.yuzubrowser.legacy.action

import android.content.Context
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.legacy.action.manager.*
import java.util.*

open class ActionManager {

    private val actionFileList: ArrayList<ActionFile>
        get() {
            val list = ArrayList<ActionFile>()
            try {
                javaClass.declaredFields.forEach {
                    it.isAccessible = true
                    val obj = it.get(this)
                    if (obj is ActionFile) {
                        list.add(obj)
                    }
                }
            } catch (e: IllegalArgumentException) {
                ErrorReport.printAndWriteLog(e)
            } catch (e: IllegalAccessException) {
                ErrorReport.printAndWriteLog(e)
            }

            return list
        }

    fun load(context: Context): Boolean {
        for (actions in actionFileList) {
            if (!actions.load(context)) {
                Logger.e(TAG, "init failed")
                return false
            }
        }
        return true
    }

    fun save(context: Context): Boolean {
        for (actions in actionFileList) {
            if (!actions.write(context)) {
                Logger.e(TAG, "save failed")
                return false
            }
        }
        return true
    }

    companion object {
        const val INTENT_EXTRA_ACTION_TYPE = "ActionManager.extra.actionType"
        const val INTENT_EXTRA_ACTION_ID = "ActionManager.extra.actionId"

        //same as attrs.xml
        private const val TYPE_SOFT_BUTTON = 1
        private const val TYPE_MENU = 2
        private const val TYPE_HARD_BUTTON = 3
        private const val TYPE_TAB = 4
        private const val TYPE_SOFT_BUTTON_CUSTOMBAR = 5
        private const val TYPE_LONGPRESS = 6
        private const val TYPE_FLICK = 7
        private const val TYPE_QUICK_CONTROL = 8
        private const val TYPE_WEB_SWIPE = 9
        private const val TYPE_SOFT_BUTTON_ARRAY = 10
        private const val TYPE_DOUBLE_TAP_FLICK = 11

        fun getActionManager(context: Context, type: Int): ActionManager {
            when (type) {
                TYPE_SOFT_BUTTON -> return SoftButtonActionManager.getInstance(context)
                TYPE_MENU -> return MenuActionManager.getInstance(context)
                TYPE_HARD_BUTTON -> return HardButtonActionManager.getInstance(context)
                TYPE_TAB -> return TabActionManager.getInstance(context)
                TYPE_SOFT_BUTTON_CUSTOMBAR -> return ToolbarActionManager.getInstance(context)
                TYPE_LONGPRESS -> return LongPressActionManager.getInstance(context)
                TYPE_FLICK -> return FlickActionManager.getInstance(context)
                TYPE_QUICK_CONTROL -> return QuickControlActionManager.getInstance(context)
                TYPE_WEB_SWIPE -> return WebSwipeActionManager.getInstance(context)
                TYPE_SOFT_BUTTON_ARRAY -> return SoftButtonActionArrayManager.getInstance(context)
                TYPE_DOUBLE_TAP_FLICK -> return DoubleTapFlickActionManager.getInstance(context)
            }
            throw IllegalArgumentException()
        }

        private const val TAG = "ActionManagerBase"
    }
}
