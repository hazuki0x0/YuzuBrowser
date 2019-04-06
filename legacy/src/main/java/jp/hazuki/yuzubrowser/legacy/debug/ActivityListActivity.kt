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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class ActivityListActivity : ThemeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)
        title = "Debug mode"

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, ActivityListFragment())
                .commit()
    }

    class ActivityListFragment : androidx.fragment.app.ListFragment() {
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

            try {
                val activity = activity ?: return
                val activities = activity.packageManager.getPackageInfo(activity.packageName, PackageManager.GET_ACTIVITIES).activities
                listAdapter = object : ArrayAdapter<ActivityInfo>(activity, 0, activities) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = convertView ?: LayoutInflater.from(activity).inflate(android.R.layout.simple_list_item_1, parent, false)

                        getItem(position)?.run {
                            view.findViewById<TextView>(android.R.id.text1).text = name.substring(name.lastIndexOf('.') + 1)
                        }

                        return view
                    }
                }
            } catch (e: NameNotFoundException) {
                e.printStackTrace()
            }

        }

        override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
            super.onListItemClick(l, v, position, id)
            val intent = Intent()
            intent.setClassName(activity, (l.adapter.getItem(position) as ActivityInfo).name)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, "This activity can't open.", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
