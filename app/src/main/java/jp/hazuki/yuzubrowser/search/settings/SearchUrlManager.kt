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

package jp.hazuki.yuzubrowser.search.settings

import android.content.Context
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.utils.JsonUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class SearchUrlManager(context: Context) : ArrayList<SearchUrl>() {

    val file: File
    var selectedId = 0
    var idCount = 0

    companion object {
        private val NAME = "searchUrl.json"

        private val FIELD_SELECTED = "sel"
        private val FIELD_ITEMS = "item"
        private val FIELD_ID_CURRENT = "idc"

        private val FIELD_TITLE = "0"
        private val FIELD_URL = "1"
        private val FIELD_COLOR = "2"
        private val FIELD_ID = "3"
    }

    init {
        file = File(context.filesDir, NAME)
        load()
    }

    fun load() {
        clear()
        try {
            FileInputStream(file).use {
                val parser = JsonUtils.getFactory().createParser(it)
                if (parser.nextToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        when (parser.currentName) {
                            FIELD_SELECTED -> selectedId = parser.nextIntValue(0)
                            FIELD_ID_CURRENT -> idCount = parser.nextIntValue(0)
                            FIELD_ITEMS -> parseItems(parser)
                            else -> {
                                parser.nextToken()
                                if (parser.currentToken == JsonToken.START_OBJECT || parser.currentToken == JsonToken.START_ARRAY) {
                                    parser.skipChildren()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun parseItems(parser: JsonParser) {
        if (parser.nextToken() == JsonToken.START_ARRAY) {
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.currentToken == JsonToken.START_OBJECT) {
                    var title: String? = null
                    var url: String? = null
                    var color = 0
                    var id = -1
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        when (parser.currentName) {
                            FIELD_TITLE -> title = parser.nextTextValue()
                            FIELD_URL -> url = parser.nextTextValue()
                            FIELD_COLOR -> color = parser.nextIntValue(0)
                            FIELD_ID -> id = parser.nextIntValue(-1)
                            else -> {
                                parser.nextToken()
                                if (parser.currentToken == JsonToken.START_OBJECT || parser.currentToken == JsonToken.START_ARRAY) {
                                    parser.skipChildren()
                                }
                            }
                        }
                    }
                    if (title != null && url != null) {
                        if (id < 0) {
                            id = ++idCount
                        }
                        add(SearchUrl(id, title, url, color))
                    }
                }
            }
        }
    }

    fun save() {
        FileOutputStream(file).use {
            val generator = JsonUtils.getFactory().createGenerator(it)
            generator.writeStartObject()
            generator.writeNumberField(FIELD_SELECTED, selectedId)
            generator.writeNumberField(FIELD_ID_CURRENT, idCount)
            generator.writeArrayFieldStart(FIELD_ITEMS)
            for (item in this) {
                generator.writeStartObject()
                generator.writeStringField(FIELD_TITLE, item.title)
                generator.writeStringField(FIELD_URL, item.url)
                generator.writeNumberField(FIELD_COLOR, item.color)
                generator.writeNumberField(FIELD_ID, item.id)
                generator.writeEndObject()
            }
            generator.writeEndArray()
            generator.writeEndObject()
            generator.close()
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

    override fun removeAt(index: Int): SearchUrl {
        return super.removeAt(index)
    }
}