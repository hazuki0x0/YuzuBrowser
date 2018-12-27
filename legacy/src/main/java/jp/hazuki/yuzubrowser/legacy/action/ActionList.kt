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

package jp.hazuki.yuzubrowser.legacy.action

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.legacy.utils.ErrorReport
import jp.hazuki.yuzubrowser.legacy.utils.JsonUtils
import jp.hazuki.yuzubrowser.legacy.utils.util.JsonConvertable
import java.io.IOException
import java.io.StringWriter
import java.util.*

class ActionList : ArrayList<Action>, Parcelable, JsonConvertable {

    constructor() : super() {}

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
    fun writeAction(field_name: String, generator: JsonGenerator) {
        generator.writeFieldName(field_name)
        writeAction(generator)
    }

    @Throws(IOException::class)
    fun writeAction(generator: JsonGenerator) {
        generator.writeStartArray()
        for (action in this) {
            action.writeAction(generator)
        }
        generator.writeEndArray()
    }

    @Throws(IOException::class)
    fun loadAction(parser: JsonParser): Boolean {
        if (parser.nextToken() != JsonToken.START_ARRAY) return false
        while (true) {
            val action = Action()
            if (!action.loadAction(parser)) {
                return if (parser.currentToken == JsonToken.END_ARRAY)
                    break
                else
                    false
            }
            add(action)
        }
        return true
    }

    override fun toJsonString(): String? {
        try {
            val writer = StringWriter()
            JsonUtils.getFactory().createGenerator(writer).use {
                writeAction(it)
            }
            return writer.toString()
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }
        return null
    }

    override fun fromJsonString(str: String): Boolean {
        clear()

        try {
            JsonUtils.getFactory().createParser(str).use {
                return loadAction(it)
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }
        return false
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
