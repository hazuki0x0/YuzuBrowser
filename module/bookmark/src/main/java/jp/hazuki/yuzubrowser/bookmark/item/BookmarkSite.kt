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
import java.io.IOException
import java.io.Serializable

class BookmarkSite : BookmarkItem, Serializable {

    lateinit var url: String

    override val type: Int
        get() = BOOKMARK_ITEM_ID

    constructor(title: String, id: Long) : super(title, id)

    constructor(title: String, url: String, id: Long) : super(title, id) {
        this.url = url
    }

    @Throws(IOException::class)
    override fun writeMain(writer: JsonWriter): Boolean {
        writer.name(COLUMN_NAME_URL)
        writer.value(url)
        return true
    }

    @Throws(IOException::class)
    override fun readMain(name: String?, reader: JsonReader): Boolean {
        if (COLUMN_NAME_URL != name) return false
        if (reader.peek() != JsonReader.Token.STRING) return false
        url = reader.nextString()
        return true
    }

    companion object {
        protected const val COLUMN_NAME_URL = "2"
        const val BOOKMARK_ITEM_ID = 2
    }

}
