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

package jp.hazuki.yuzubrowser.legacy.settings.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils
import jp.hazuki.yuzubrowser.core.utility.utils.externalUserDirectory
import jp.hazuki.yuzubrowser.legacy.R
import java.io.File
import java.util.*

class FontListPreference(context: Context, attrs: AttributeSet) : ListPreference(context, attrs) {

    init {
        setNegativeButtonText(android.R.string.cancel)
        init()
    }

    private fun init() {
        val fontDir = File(externalUserDirectory, "fonts")

        if (!fontDir.isDirectory) {
            FileUtils.deleteFile(fontDir)
        }

        if (!fontDir.exists()) {
            fontDir.mkdirs()
        }

        var fileList: Array<File>? = null
        if (fontDir.exists() && fontDir.isDirectory) {
            fileList = fontDir.listFiles()
        }

        val names = ArrayList<String>()
        val values = ArrayList<String>()

        names.add(context.getString(R.string.default_text))
        values.add("")

        if (fileList != null) {
            fileList.asSequence()
                    .filter {
                        val name = it.absolutePath
                        name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".ttc")
                    }
                    .forEach {
                        names.add(it.name)
                        values.add(it.absolutePath)
                    }
        }

        entries = names.toTypedArray()
        entryValues = values.toTypedArray()
    }
}
