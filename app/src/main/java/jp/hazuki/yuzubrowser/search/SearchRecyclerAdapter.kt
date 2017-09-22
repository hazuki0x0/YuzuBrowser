/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.search.suggest.SuggestHistory
import jp.hazuki.yuzubrowser.search.suggest.SuggestItem
import jp.hazuki.yuzubrowser.utils.view.recycler.ArrayRecyclerAdapter
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.search_activity_list_history_item.*
import kotlinx.android.synthetic.main.search_activity_list_item.*

class SearchRecyclerAdapter(context: Context, list: List<SuggestItem>, private val listener: OnSuggestSelectListener) : ArrayRecyclerAdapter<SuggestItem, SearchRecyclerAdapter.SuggestionViewHolder>(context, list, null) {

    companion object {
        private const val TYPE_SUGGESTION = 0
        private const val TYPE_HISTORY = 1
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        return when (viewType) {
            TYPE_HISTORY -> HistoryViewHolder(inflater.inflate(R.layout.search_activity_list_history_item, parent, false), this)
            else -> SuggestionViewHolder(inflater.inflate(R.layout.search_activity_list_item, parent, false), this)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (get(position)) {
            is SuggestHistory -> TYPE_HISTORY
            else -> TYPE_SUGGESTION
        }
    }

    fun onSelectSuggest(query: String) {
        listener.onSelectSuggest(query)
    }

    fun onInputSuggest(query: String) {
        listener.onInputSuggest(query)
    }

    fun onLongClicked(query: String) {
        listener.onLongClicked(query)
    }

    open class SuggestionViewHolder(override val containerView: View?, adapter: SearchRecyclerAdapter) : ArrayRecyclerAdapter.ArrayViewHolder<SuggestItem>(containerView, adapter), LayoutContainer {

        init {
            @Suppress("LeakingThis")
            viewInit(adapter)
        }

        open internal fun viewInit(adapter: SearchRecyclerAdapter) {
            imageButton.setOnClickListener { adapter.onInputSuggest(item.title) }

            textView.apply {
                setOnClickListener { adapter.onSelectSuggest(item.title) }
                setOnLongClickListener {
                    if (item.suggestHistory)
                        adapter.onLongClicked(item.title)
                    true
                }
            }
        }

        override fun setUp(item: SuggestItem) {
            super.setUp(item)
            setItem(item)
        }

        open internal fun setItem(item: SuggestItem) {
            textView.text = item.title
        }
    }

    class HistoryViewHolder(view: View?, adapter: SearchRecyclerAdapter) : SuggestionViewHolder(view, adapter) {

        override fun viewInit(adapter: SearchRecyclerAdapter) {
            inputImageButton.setOnClickListener {
                adapter.onInputSuggest((item as SuggestHistory).url)
            }

            itemView.apply {
                setOnClickListener { adapter.onSelectSuggest((item as SuggestHistory).url) }
            }
        }

        override fun setItem(item: SuggestItem) {
            (item as SuggestHistory).run {
                titleTextView.text = title
                urlTextView.text = url
            }
        }
    }

    interface OnSuggestSelectListener {
        fun onSelectSuggest(query: String)

        fun onInputSuggest(query: String)

        fun onLongClicked(query: String)
    }
}
