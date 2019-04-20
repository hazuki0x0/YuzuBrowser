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

package jp.hazuki.yuzubrowser.adblock.ui.original

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import jp.hazuki.yuzubrowser.adblock.R
import jp.hazuki.yuzubrowser.adblock.repository.original.AdBlock
import jp.hazuki.yuzubrowser.core.utility.extensions.getResColor
import jp.hazuki.yuzubrowser.ui.widget.recycler.ArrayRecyclerAdapter
import jp.hazuki.yuzubrowser.ui.widget.recycler.OnRecyclerListener

class AdBlockArrayRecyclerAdapter(context: Context, list: MutableList<AdBlock>, listener: OnRecyclerListener) : ArrayRecyclerAdapter<AdBlock, AdBlockArrayRecyclerAdapter.ItemHolder>(context, list, listener) {

    private val foregroundOverlay = ColorDrawable(context.getResColor(R.color.selected_overlay))

    override fun onBindViewHolder(holder: ItemHolder, item: AdBlock, position: Int) {
        holder.match.text = item.match
        holder.checkBox.isChecked = item.isEnable

        holder.foreground.background = if (isMultiSelectMode && isSelected(position)) foregroundOverlay else null
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): ItemHolder =
            ItemHolder(inflater.inflate(R.layout.fragment_ad_block_list_item, parent, false), this)

    class ItemHolder(itemView: View, adapter: AdBlockArrayRecyclerAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<AdBlock>(itemView, adapter) {
        val match: TextView = itemView.findViewById(R.id.matchTextView)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val foreground: View = itemView.findViewById(R.id.foreground)
    }
}
