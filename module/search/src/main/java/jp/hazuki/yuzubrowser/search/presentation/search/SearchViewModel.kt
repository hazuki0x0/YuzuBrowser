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

import android.app.Application
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.search.domain.usecase.SearchViewUseCase
import jp.hazuki.yuzubrowser.search.model.SearchSuggestModel
import jp.hazuki.yuzubrowser.search.repository.SearchPrefsProvider
import jp.hazuki.yuzubrowser.ui.extensions.addOnPropertyChangedCallback
import jp.hazuki.yuzubrowser.ui.extensions.decodePunyCodeUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async

internal class SearchViewModel(
    application: Application,
    private val useCase: SearchViewUseCase,
    private val provider: SearchPrefsProvider
) : AndroidViewModel(application) {
    var initQuery: String? = null
        private set
    var decodedInitQuery: String? = null
        private set
    var query = ""
        private set

    private var queryJob: Job? = null

    val suggestModels = MutableLiveData<List<SearchSuggestModel>>()

    val suggestProviders = useCase.loadSuggestProviders()

    val providerSelection = ObservableInt(-1)

    init {
        providerSelection.addOnPropertyChangedCallback { _, _ ->
            suggestProviders.selectedId = providerSelection.get()
        }
    }

    fun setQuery(query: String) {
        this.query = query
        queryJob?.cancel()
        queryJob = ui {
            val prefs = provider.get()
            val search = async(Dispatchers.Default) { useCase.getSearchQuery(query) }
            val histories = if (prefs.searchSuggestHistories) async { useCase.getHistoryQuery(query) } else null
            val bookmarks = if (prefs.searchSuggestBookmarks) async { useCase.getBookmarkQuery(query) } else null

            val suggestions = mutableListOf<SearchSuggestModel>()
            suggestions.addAll(search.await())
            histories?.run { suggestions.addAll(await()) }
            bookmarks?.run { suggestions.addAll(await()) }

            queryJob = null
            suggestModels.postValue(suggestions)
        }
    }

    fun getFinishResult(mode: Int): FinishResult {
        if (!provider.get().isPrivateMode && mode != SEARCH_MODE_URL) {
            useCase.saveQuery(query)
        }
        return FinishResult(query, suggestProviders[providerSelection.get()].url)
    }

    fun setInitQuery(query: String) {
        initQuery = query
        decodedInitQuery = query.decodePunyCodeUrl()
    }

    fun deleteQuery(query: String) {
        useCase.saveQuery(query)
    }

    fun saveProvider() {
        useCase.saveSuggestProviders(suggestProviders)
    }

    companion object {
        const val SEARCH_MODE_AUTO = 0
        const val SEARCH_MODE_URL = 1
        const val SEARCH_MODE_WORD = 2
    }

    class FinishResult(val query: String, val url: String)

    class Factory(
        private val application: Application,
        private val useCase: SearchViewUseCase,
        private val provider: SearchPrefsProvider
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(application, useCase, provider) as T
        }
    }
}
