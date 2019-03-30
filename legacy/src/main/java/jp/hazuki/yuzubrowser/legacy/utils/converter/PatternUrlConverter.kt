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

package jp.hazuki.yuzubrowser.legacy.utils.converter

import android.content.Context
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.pattern.PatternAction
import jp.hazuki.yuzubrowser.legacy.pattern.PatternChecker
import jp.hazuki.yuzubrowser.legacy.pattern.PatternManager
import java.io.IOException
import java.util.regex.PatternSyntaxException

class PatternUrlConverter {

    fun convert(context: Context) {
        val manager = ConvertManager(context, "url_1.dat")
        manager.load(context)
        manager.save(context)
    }

    private class ConvertManager internal constructor(context: Context, file: String) : PatternManager<ConvertChecker>(context, file) {

        @Throws(IOException::class)
        override fun newInstance(reader: JsonReader): ConvertChecker {
            return ConvertChecker(reader)
        }
    }

    private class ConvertChecker @Throws(PatternSyntaxException::class, IOException::class)
    internal constructor(reader: JsonReader) : PatternChecker(PatternAction.newInstance(reader)) {
        private var convertedString: String? = null

        init {
            init(reader)
        }

        private fun init(reader: JsonReader) {
            //TODO not set mPattern
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT)
                reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "0" -> {
                        if (reader.peek() == JsonReader.Token.STRING) {
                            setConvertedString(reader.nextString())
                        } else {
                            reader.skipValue()
                        }
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        }

        private fun setConvertedString(s: String) {
            convertedString = if (s.startsWith("?")) {
                "[${s.substring(1)}]"
            } else {
                s.replace("?", "\\?").replace("#", "\\#")
            }
        }

        override fun getTitle(context: Context): String? {
            return convertedString
        }

        override fun isEnable(): Boolean {
            return false
        }

        override fun setEnable(enable: Boolean) {

        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter): Boolean {
            action.write(writer)
            writer.beginObject()
            writer.name("0")
            writer.value(convertedString)
            writer.endObject()
            return true
        }
    }
}
