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

package jp.hazuki.yuzubrowser.legacy.gesture.multiFinger

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.data.MultiFingerGestureItem
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener

class MfsListAdapter(context: Context, list: MutableList<MultiFingerGestureItem>, private val nameList: ActionNameArray, listener: OnRecyclerListener) : ArrayRecyclerAdapter<MultiFingerGestureItem, MfsListAdapter.ViewHolder>(context, list, listener) {

    override fun onBindViewHolder(holder: ViewHolder, item: MultiFingerGestureItem, position: Int) {
        val action = item.action

        if (action.isEmpty())
            holder.title.setText(R.string.action_empty)
        else
            holder.title.text = action.toString(nameList)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.simple_recycler_list_item_1, parent, false), this)
    }

    class ViewHolder(itemView: View, adapter: MfsListAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<MultiFingerGestureItem>(itemView, adapter) {
        var title: TextView = itemView.findViewById(android.R.id.text1)
    }
}
