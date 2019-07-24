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
import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import jp.hazuki.yuzubrowser.core.utility.utils.createLanguageConfig
import jp.hazuki.yuzubrowser.ui.theme.ThemeData

@SuppressLint("Registered")
open class ThemeActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val application = newBase.applicationContext
        if (!ThemeData.isLoaded()) {
            ThemeData.createInstance(application, PrefPool.getSharedPref(application).getString(theme_setting, ThemeData.THEME_LIGHT))
        }

        val config = newBase.createLanguageConfig(PrefPool.getSharedPref(application).getString(language, ""))

        val isLightMode = isLightMode()
        applyThemeMode(isLightMode)
        config.updateTheme(isLightMode)

        super.attachBaseContext(ContextCompat(newBase.createConfigurationContext(config), newBase))
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
        return ThemeData.getInstance()?.lightTheme ?: false
    }

    private fun Configuration.updateTheme(isLightMode: Boolean) {
        val newNightMode = if (isLightMode) Configuration.UI_MODE_NIGHT_NO else Configuration.UI_MODE_NIGHT_YES

        uiMode = newNightMode or (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv())
    }

    private class ContextCompat(
        configContext: Context,
        private val baseActivityContext: Context
    ) : ContextWrapper(configContext) {

        override fun getSystemService(name: String): Any? {
            return baseActivityContext.getSystemService(name)
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun getSystemServiceName(serviceClass: Class<*>): String? {
            return baseActivityContext.getSystemServiceName(serviceClass)
        }
    }

    companion object {
        private const val theme_setting = "theme_setting"
        private const val language = "language"
    }
}
