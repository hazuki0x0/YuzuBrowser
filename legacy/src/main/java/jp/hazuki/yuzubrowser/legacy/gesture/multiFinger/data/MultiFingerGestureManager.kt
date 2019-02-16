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

package jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.data

import android.content.Context
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils
import jp.hazuki.yuzubrowser.legacy.action.Action
import okio.Okio
import okio.buffer
import okio.sink
import okio.source
import java.io.IOException
import java.util.*

class MultiFingerGestureManager(context: Context) {
    private val jsonFile = context.getFileStreamPath(FILENAME)
    val gestureItems: MutableList<MultiFingerGestureItem> = ArrayList()

    init {
        load()
    }

    fun add(item: MultiFingerGestureItem) {
        gestureItems.add(item)
        save()
    }

    fun add(index: Int, item: MultiFingerGestureItem) {
        gestureItems.add(index, item)
        save()
    }

    operator fun set(index: Int, item: MultiFingerGestureItem) {
        gestureItems[index] = item
        save()
    }

    fun remove(index: Int): MultiFingerGestureItem {
        val item = gestureItems.removeAt(index)
        save()
        return item
    }

    fun remove(item: MultiFingerGestureItem) {
        if (gestureItems.remove(item))
            save()
    }

    fun move(from: Int, to: Int) {
        ArrayUtils.move(gestureItems, from, to)
        save()
    }

    fun indexOf(item: MultiFingerGestureItem): Int {
        return gestureItems.indexOf(item)
    }

    private fun load() {
        gestureItems.clear()
        if (jsonFile == null || !jsonFile.exists() || jsonFile.isDirectory) return
        try {
            JsonReader.of(jsonFile.source().buffer()).use { reader ->
                if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                            reader.beginObject()
                            val item = MultiFingerGestureItem()
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    JSON_FINGERS -> {
                                        item.fingers = reader.nextInt()
                                    }
                                    JSON_TRACES -> if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                                        reader.beginArray()
                                        while (reader.hasNext()) {
                                            item.addTrace(reader.nextInt())
                                        }
                                        reader.endArray()
                                    } else {
                                        reader.skipValue()
                                    }
                                    JSON_ACTION -> {
                                        val action = Action()
                                        action.loadAction(reader)
                                        item.action = action
                                    }
                                    else -> reader.skipValue()
                                }
                            }
                            gestureItems.add(item)
                            reader.endObject()
                        } else {
                            reader.skipValue()
                        }
                    }
                    reader.endArray()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            JsonWriter.of(jsonFile.sink().buffer()).use { writer ->
                writer.beginArray()
                gestureItems.forEach { item ->
                    writer.beginObject()

                    writer.name(JSON_FINGERS)
                    writer.value(item.fingers)

                    // finger actions
                    writer.name(JSON_TRACES)
                    writer.beginArray()
                    item.traces.forEach { writer.value(it) }
                    writer.endArray()

                    // browser action
                    writer.name(JSON_ACTION)
                    item.action.writeAction(writer)

                    writer.endObject()
                }
                writer.endArray()
                writer.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        private const val FILENAME = "multiFingerGes_1.dat"

        private const val JSON_FINGERS = "0"
        private const val JSON_TRACES = "1"
        private const val JSON_ACTION = "2"
    }
}
