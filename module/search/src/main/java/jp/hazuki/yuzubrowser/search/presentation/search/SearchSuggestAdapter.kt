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

package jp.hazuki.yuzubrowser.search.presentation.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.search.R
import jp.hazuki.yuzubrowser.search.databinding.SearchActivitySuggestHistoryBinding
import jp.hazuki.yuzubrowser.search.databinding.SearchActivtySuggestSuggestBinding
import jp.hazuki.yuzubrowser.search.model.SearchSuggestModel

class SearchSuggestAdapter : RecyclerView.Adapter<SearchSuggestAdapter.SuggestHolder>() {
    val list: MutableList<SearchSuggestModel> = mutableListOf()

    var listener: OnSearchSelectedListener? = null

    override fun getItemCount() = list.size

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is SearchSuggestModel.SuggestModel -> TYPE_SUGGEST
            is SearchSuggestModel.HistoryModel -> TYPE_HISTORY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SuggestHolder(LayoutInflater.from(parent.context).inflate(getLayoutId(viewType), parent, false))

    override fun onBindViewHolder(holder: SuggestHolder, position: Int) {
        when (val item = list[position]) {
            is SearchSuggestModel.SuggestModel -> {
                (holder.binding as SearchActivtySuggestSuggestBinding).model = item
                holder.binding.textView.setOnClickListener {
                    listener?.onSelectedQuery(item.suggest)
                }
                holder.binding.imageButton.setOnClickListener {
                    listener?.onInputQuery(item.suggest)
                }
                if (item.suggestHistory) {
                    holder.binding.textView.setOnLongClickListener {
                        if (item.suggestHistory) listener?.onDeleteQuery(item.suggest)
                        true
                    }
                } else {
                    holder.binding.textView.setOnLongClickListener(null)
                }
            }
            is SearchSuggestModel.HistoryModel -> {
                (holder.binding as SearchActivitySuggestHistoryBinding).model = item
                holder.binding.background.setOnClickListener {
                    listener?.onSelectedQuery(item.url)
                }
                holder.binding.inputImageButton.setOnClickListener {
                    listener?.onInputQuery(item.url)
                }
            }
        }
    }

    private fun getLayoutId(type: Int): Int {
        return when (type) {
            TYPE_SUGGEST -> R.layout.search_activty_suggest_suggest
            TYPE_HISTORY -> R.layout.search_activity_suggest_history
            else -> throw IllegalArgumentException("Unknown viewType $type")
        }
    }

    inner class SuggestHolder(v: View) : RecyclerView.ViewHolder(v) {
        val binding: ViewDataBinding = DataBindingUtil.bind(v)!!
    }

    companion object {
        private const val TYPE_SUGGEST = 0
        private const val TYPE_HISTORY = 1
    }

    interface OnSearchSelectedListener {
        fun onSelectedQuery(query: String)

        fun onInputQuery(query: String)

        fun onDeleteQuery(query: String)
    }
}
