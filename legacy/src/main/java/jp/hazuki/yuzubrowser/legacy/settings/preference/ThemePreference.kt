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
import jp.hazuki.yuzubrowser.core.utility.utils.externalUserDirectory
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.theme.ThemeData
import jp.hazuki.yuzubrowser.ui.theme.ThemeManifest
import java.io.File
import java.io.IOException
import java.util.*

class ThemePreference(context: Context, attrs: AttributeSet) : ListPreference(context, attrs) {
    init {
        init()
    }

    private fun init() {
        val dir = File(externalUserDirectory, "theme")

        if (!dir.isDirectory) {
            dir.delete()
        }

        if (!dir.exists()) {
            dir.mkdirs()
        }

        val noMedia = File(dir, ".nomedia")

        if (!noMedia.exists()) {
            try {
                noMedia.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        val themes = dir.listFiles()

        val themeList = ArrayList<String>()
        val valueList = ArrayList<String>()

        //Add default
        themeList.add(context.getString(R.string.pref_dark_theme))
        valueList.add("")
        themeList.add(context.getString(R.string.pref_light_theme))
        valueList.add(ThemeData.THEME_LIGHT)

        if (themes != null) {
            for (theme in themes) {
                if (theme.isDirectory) {
                    val manifest = ThemeManifest.getManifest(theme)
                    themeList.add(manifest?.name ?: theme.name)
                    valueList.add(theme.name)
                }
            }
        }


        entries = themeList.toTypedArray()
        entryValues = valueList.toTypedArray()
    }
}
