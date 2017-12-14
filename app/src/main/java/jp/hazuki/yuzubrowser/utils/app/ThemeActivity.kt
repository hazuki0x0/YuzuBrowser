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

package jp.hazuki.yuzubrowser.utils.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.StyleRes
import android.support.v7.app.AppCompatActivity

import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.theme.ThemeData

@SuppressLint("Registered")
open class ThemeActivity : AppCompatActivity() {

    protected open val isLoadThemeData: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isLoadThemeData) {
            ThemeData.createInstance(applicationContext, AppData.theme_setting.get())
        }

        if (!useDarkTheme() && useLightTheme() || ThemeData.isEnabled() && ThemeData.getInstance().lightTheme) {
            setTheme(lightThemeResource())
        }

        super.onCreate(savedInstanceState)
    }

    @StyleRes
    protected open fun lightThemeResource(): Int = R.style.CustomThemeLight

    protected open fun useLightTheme(): Boolean = false

    protected open fun useDarkTheme(): Boolean = false
}
