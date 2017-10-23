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

package jp.hazuki.yuzubrowser.action.manager

import android.content.Context
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import jp.hazuki.yuzubrowser.action.ActionFile
import jp.hazuki.yuzubrowser.action.ActionList
import java.io.File
import java.io.IOException

class ActionArrayFile(private val FOLDER_NAME: String, private val id: Int) : ActionFile() {
    val list = ActionList()

    override fun getFile(context: Context): File {
        return File(context.getDir(FOLDER_NAME, Context.MODE_PRIVATE), id.toString() + ".dat")
    }

    override fun reset() {
        list.clear()
    }

    @Throws(IOException::class)
    override fun load(parser: JsonParser): Boolean {
        return list.loadAction(parser)
    }

    @Throws(IOException::class)
    override fun write(generator: JsonGenerator): Boolean {
        list.writeAction(generator)
        return true
    }

    companion object {
        private const val serialVersionUID = 6536274056164364431L
    }
}
