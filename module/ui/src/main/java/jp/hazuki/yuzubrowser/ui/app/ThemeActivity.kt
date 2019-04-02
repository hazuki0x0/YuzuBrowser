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
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import jp.hazuki.yuzubrowser.core.utility.utils.createLanguageContext
import jp.hazuki.yuzubrowser.ui.theme.ThemeData

@SuppressLint("Registered")
open class ThemeActivity : AppCompatActivity() {
    private var themeApplied = false

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        if (!themeApplied) applyThemeMode(isLightMode())

        super.onCreate(savedInstanceState, persistentState)
    }

    override fun attachBaseContext(newBase: Context) {
        val application = newBase.applicationContext
        if (!ThemeData.isLoaded()) {
            ThemeData.createInstance(application, PrefPool.getSharedPref(application).getString(theme_setting, ThemeData.THEME_LIGHT))
        }

        val isLightMode = isLightMode()
        if (updateTheme(isLightMode)) {
            applyThemeMode(isLightMode)
            onNightModeChanged(if (isLightMode) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES)
            themeApplied = true
        }

        val langContext = newBase.createLanguageContext(PrefPool.getSharedPref(application).getString(language, "")!!)

        super.attachBaseContext(langContext)
    }

    private fun applyThemeMode(isLightMode: Boolean) {
        val defaultMode = AppCompatDelegate.getDefaultNightMode()
        if (isLightMode) {
            if (defaultMode != AppCompatDelegate.MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            if (defaultMode != AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun isLightMode(): Boolean {
        return ThemeData.isEnabled() && ThemeData.getInstance().lightTheme
    }

    private fun updateTheme(isLightMode: Boolean): Boolean {
        val newNightMode = if (isLightMode) Configuration.UI_MODE_NIGHT_NO else Configuration.UI_MODE_NIGHT_YES

        // If we're here then we can try and apply an override configuration on the Context.
        val conf = Configuration()
        conf.uiMode = newNightMode or (conf.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv())

        try {
            applyOverrideConfiguration(conf)
        } catch (e: IllegalStateException) {
            // applyOverrideConfiguration throws an IllegalStateException if it's resources
            // have already been created. Since there's no way to check this beforehand we
            // just have to try it and catch the exception
            return false
        }
        return true
    }

    companion object {
        private const val theme_setting = "theme_setting"
        private const val language = "language"
    }
}
