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

package jp.hazuki.yuzubrowser.ui.widget.breadcrumbs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.core.utility.extensions.convertDpToPx
import jp.hazuki.yuzubrowser.ui.R

class BreadcrumbsViewAdapter<T : BreadcrumbsView.Breadcrumb>(context: Context, private val breadcrumbsView: BreadcrumbsView) : RecyclerView.Adapter<BreadcrumbsViewAdapter.BreadcrumbViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val items = mutableListOf<T>()

    private val leftOffset = context.convertDpToPx(72 - 24 - 8)
    private val paddingBig = context.convertDpToPx(16)
    private val paddingSmall = context.convertDpToPx(8)

    val crumbs: List<T>
        get() = items

    var selectedIndex: Int = -1
        private set

    fun addItem(item: T) {
        if (selectedIndex != items.lastIndex) {
            val next = selectedIndex + 1
            if ((items.size > next && items[next] == item)) {
                selectedIndex = next
                notifyDataSetChanged()
                breadcrumbsView.linearLayoutManager.scrollToPositionWithOffset(selectedIndex, leftOffset)
                return
            }
            while (items.size > selectedIndex + 1) {
                items.removeAt(items.lastIndex)
            }
        }
        items.add(item)
        selectedIndex = items.size - 1
        notifyDataSetChanged()
        breadcrumbsView.linearLayoutManager.scrollToPositionWithOffset(selectedIndex, leftOffset)
    }

    fun select(index: Int) {
        if (index < 0 || items.size <= index) throw IllegalArgumentException()
        selectedIndex = index
        notifyDataSetChanged()
        breadcrumbsView.linearLayoutManager.scrollToPositionWithOffset(selectedIndex, leftOffset)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreadcrumbViewHolder {
        val holder = BreadcrumbViewHolder(inflater.inflate(R.layout.breadcrumbs_item, parent, false))
        holder.title.setOnClickListener { breadcrumbsView.listener?.onBreadcrumbItemClick(holder.adapterPosition) }
        return holder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BreadcrumbViewHolder, position: Int) {
        val item = items[position]
        val textView = holder.title
        textView.text = item.title

        if (position == selectedIndex) {
            textView.setTextColor(breadcrumbsView.currentTextColor)
        } else {
            textView.setTextColor(breadcrumbsView.otherTextColor)
        }

        textView.setPadding(paddingSmall, 0, if (position == items.lastIndex) paddingBig else paddingSmall, 0)
    }

    open class BreadcrumbViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view as TextView
    }
}