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
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.*

fun Context.createLanguageConfig(lang: String?): Configuration {
    val config = resources.configuration
    config.getLocaleIfNeed(lang)?.let {
        config.setLocale(it)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val list = LocaleList(it)
            LocaleList.setDefault(list)
            config.setLocales(list)
        }
    }
    return config
}

private fun Configuration.getLocaleIfNeed(lang: String?): Locale? {
    if (lang.isNullOrEmpty()) return null

    val sysLocale = getSystemLocale()
    val split = lang.split('-')
    val language: String
    val country: String
    if (split.size == 2) {
        language = split[0]
        country = split[1]
    } else {
        language = split[0]
        country = ""
    }

    return if (sysLocale.language != language || sysLocale.country != country) {
        Locale(language, country)
    } else {
        null
    }
}

@Suppress("DEPRECATION")
fun Configuration.getSystemLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) locales[0] else locale
}
