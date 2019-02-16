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

package jp.hazuki.yuzubrowser.legacy.speeddial.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDial
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener
import java.util.*

internal class SpeedDialRecyclerAdapter(context: Context, private val data: ArrayList<SpeedDial>, listener: OnRecyclerListener) : ArrayRecyclerAdapter<SpeedDial, SpeedDialRecyclerAdapter.ViewHolder>(context, data, listener) {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.simple_recycler_list_item_2, parent, false), this)
    }

    override fun get(index: Int): SpeedDial {
        return data[index]
    }

    internal inner class ViewHolder(itemView: View, adapter: SpeedDialRecyclerAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<SpeedDial>(itemView, adapter) {

        var title: TextView = itemView.findViewById(android.R.id.text1)
        var url: TextView = itemView.findViewById(android.R.id.text2)

        override fun setUp(item: SpeedDial) {
            super.setUp(item)
            title.text = item.title
            url.text = item.url
        }
    }
}
