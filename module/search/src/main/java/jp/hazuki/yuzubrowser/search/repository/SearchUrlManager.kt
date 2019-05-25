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

import android.content.Context
import com.squareup.moshi.Moshi
import jp.hazuki.yuzubrowser.search.domain.ISearchUrlRepository
import jp.hazuki.yuzubrowser.search.model.provider.SearchSettings
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException

class SearchUrlManager(context: Context, private val moshi: Moshi) : ISearchUrlRepository {

    val file = File(context.filesDir, NAME)

    override fun load(): SearchSettings {
        try {
            val items = file.source().buffer().use {
                val adapter = moshi.adapter(SearchSettings::class.java)
                adapter.fromJson(it)
            }
            if (items?.items != null) {
                return items
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return SearchSettings(0, 0, listOf())
    }

    override fun save(settings: SearchSettings) {
        try {
            file.sink().buffer().use {
                val adapter = moshi.adapter(SearchSettings::class.java)
                adapter.toJson(it, settings)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val NAME = "searchUrl.json"
    }
}
