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

package jp.hazuki.yuzubrowser.legacy.action.item.startactivity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import jp.hazuki.yuzubrowser.legacy.R

class StartActivityPreferenceFragment : androidx.fragment.app.ListFragment() {

    private var mListener: OnActionListener? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        listAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.action_start_activity_template))
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        when (position) {
            0 -> mListener?.openApplicationList() //Application
            1 -> mListener?.openShortCutList() //Shortcut
            2 -> mListener?.openSharePage() //Share page
            3 -> mListener?.openOther() //Open in other app
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = activity as OnActionListener
        } catch (e: ClassCastException) {
            throw IllegalStateException(e)
        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnActionListener {
        fun openApplicationList()

        fun openShortCutList()

        fun openSharePage()

        fun openOther()
    }
}
