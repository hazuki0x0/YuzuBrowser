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

import android.content.Context
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.legacy.utils.JsonUtils
import java.io.*

abstract class ActionFile : Serializable {

    abstract fun getFile(context: Context): File

    abstract fun reset()

    fun load(context: Context): Boolean {
        reset()

        val file = getFile(context)
        if (!file.exists() || file.isDirectory) return true

        try {
            JsonUtils.getFactory().createParser(BufferedInputStream(FileInputStream(file))).use {
                if (!load(it)) {
                    Logger.e(TAG, "loadMain error (return false)")
                    return false
                }
                return true
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }

        return false
    }

    @Throws(IOException::class)
    abstract fun load(parser: JsonParser): Boolean

    fun write(context: Context): Boolean {
        val file = getFile(context)

        try {
            JsonUtils.getFactory().createGenerator(BufferedOutputStream(FileOutputStream(file))).use {
                if (!write(it)) {
                    Logger.e(TAG, "writeMain error (return false)")
                    return false
                }
                return true
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }
        return false
    }

    @Throws(IOException::class)
    abstract fun write(generator: JsonGenerator): Boolean

    companion object {
        private const val serialVersionUID = 9159377694255234638L

        private const val TAG = "ActionFile"
    }
}
