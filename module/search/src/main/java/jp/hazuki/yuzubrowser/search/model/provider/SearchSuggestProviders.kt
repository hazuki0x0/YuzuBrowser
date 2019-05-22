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

package jp.hazuki.yuzubrowser.search.model.provider

class SearchSuggestProviders(
    var selectedId: Int,
    private var idCount: Int,
    val items: MutableList<SearchUrl>
) {
    constructor(settings: SearchSettings) : this(settings.selectedId, settings.idCount, settings.items.toMutableList())

    val urls: List<SearchUrl> = items

    fun add(element: SearchUrl): Boolean {
        if (element.id < 0) {
            element.id = ++idCount
        }
        return items.add(element)
    }

    fun add(index: Int, element: SearchUrl) {
        if (element.id < 0) {
            element.id = ++idCount
        }
        items.add(index, element)
    }

    fun getSelectedIndex(): Int {
        items.forEachIndexed { index, searchUrl ->
            if (searchUrl.id == selectedId) {
                return index
            }
        }
        return 0
    }

    operator fun get(index: Int): SearchUrl {
        return items[index]
    }

    operator fun set(index: Int, searchUrl: SearchUrl) {
        items[index] = searchUrl
    }

    fun removeAt(index: Int) = items.removeAt(index)

    val size: Int
        get() = items.size

    fun toSettings(): SearchSettings {
        return SearchSettings(selectedId, idCount, items.toList())
    }
}
