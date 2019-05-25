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

package jp.hazuki.yuzubrowser.search.domain.usecase

import jp.hazuki.yuzubrowser.search.domain.ISearchUrlRepository
import jp.hazuki.yuzubrowser.search.model.provider.SearchSuggestProviders
import jp.hazuki.yuzubrowser.search.model.provider.SearchUrl
import java.util.Collections.swap

internal class SearchSettingsViewUseCase(
    private val repository: ISearchUrlRepository
) {
    private var modified = false

    private val providers = SearchSuggestProviders(repository.load())

    val list: List<SearchUrl>
        get() = providers.items

    fun move(fromPosition: Int, toPosition: Int) {
        modified = true
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                swap(providers.items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                swap(providers.items, i, i - 1)
            }
        }
    }

    fun moveUp(index: Int): Boolean {
        if (index <= 0) return false

        modified = true
        swap(providers.items, index, index - 1)
        return true
    }

    fun moveDown(index: Int): Boolean {
        if (index >= providers.items.lastIndex) return false

        modified = true
        swap(providers.items, index, index + 1)
        return true
    }

    operator fun get(index: Int) = providers.items[index]

    operator fun set(index: Int, url: SearchUrl) {
        modified = true
        providers.items[index] = url
    }

    fun add(url: SearchUrl) {
        modified = true
        providers.items.add(url)
    }

    fun add(index: Int, url: SearchUrl) {
        modified = true
        providers.items.add(index, url)
    }

    fun removeAt(index: Int): SearchUrl {
        modified = true
        return providers.removeAt(index)
    }

    fun commitIfNeed() {
        if (modified) commit()
    }

    fun commit() {
        repository.save(providers.toSettings())
    }
}
