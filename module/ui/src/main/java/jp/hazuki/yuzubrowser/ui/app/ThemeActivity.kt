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

package jp.hazuki.yuzubrowser.ui.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import jp.hazuki.yuzubrowser.core.utility.utils.createLanguageContext
import jp.hazuki.yuzubrowser.ui.theme.ThemeData

@SuppressLint("Registered")
open class ThemeActivity : AppCompatActivity() {

    protected open val isLoadThemeData: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isLoadThemeData) {
            ThemeData.createInstance(applicationContext, PrefPool.getSharedPref(this).getString(theme_setting, ThemeData.THEME_LIGHT))
        }

        val nightMode = AppCompatDelegate.getDefaultNightMode()
        if (isLightMode()) {
            if (nightMode != AppCompatDelegate.MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            if (nightMode != AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        super.onCreate(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.createLanguageContext(PrefPool.getSharedPref(newBase).getString(language, "")!!))
    }

    private fun isLightMode(): Boolean {
        return ThemeData.isEnabled() && ThemeData.getInstance().lightTheme
    }

    companion object {
        private const val theme_setting = "theme_setting"
        private const val language = "language"
    }
}
