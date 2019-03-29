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

package jp.hazuki.yuzubrowser.core.utility.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import java.util.*

fun Context.createLanguageContext(lang: String): Context {
    val config = applicationContext.resources.configuration
    val sysLocale = config.getSystemLocale()

    if (lang.isNotEmpty() && sysLocale.language != lang) {
        val locale = Locale(lang)
        config.setLocale(locale)
        return ContextWrapper(applicationContext.createConfigurationContext(config))
    }
    return this
}

@Suppress("DEPRECATION")
fun Configuration.getSystemLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) locales[0] else locale
}