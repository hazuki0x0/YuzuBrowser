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

package jp.hazuki.yuzubrowser.legacy.action.manager

import android.content.Context
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.action.ActionFile
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import java.io.File
import java.io.IOException
import java.util.*

class SoftButtonActionArrayFile(private val FOLDER_NAME: String, private val id: Int) : ActionFile() {
    val list: MutableList<SoftButtonActionFile> = ArrayList()

    fun add(action: SingleAction) {
        val array = SoftButtonActionFile()
        array.press.add(action)
        list.add(array)
    }

    fun add(action: SingleAction, longPress: SingleAction) {
        val array = SoftButtonActionFile()
        array.press.add(action)
        array.lpress.add(longPress)
        list.add(array)
    }

    fun add(action: SoftButtonActionFile) {
        val array = SoftButtonActionFile()
        array.press.addAll(action.press)
        array.lpress.addAll(action.lpress)
        array.up.addAll(action.up)
        array.down.addAll(action.down)
        array.left.addAll(action.left)
        array.right.addAll(action.right)
        list.add(array)
    }

    fun getActionList(no: Int): SoftButtonActionFile {
        if (no < 0)
            throw IllegalArgumentException("no < 0")
        expand(no + 1)
        return list[no]
    }

    fun expand(size: Int): Int {
        if (size < 0)
            throw IllegalArgumentException("size < 0")
        for (i in list.size - size..-1)
            list.add(SoftButtonActionFile())
        return list.size
    }

    override fun getFile(context: Context): File {
        return File(context.getDir(FOLDER_NAME, Context.MODE_PRIVATE), id.toString() + ".dat")
    }

    override fun reset() {
        list.clear()
    }

    @Throws(IOException::class)
    override fun load(reader: JsonReader): Boolean {
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) return false
        reader.beginArray()
        while (reader.hasNext()) {
            val action = SoftButtonActionFile()
            if (!action.load(reader)) {
                return if (reader.peek() == JsonReader.Token.END_ARRAY)
                    break
                else
                    false
            }
            list.add(action)
        }
        reader.endArray()
        return true
    }

    @Throws(IOException::class)
    override fun write(writer: JsonWriter): Boolean {
        writer.beginArray()
        list.forEach { it.write(writer) }
        writer.endArray()
        return true
    }

    companion object {
        private const val serialVersionUID = -8451972340596132660L
    }

}
