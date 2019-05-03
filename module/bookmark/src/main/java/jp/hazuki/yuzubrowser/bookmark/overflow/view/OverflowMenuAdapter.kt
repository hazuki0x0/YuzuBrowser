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

package jp.hazuki.yuzubrowser.bookmark.overflow.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.bookmark.databinding.BookmarkOverlowMenuItemBinding
import jp.hazuki.yuzubrowser.bookmark.overflow.model.OverflowMenuModel

class OverflowMenuAdapter : RecyclerView.Adapter<OverflowMenuAdapter.OverflowMenuHolder>() {

    val list: MutableList<OverflowMenuModel> = mutableListOf()
    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: OverflowMenuHolder, position: Int) {
        holder.binding.model = list[position]
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverflowMenuHolder {
        val inflater = LayoutInflater.from(parent.context)
        return OverflowMenuHolder(BookmarkOverlowMenuItemBinding.inflate(inflater, parent, false))
    }


    class OverflowMenuHolder(var binding: BookmarkOverlowMenuItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                binding.model?.itemClick()
            }
        }
    }
}
