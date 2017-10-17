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

package jp.hazuki.yuzubrowser.debug

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

import jp.hazuki.yuzubrowser.BuildConfig
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.view.ActionStringActivity
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity

class DebugActivity : ThemeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)
        title = "Debug mode"

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, ItemFragment())
                .commit()
    }


    class ItemFragment : ListFragment() {
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            val list = arrayOf("file list", "activity list", "action json string", "action list json string", "environment")
            listAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, list)
        }

        override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
            super.onListItemClick(l, v, position, id)
            when (position) {
                0 -> startActivity(Intent(activity, DebugFileListActivity::class.java))
                1 -> {
                    if (BuildConfig.DEBUG)
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
            }
        }
    }
}
