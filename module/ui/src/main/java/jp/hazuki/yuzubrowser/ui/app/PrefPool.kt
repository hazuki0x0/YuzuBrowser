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

import android.content.Context
import android.content.SharedPreferences

internal object PrefPool {
    private var pref: SharedPreferences? = null

    private const val PREFERENCE_NAME = "main_preference"

    fun getSharedPref(context: Context): SharedPreferences {
        if (pref == null) {
            pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }
        return pref!!
    }
}