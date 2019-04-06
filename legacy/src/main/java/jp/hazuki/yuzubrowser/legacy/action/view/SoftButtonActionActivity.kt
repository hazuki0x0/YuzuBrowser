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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionManager
import jp.hazuki.yuzubrowser.legacy.action.manager.SoftButtonActionFile
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class SoftButtonActionActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        val intent = intent
        val mActionType = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, 0)
        val mActionId = intent.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_ID, 0)
        title = intent.getStringExtra(Intent.EXTRA_TITLE)

        if (mActionType == 0)
            throw IllegalArgumentException("actiontype is 0")

        val fragment = ActionFragment().apply {
            arguments = Bundle().apply {
                putInt(ActionManager.INTENT_EXTRA_ACTION_ID, mActionId)
                putInt(ActionManager.INTENT_EXTRA_ACTION_TYPE, mActionType)
            }
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }


    class ActionFragment : androidx.fragment.app.ListFragment() {
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            listAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1).apply {
                add(getString(R.string.pref_btn_action_press))
                add(getString(R.string.pref_btn_action_lpress))
                add(getString(R.string.pref_btn_action_up))
                add(getString(R.string.pref_btn_action_down))
                add(getString(R.string.pref_btn_action_left))
                add(getString(R.string.pref_btn_action_right))
            }
        }

        override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
            val activity = activity ?: return
            val arguments = arguments ?: throw IllegalArgumentException()

            var actionId = arguments.getInt(ActionManager.INTENT_EXTRA_ACTION_ID)
            actionId = when (position) {
                0 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_PRESS
                1 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_LPRESS
                2 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_UP
                3 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_DOWN
                4 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_LEFT
                5 -> actionId or SoftButtonActionFile.BUTTON_SWIPE_RIGHT
                else -> throw IllegalArgumentException("Unknown position:$position")
            }

            ActionActivity.Builder(activity)
                    .setTitle(activity.title)
                    .setActionManager(arguments.getInt(ActionManager.INTENT_EXTRA_ACTION_TYPE), actionId)
                    .show()
        }
    }

    class Builder(private val context: Context) {
        private val intent = Intent(context.applicationContext, SoftButtonActionActivity::class.java)

        fun setTitle(title: Int): Builder {
            intent.putExtra(Intent.EXTRA_TITLE, context.getString(title))
            return this
        }

        fun setTitle(title: CharSequence): Builder {
            intent.putExtra(Intent.EXTRA_TITLE, title)
            return this
        }

        fun setActionManager(actionType: Int, actionId: Int): Builder {
            intent.putExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, actionType)
            intent.putExtra(ActionManager.INTENT_EXTRA_ACTION_ID, actionId)
            return this
        }

        fun show() {
            context.startActivity(intent)
        }

        fun create(): Intent {
            return intent
        }

        fun show(requestCode: Int) {
            if (context is Activity)
                context.startActivityForResult(intent, requestCode)
            else
                throw IllegalArgumentException("Context is not instanceof Activity")
        }
    }
}
