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

package jp.hazuki.yuzubrowser.adblock.ui.abp

import android.content.Context
import android.text.format.DateUtils.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.adblock.R
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpEntity
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener


class AbpEntityAdapter(private val context: Context, val items: MutableList<AbpEntity>, private val listener: OnRecyclerListener) : RecyclerView.Adapter<AbpEntityAdapter.Holder>() {

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]

        holder.title.text = item.title ?: item.url
        holder.update.text = if (item.lastLocalUpdate > 0) {
            context.getString(R.string.last_update,
                    getRelativeTimeSpanString(item.lastLocalUpdate,
                            System.currentTimeMillis(), MINUTE_IN_MILLIS, FORMAT_NUMERIC_DATE))
        } else {
            context.getString(R.string.unknown)
        }
        holder.enable.isChecked = item.enabled
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(context).inflate(R.layout.abp_list_item, parent, false), listener)
    }


    class Holder(view: View, listener: OnRecyclerListener) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleTextView)
        val update: TextView = view.findViewById(R.id.lastUpdateTextView)
        val enable: CheckBox = view.findViewById(R.id.enableCheckBox)

        init {
            view.setOnClickListener { listener.onRecyclerItemClicked(view, adapterPosition) }
            view.setOnLongClickListener { listener.onRecyclerItemLongClicked(view, adapterPosition) }
        }
    }
}