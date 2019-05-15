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

package jp.hazuki.yuzubrowser.legacy.pattern.action

import android.content.Context
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.pattern.PatternAction
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import java.io.IOException

class BlockPatternAction : PatternAction {

    override val typeId: Int
        get() = BLOCK

    constructor()

    @Throws(IOException::class)
    constructor(reader: JsonReader) {
        if (reader.peek() != JsonReader.Token.NUMBER) return
        reader.nextInt()
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.pattern_block)
    }

    @Throws(IOException::class)
    override fun write(writer: JsonWriter): Boolean {
        writer.value(BLOCK)
        writer.value(0)
        return true
    }

    override fun run(context: Context, tab: MainTabData, url: String) = true
}
