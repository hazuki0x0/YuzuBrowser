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

package jp.hazuki.yuzubrowser.legacy.bookmark

import android.text.TextUtils
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.bookmark.util.BookmarkIdGenerator
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import java.io.IOException
import java.io.Serializable
import java.util.*

class BookmarkFolder : BookmarkItem, Serializable {

    var parent: BookmarkFolder? = null
    internal val list: ArrayList<BookmarkItem>

    val itemList: List<BookmarkItem>
        get() = list

    override val type: Int
        get() = BOOKMARK_ITEM_ID

    constructor(title: String?, parent: BookmarkFolder?, id: Long) : super(title, id) {
        this.list = ArrayList()
        this.parent = parent
    }

    constructor(list: List<MainTabData>, id: Long) : super(null, id) {
        this.list = ArrayList(list.size)
        for (tab in list)
            if (!TextUtils.isEmpty(tab.url))
                this.list.add(BookmarkSite(if (tab.title != null) tab.title else tab.url, tab.url, BookmarkIdGenerator.getNewId()))
        this.parent = null
    }

    fun add(item: BookmarkItem) {
        list.add(item)
    }

    fun add(folder: BookmarkFolder) {
        list.add(folder)
    }

    fun addFirst(folder: BookmarkFolder) {
        list.add(0, folder)
    }

    operator fun get(index: Int): BookmarkItem {
        return list[index]
    }

    fun size(): Int {
        return list.size
    }

    fun clear() {
        list.clear()
    }

    @Throws(IOException::class)
    override fun writeMain(writer: JsonWriter): Boolean {
        writer.name(COLUMN_NAME_LIST)
        writer.beginArray()
        list.forEach {
            if (!it.write(writer)) return false
        }
        writer.endArray()
        return true
    }

    @Throws(IOException::class)
    override fun readMain(name: String?, reader: JsonReader): Boolean {
        if (COLUMN_NAME_LIST != name) return false
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) return false
        reader.beginArray()
        while (reader.hasNext()) {
            list.add(read(reader, this) ?: return false)
        }
        reader.endArray()
        return true
    }

    @Throws(IOException::class)
    fun writeForRoot(writer: JsonWriter): Boolean {
        writer.beginArray()
        list.forEach {
            if (!it.write(writer)) return false
        }
        writer.endArray()
        return true
    }

    @Throws(IOException::class)
    fun readForRoot(reader: JsonReader): Boolean {
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) return false
        reader.beginArray()
        while (reader.hasNext()) {
            list.add(read(reader, this) ?: return false)
        }
        reader.endArray()
        return true
    }

    companion object {
        protected const val COLUMN_NAME_LIST = "2"
        const val BOOKMARK_ITEM_ID = 1
    }
}
