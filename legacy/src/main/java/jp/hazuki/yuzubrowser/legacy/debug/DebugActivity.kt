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

package jp.hazuki.yuzubrowser.legacy.debug

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.view.ActionStringActivity
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs

class DebugActivity : ThemeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)
        title = "Debug mode"

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, ItemFragment())
                .commit()
    }


    class ItemFragment : androidx.fragment.app.ListFragment() {
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            val activity = activity ?: throw IllegalStateException()
            val list = arrayOf("file list", "activity list", "action json string", "action list json string", "environment", "language")
            listAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, list)
        }

        override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
            super.onListItemClick(l, v, position, id)
            when (position) {
                0 -> startActivity(Intent(activity, DebugFileListActivity::class.java))
                1 -> {
                    if (context!!.resources.getBoolean(R.bool.package_debug))
                        startActivity(Intent(activity, ActivityListActivity::class.java))
                    else
                        Toast.makeText(activity, "This feature is only valid for debug builds", Toast.LENGTH_SHORT).show()
                }
                2 -> startActivity(Intent(activity, ActionStringActivity::class.java).apply {
                    putExtra(ActionStringActivity.EXTRA_ACTIVITY, ActionStringActivity.ACTION_ACTIVITY)
                })
                3 -> startActivity(Intent(activity, ActionStringActivity::class.java).apply {
                    putExtra(ActionStringActivity.EXTRA_ACTIVITY, ActionStringActivity.ACTION_LIST_ACTIVITY)
                })
                4 -> startActivity(Intent(activity, EnvironmentActivity::class.java))
                5 -> LanguageFragment().show(childFragmentManager, "language")
            }
        }

        class LanguageFragment : androidx.fragment.app.DialogFragment() {

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                val activity = activity ?: throw IllegalStateException()
                val names = activity.resources.getStringArray(R.array.language_list)
                val values = activity.resources.getStringArray(R.array.language_value)
                val checked = values.indexOf(AppPrefs.language.get())

                return AlertDialog.Builder(activity)
                        .setTitle("Language")
                        .setSingleChoiceItems(names, checked) { dialog, which ->
                            AppPrefs.language.set(values[which])
                            AppPrefs.commit(activity, AppPrefs.language)
                            dialog.dismiss()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
            }
        }
    }
}
