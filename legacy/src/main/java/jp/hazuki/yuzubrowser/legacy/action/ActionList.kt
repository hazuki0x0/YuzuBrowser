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

package jp.hazuki.yuzubrowser.legacy.action

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.utils.util.JsonConvertable
import okio.Buffer
import java.io.IOException
import java.util.*

class ActionList : ArrayList<Action>, Parcelable, JsonConvertable {

    constructor() : super()

    constructor(jsonStr: String) : super() {
        fromJsonString(jsonStr)
    }

    constructor(source: Parcel) : super() {
        source.readList(this, Action::class.java.classLoader)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeList(this)
    }

    fun add(`object`: SingleAction): Boolean {
        return add(Action(`object`))
    }

    @Throws(IOException::class)
    fun writeAction(writer: JsonWriter) {
        writer.beginArray()
        for (action in this) {
            action.writeAction(writer)
        }
        writer.endArray()
    }

    @Throws(IOException::class)
    fun loadAction(reader: JsonReader): Boolean {
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) return false
        reader.beginArray()
        while (reader.hasNext()) {
            val action = Action()
            if (!action.loadAction(reader)) {
                return if (reader.peek() == JsonReader.Token.END_ARRAY)
                    break
                else
                    false
            }
            add(action)
        }
        reader.endArray()
        return true
    }

    override fun toJsonString(): String? {
        val buffer = Buffer()
        JsonWriter.of(buffer).use {
            writeAction(it)
        }
        return buffer.readUtf8()
    }

    override fun fromJsonString(str: String): Boolean {
        clear()

        JsonReader.of(Buffer().writeUtf8(str)).use {
            return loadAction(it)
        }
    }

    companion object {
        const val serialVersionUID = 4454998466204378989L

        @JvmField
        val CREATOR: Parcelable.Creator<ActionList> = object : Parcelable.Creator<ActionList> {
            override fun createFromParcel(source: Parcel): ActionList {
                return ActionList(source)
            }

            override fun newArray(size: Int): Array<ActionList?> {
                return arrayOfNulls(size)
            }
        }
    }
}
