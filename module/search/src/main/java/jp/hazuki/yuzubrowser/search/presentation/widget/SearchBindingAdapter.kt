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

package jp.hazuki.yuzubrowser.search.presentation.widget

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.search.model.SearchSuggestModel
import jp.hazuki.yuzubrowser.search.model.provider.SearchUrl
import jp.hazuki.yuzubrowser.search.presentation.search.SearchSuggestAdapter
import jp.hazuki.yuzubrowser.search.presentation.settings.SearchUrlAdapter
import jp.hazuki.yuzubrowser.search.presentation.settings.SearchUrlDiffCallback

@BindingMethods(BindingMethod(type = SearchButton::class, attribute = "callback", method = "setActionCallback"))
class SearchBindingAdapter

@BindingAdapter("viewmodels")
fun RecyclerView.setViewModels(suggestModels: List<SearchSuggestModel>?) {
    if (suggestModels != null) {
        val adapter = adapter as SearchSuggestAdapter
        adapter.list.run {
            clear()
            addAll(suggestModels)
        }
        adapter.notifyDataSetChanged()
        val layoutManager = layoutManager as LinearLayoutManager
        if (layoutManager.reverseLayout && adapter.itemCount > 0) {
            scrollToPosition(0)
        }
    }
}

@BindingAdapter("viewmodels")
fun RecyclerView.setSearchUrl(searchUrls: List<SearchUrl>?) {
    if (searchUrls == null) return

    val adapter = adapter as SearchUrlAdapter
    val diff = DiffUtil.calculateDiff(SearchUrlDiffCallback(adapter.list, searchUrls), true)
    adapter.list.run {
        clear()
        addAll(searchUrls)
    }
    diff.dispatchUpdatesTo(adapter)
}

@BindingAdapter("callback")
fun EditText.setSearchCallback(callback: SearchButton.Callback) {
    setOnEditorActionListener { _, actionId, _ ->
        if (EditorInfo.IME_ACTION_GO == actionId) {
            callback.autoSearch()
            return@setOnEditorActionListener true
        }
        return@setOnEditorActionListener false
    }
}
