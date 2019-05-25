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

package jp.hazuki.yuzubrowser.search.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.search.databinding.SearchUrlListEditItemBinding
import jp.hazuki.yuzubrowser.search.model.provider.SearchUrl

class SearchUrlAdapter(
    private val faviconManager: FaviconManager,
    private val listener: OnSearchUrlClickListener
) : RecyclerView.Adapter<SearchUrlAdapter.ViewHolder>() {
    val list = mutableListOf<SearchUrl>()

    override fun getItemCount() = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(SearchUrlListEditItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val binding = holder.binding
        binding.model = item

        val favicon = faviconManager[item.url]
        if (favicon != null) {
            binding.iconColorView.setFavicon(favicon)
        } else {
            binding.iconColorView.setSearchUrl(item)
        }

        binding.root.setOnClickListener { listener.onEdit(holder.adapterPosition, item) }
        binding.menuImageButton.setOnClickListener { listener.onOpenMenu(it, holder.adapterPosition) }
    }

    class ViewHolder(val binding: SearchUrlListEditItemBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnSearchUrlClickListener {
        fun onEdit(position: Int, searchUrl: SearchUrl)

        fun onOpenMenu(view: View, position: Int)
    }
}
