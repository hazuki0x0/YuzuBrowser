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

package jp.hazuki.yuzubrowser.search.repository

import android.app.Application
import android.app.SearchManager
import android.content.ContentValues
import android.net.Uri
import jp.hazuki.yuzubrowser.search.domain.ISuggestRepository
import jp.hazuki.yuzubrowser.search.model.SearchSuggestModel
import jp.hazuki.yuzubrowser.ui.provider.ISuggestProvider

class SuggestRepository(
    private val application: Application,
    private val suggestProvider: ISuggestProvider
) : ISuggestRepository {

    private var cachedType = -1
    private lateinit var baseUri: Uri

    override fun getSearchQuery(suggestType: Int, query: String): List<SearchSuggestModel> {
        setUri(suggestType)

        val uri = baseUri.buildUpon().appendQueryParameter("q", query).build()

        val suggestions = mutableListOf<SearchSuggestModel>()

        application.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val colQuery = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY)
            val colHistory = c.getColumnIndex(suggestProvider.suggestHistory)
            while (c.moveToNext()) {
                suggestions.add(SearchSuggestModel.SuggestModel(c.getString(colQuery), c.getInt(colHistory) == 1))
            }
        }
        return suggestions
    }

    override fun insert(suggestType: Int, query: String) {
        setUri(suggestType)

        application.contentResolver
            .insert(baseUri, ContentValues().apply { put(SearchManager.SUGGEST_COLUMN_QUERY, query) })
    }

    override fun delete(suggestType: Int, query: String) {
        setUri(suggestType)

        application.contentResolver
            .delete(baseUri, "${SearchManager.SUGGEST_COLUMN_QUERY} = ?", arrayOf(query))
    }

    private fun setUri(suggestType: Int) {
        if (cachedType != suggestType) {
            baseUri = when (suggestType) {
                0 -> suggestProvider.uriNormal
                1 -> suggestProvider.uriNet
                2 -> suggestProvider.uriLocal
                3 -> suggestProvider.uriNone
                else -> suggestProvider.uriNormal
            }
        }
    }
}
