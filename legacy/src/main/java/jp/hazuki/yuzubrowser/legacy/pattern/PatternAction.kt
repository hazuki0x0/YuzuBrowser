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

package jp.hazuki.yuzubrowser.legacy.pattern

import android.content.Context
import com.squareup.moshi.JsonReader
import jp.hazuki.yuzubrowser.legacy.pattern.action.BlockPatternAction
import jp.hazuki.yuzubrowser.legacy.pattern.action.OpenOthersPatternAction
import jp.hazuki.yuzubrowser.legacy.pattern.action.WebSettingPatternAction
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.utils.matcher.AbstractPatternAction
import java.io.IOException

abstract class PatternAction : AbstractPatternAction() {

    abstract val typeId: Int

    abstract fun run(context: Context, tab: MainTabData, url: String): Boolean


    companion object {
        const val OPEN_OTHERS = 1
        const val WEB_SETTING = 3
        const val BLOCK = 4

        @Throws(IOException::class)
        fun newInstance(reader: JsonReader): PatternAction? {
            if (reader.peek() != JsonReader.Token.NUMBER) return null
            return when (val id = reader.nextInt()) {
                OPEN_OTHERS -> OpenOthersPatternAction(reader)
                WEB_SETTING -> WebSettingPatternAction(reader)
                BLOCK -> BlockPatternAction(reader)
                else -> throw RuntimeException("unknown id : $id")
            }
        }
    }
}
