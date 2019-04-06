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

package jp.hazuki.yuzubrowser.legacy.readitlater

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.Okio
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException

class ReadItLaterIndex(private val moshi: Moshi, root: File) : ArrayList<ReadItem>() {

    private val indexFile: File = File(root, "index")

    init {
        load()
    }

    fun load() {
        clear()
        try {
            val items = indexFile.source().buffer().use {
                val type = Types.newParameterizedType(List::class.java, ReadItem::class.java)
                val adapter = moshi.adapter<List<ReadItem>>(type)
                adapter.fromJson(it)
            }
            items?.let { addAll(it) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            indexFile.sink().buffer().use {
                val type = Types.newParameterizedType(List::class.java, ReadItem::class.java)
                val adapter = moshi.adapter<List<ReadItem>>(type)
                adapter.toJson(it, this)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}