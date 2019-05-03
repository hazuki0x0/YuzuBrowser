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

package jp.hazuki.yuzubrowser.bookmark.item

import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.bookmark.util.BookmarkIdGenerator
import java.io.IOException
import java.io.Serializable

abstract class BookmarkItem(var title: String?, val id: Long) : Serializable {

    protected abstract val type: Int

    @Throws(IOException::class)
    fun write(writer: JsonWriter): Boolean {
        writer.beginObject()
        writer.name(COLUMN_NAME_TYPE)
        writer.value(type)
        writer.name(COLUMN_NAME_TITLE)
        writer.value(title)
        writer.name(COLUMN_NAME_ID)
        writer.value(id)
        val ret = writeMain(writer)
        writer.endObject()
        return ret
    }

    @Throws(IOException::class)
    protected fun read(reader: JsonReader, parent: BookmarkFolder): BookmarkItem? {
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return null
        reader.beginObject()
        var id = -1
        var itemId = -1L
        var title: String? = null
        var lastName: String? = null
        loop@ while (reader.hasNext()) {
            when (val name = reader.nextName()) {
                COLUMN_NAME_TYPE -> {
                    if (reader.peek() != JsonReader.Token.NUMBER) return null
                    id = reader.nextInt()
                }
                COLUMN_NAME_ID -> {
                    if (reader.peek() != JsonReader.Token.NUMBER) return null
                    itemId = reader.nextLong()
                }
                COLUMN_NAME_TITLE -> {
                    if (reader.peek() == JsonReader.Token.STRING) {
                        title = reader.nextString()
                    } else {
                        reader.skipValue()
                    }
                }
                else -> {
                    lastName = name
                    break@loop
                }
            }
        }
        if (itemId < 0) itemId = BookmarkIdGenerator.getNewId()
        if (id < 0 || title == null) return null

        val item = when (id) {
            BookmarkFolder.BOOKMARK_ITEM_ID -> BookmarkFolder(title, parent, itemId)
            BookmarkSite.BOOKMARK_ITEM_ID -> BookmarkSite(title, itemId)
            else -> return null
        }
        item.readMain(lastName, reader)
        while (reader.peek() != JsonReader.Token.END_OBJECT) {
            reader.skipValue()
        }
        reader.endObject()
        return item
    }

    @Throws(IOException::class)
    protected abstract fun writeMain(writer: JsonWriter): Boolean

    @Throws(IOException::class)
    protected abstract fun readMain(name: String?, reader: JsonReader): Boolean

    companion object {
        protected const val COLUMN_NAME_TYPE = "0"
        protected const val COLUMN_NAME_TITLE = "1"
        protected const val COLUMN_NAME_ID = "3"
        protected const val COLUMN_NAME_DATA = "2"
    }
}
