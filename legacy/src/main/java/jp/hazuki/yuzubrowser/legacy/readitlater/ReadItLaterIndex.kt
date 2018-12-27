/*
 * Copyright (C) 2017 Hazuki
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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.legacy.utils.JsonUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

class ReadItLaterIndex(root: File) : ArrayList<ReadItem>() {

    companion object {
        const val TIME = "time"
        const val TITLE = "title"
        const val URL = "url"
    }

    private val indexFile: File = File(root, "index")

    init {
        load()
    }

    fun load() {
        clear()
        try {
            FileInputStream(indexFile).use {
                val parser = JsonUtils.getFactory().createParser(it)
                if (parser.nextToken() == JsonToken.START_ARRAY) {
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        val item = read(parser)
                        if (item != null) {
                            add(item)
                        }
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    fun save() {
        FileOutputStream(indexFile).use {
            val generator = JsonUtils.getFactory().createGenerator(it)
            generator.writeStartArray()
            for (item in this) {
                generator.writeStartObject()
                generator.writeNumberField(TIME, item.time)
                generator.writeStringField(TITLE, item.title)
                generator.writeStringField(URL, item.url)
                generator.writeEndObject()
            }
            generator.writeEndArray()
            generator.close()
        }
    }

    private fun read(parser: JsonParser): ReadItem? {
        if (parser.currentToken == JsonToken.START_OBJECT) {
            var time = -1L
            var title: String? = null
            var url: String? = null
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                when (parser.currentName) {
                    TIME -> time = parser.nextLongValue(-1)
                    TITLE -> title = parser.nextTextValue()
                    URL -> url = parser.nextTextValue()
                    else -> {
                        parser.nextToken()
                        if (parser.currentToken == JsonToken.START_ARRAY || parser.currentToken == JsonToken.START_OBJECT) {
                            parser.skipChildren()
                        }
                    }
                }
            }
            if (time > 0 && title != null && url != null) {
                return ReadItem(time, url, title)
            }
        }
        return null
    }
}