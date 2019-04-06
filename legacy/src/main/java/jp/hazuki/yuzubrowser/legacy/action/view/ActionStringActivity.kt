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

package jp.hazuki.yuzubrowser.legacy.action.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.widget.Toast
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.legacy.action.ActionList
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.utils.util.JsonConvertable
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import kotlinx.android.synthetic.main.scroll_edittext.*

class ActionStringActivity : ThemeActivity() {
    private var mTarget: Int = 0
    private var mActionNameArray: ActionNameArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scroll_edittext)

        val intent = intent ?: throw NullPointerException("Intent is null")

        mActionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA)

        val action = intent.getParcelableExtra<Parcelable>(EXTRA_ACTION)
        if (action != null) {
            if (action is Action) {
                mTarget = ACTION_ACTIVITY
                editText.setText((action as JsonConvertable).toJsonString())
                return
            } else if (action is ActionList) {
                mTarget = ACTION_LIST_ACTIVITY
                editText.setText((action as JsonConvertable).toJsonString())
                return
            }
            throw IllegalArgumentException("ARG_ACTION is not action or actionlist")
        } else {
            mTarget = getIntent().getIntExtra(EXTRA_ACTIVITY, ACTION_ACTIVITY)

            when (mTarget) {
                ACTION_ACTIVITY -> ActionActivity.Builder(this)
                        .show(ACTION_ACTIVITY)
                ACTION_LIST_ACTIVITY -> ActionListActivity.Builder(this)
                        .show(ACTION_LIST_ACTIVITY)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.json_to_action).setOnMenuItemClickListener {
            val jsonStr = editText.text.toString()

            if (callingPackage == null) {
                when (mTarget) {
                    ACTION_ACTIVITY -> ActionActivity.Builder(this@ActionStringActivity)
                            .setDefaultAction(Action(jsonStr))
                            .setActionNameArray(mActionNameArray)
                            .show(ACTION_ACTIVITY)
                    ACTION_LIST_ACTIVITY -> ActionListActivity.Builder(this@ActionStringActivity)
                            .setDefaultActionList(ActionList(jsonStr))
                            .setActionNameArray(mActionNameArray)
                            .show(ACTION_LIST_ACTIVITY)
                }
            } else {
                var result = false
                var actionObj: Parcelable? = null
                when (mTarget) {
                    ACTION_ACTIVITY -> {
                        val action = Action()
                        result = action.fromJsonString(jsonStr)
                        actionObj = action
                    }
                    ACTION_LIST_ACTIVITY -> {
                        val actionList = ActionList()
                        result = actionList.fromJsonString(jsonStr)
                        actionObj = actionList
                    }
                }

                if (result) {
                    val intent = Intent()
                    intent.putExtra(EXTRA_ACTION, actionObj)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(this, R.string.invalid_json_format, Toast.LENGTH_SHORT).show()
                }

            }

            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ACTION_ACTIVITY -> {
                if (resultCode != Activity.RESULT_OK || data == null) return

                val action = data.getParcelableExtra<Action>(ActionActivity.EXTRA_ACTION) ?: return
                editText.setText(action.toJsonString())
            }
            ACTION_LIST_ACTIVITY -> {
                if (resultCode != Activity.RESULT_OK || data == null) return

                val action = data.getParcelableExtra<ActionList>(ActionListActivity.EXTRA_ACTION_LIST) ?: return
                editText.setText(action.toJsonString())
            }
        }
    }

    companion object {
        const val EXTRA_ACTIVITY = "MakeActionStringActivity.extra.activity"
        const val EXTRA_ACTION = "MakeActionStringActivity.extra.action"
        const val ACTION_ACTIVITY = 1
        const val ACTION_LIST_ACTIVITY = 2
    }
}
