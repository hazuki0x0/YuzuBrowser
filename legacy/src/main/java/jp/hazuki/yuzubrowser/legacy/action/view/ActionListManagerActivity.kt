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

package jp.hazuki.yuzubrowser.legacy.action.view

import android.os.Bundle
import jp.hazuki.yuzubrowser.legacy.action.ActionList
import jp.hazuki.yuzubrowser.legacy.action.ActionManager
import jp.hazuki.yuzubrowser.legacy.action.ListActionManager

class ActionListManagerActivity : ActionListActivity() {
    private lateinit var mActionManager: ListActionManager
    override lateinit var actionList: ActionList

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = intent ?: throw NullPointerException("intent is null")
        val mActionType = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, 0)
        if (mActionType == 0) throw IllegalArgumentException("Unknown action type")
        val mActionId = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_ID, 0)
        if (mActionId == 0) throw IllegalArgumentException("Unknown action id")

        mActionManager = ActionManager.getActionManager(this, mActionType) as? ListActionManager ?: throw IllegalArgumentException()
        actionList = mActionManager.getActionList(mActionId)
        super.onCreate(savedInstanceState)
    }

    override fun onActionListChanged(actionList: ActionList) {
        mActionManager.save(applicationContext)
    }
}
