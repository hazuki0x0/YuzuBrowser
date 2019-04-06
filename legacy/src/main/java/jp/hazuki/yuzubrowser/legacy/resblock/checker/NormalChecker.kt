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

package jp.hazuki.yuzubrowser.legacy.resblock.checker

import android.content.Context
import android.net.Uri
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.resblock.ResourceChecker
import jp.hazuki.yuzubrowser.legacy.resblock.ResourceData
import java.io.IOException

class NormalChecker : ResourceChecker {

    var url: String? = null
        private set
    var isWhite: Boolean = false
        private set

    constructor(data: ResourceData, url: String, isWhite: Boolean) : super(data) {
        this.url = url
        this.isWhite = isWhite
    }

    @Throws(IOException::class)
    constructor(reader: JsonReader) : super(reader) {
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                FIELD_URL -> {
                    if (reader.peek() == JsonReader.Token.STRING) {
                        url = reader.nextString()
                    } else {
                        reader.skipValue()
                    }
                }
                FIELD_WHITE -> isWhite = reader.nextBoolean()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
    }

    override fun getTitle(context: Context): String? {
        return url
    }

    override fun isEnable(): Boolean {
        return true
    }

    override fun setEnable(enable: Boolean) {}

    override fun check(url: Uri): Int {
        if (!url.toString().contains(this.url!!))
            return ResourceChecker.SHOULD_CONTINUE
        return if (isWhite)
            ResourceChecker.SHOULD_BREAK
        else
            ResourceChecker.SHOULD_RUN
    }

    @Throws(IOException::class)
    override fun write(writer: JsonWriter): Boolean {
        writer.value(ResourceChecker.NORMAL_CHECKER.toLong())
        action.write(writer)
        writer.beginObject()
        writer.name(FIELD_URL)
        writer.value(url)
        writer.name(FIELD_WHITE)
        writer.value(isWhite)
        writer.endObject()
        return true
    }

    companion object {
        private const val FIELD_URL = "0"
        private const val FIELD_WHITE = "1"
    }
}
