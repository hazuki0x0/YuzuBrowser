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

package jp.hazuki.yuzubrowser.legacy.pattern.url

import android.content.Context
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.adblock.filter.fastmatch.FastMatcherFactory
import jp.hazuki.yuzubrowser.legacy.pattern.PatternAction
import jp.hazuki.yuzubrowser.legacy.pattern.PatternChecker
import jp.hazuki.yuzubrowser.legacy.utils.WebUtils
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class PatternUrlChecker : PatternChecker {
    var patternUrl: String? = null
        private set
    private var mPattern: Pattern? = null
    private var enable = true

    @Throws(PatternSyntaxException::class)
    constructor(pattern_action: PatternAction, factory: FastMatcherFactory, pattern_url: String) : super(pattern_action) {
        setPatternUrlWithThrow(factory, pattern_url)
    }

    @Throws(PatternSyntaxException::class, IOException::class)
    constructor(reader: JsonReader, factory: FastMatcherFactory) : super(PatternAction.newInstance(reader)) {
        //TODO not set mPattern
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                FIELD_PATTERN_URL -> {
                    if (reader.peek() == JsonReader.Token.STRING) {
                        setPatternUrlWithThrow(factory, reader.nextString())
                    } else {
                        reader.skipValue()
                    }
                }
                FIELD_PATTERN_ENABLE -> {
                    enable = reader.nextBoolean()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
    }

    @Throws(PatternSyntaxException::class)
    private fun setPatternUrlWithThrow(factory: FastMatcherFactory, pattern_url: String) {
        mPattern = WebUtils.makeUrlPatternWithThrow(factory, pattern_url)
        this.patternUrl = pattern_url
    }

    fun isMatchUrl(url: String): Boolean {
        return enable && mPattern != null && mPattern!!.matcher(url).find()
    }

    protected fun matcher(url: String): Matcher {
        return mPattern!!.matcher(url)
    }

    override fun getTitle(context: Context): String? {
        return patternUrl
    }

    override fun isEnable(): Boolean {
        return enable
    }

    override fun setEnable(enable: Boolean) {
        this.enable = enable
    }

    @Throws(IOException::class)
    override fun write(writer: JsonWriter): Boolean {
        action.write(writer)
        writer.beginObject()
        writer.name(FIELD_PATTERN_URL)
        writer.value(patternUrl)
        writer.name(FIELD_PATTERN_ENABLE)
        writer.value(enable)
        writer.endObject()
        return true
    }

    companion object {
        private const val FIELD_PATTERN_URL = "0"
        private const val FIELD_PATTERN_ENABLE = "1"
    }
}
