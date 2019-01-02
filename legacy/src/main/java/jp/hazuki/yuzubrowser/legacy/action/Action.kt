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

class Action : ArrayList<SingleAction>, Parcelable, JsonConvertable {

    constructor() : super(1)

    constructor(action: Action) : super(action)

    constructor(action: SingleAction) : super(1) {
        add(action)
    }

    constructor(jsonStr: String) : super(1) {
        fromJsonString(jsonStr)
    }

    constructor(actions: Collection<SingleAction>) : super(actions)

    constructor(source: Parcel) : super() {
        source.readList(this, SingleAction::class.java.classLoader)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeList(this)
    }

    @Throws(IOException::class)
    fun writeAction(writer: JsonWriter) {
        writer.beginArray()
        for (action in this) {
            writer.beginArray()
            action.writeIdAndData(writer)
            writer.endArray()
        }
        writer.endArray()
    }

    @Throws(IOException::class)
    fun loadAction(reader: JsonReader): Boolean {
        if (reader.peek() == JsonReader.Token.NULL) return false
        reader.beginArray()
        while (reader.hasNext()) {
            if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) return false
            reader.beginArray()
            if (reader.peek() == JsonReader.Token.NUMBER) {
                val id = reader.nextInt()

                val action = SingleAction.makeInstance(id, reader)
                if (reader.peek() != JsonReader.Token.END_ARRAY) return false
                reader.endArray()
                add(action)
            } else if (reader.peek() != JsonReader.Token.END_ARRAY) {
                return false
            }
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

    fun toString(nameArray: ActionNameArray): String? {
        return if (isEmpty()) null else get(0).toString(nameArray)
    }

    companion object {
        private const val serialVersionUID = 1712925333386047748L

        @JvmField
        val CREATOR: Parcelable.Creator<Action> = object : Parcelable.Creator<Action> {
            override fun createFromParcel(source: Parcel): Action {
                return Action(source)
            }

            override fun newArray(size: Int): Array<Action?> {
                return arrayOfNulls(size)
            }
        }

        fun makeInstance(id: Int): Action {
            return Action(SingleAction.makeInstance(id))
        }
    }
}
