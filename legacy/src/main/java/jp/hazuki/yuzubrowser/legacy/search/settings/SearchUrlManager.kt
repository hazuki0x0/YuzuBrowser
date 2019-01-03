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

package jp.hazuki.yuzubrowser.legacy.search.settings

import android.content.Context
import com.squareup.moshi.Moshi
import okio.Okio
import java.io.File
import java.io.IOException

class SearchUrlManager(context: Context, private val moshi: Moshi) : ArrayList<SearchUrl>() {

    val file: File
    var selectedId = 0
    var idCount = 0

    companion object {
        private const val NAME = "searchUrl.json"
    }

    init {
        file = File(context.filesDir, NAME)
        load()
    }

    fun load() {
        clear()
        try {
            val items = Okio.buffer(Okio.source(file)).use {
                val adapter = moshi.adapter(SearchSettings::class.java)
                adapter.fromJson(it)
            }
            items?.let {
                selectedId = it.selectedId
                idCount = it.idCount
                addAll(it.items)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            Okio.buffer(Okio.sink(file)).use {
                val adapter = moshi.adapter(SearchSettings::class.java)
                adapter.toJson(it, SearchSettings(selectedId, idCount, this))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getSelectedIndex(): Int {
        forEachIndexed { index, searchUrl ->
            if (searchUrl.id == selectedId) {
                return index
            }
        }
        return 0
    }

    override fun add(index: Int, element: SearchUrl) {
        if (element.id < 0) {
            element.id = ++idCount
        }
        super.add(index, element)
    }

    override fun add(element: SearchUrl): Boolean {
        if (element.id < 0) {
            element.id = ++idCount
        }
        return super.add(element)
    }
}