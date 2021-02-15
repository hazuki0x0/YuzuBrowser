/*
 * Copyright (C) 2017-2020 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.settings.activity

import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.webrtc.ui.WebPermissionActivity
import jp.hazuki.yuzubrowser.ui.extensions.startActivity

class PrivacyFragment : YuzuPreferenceFragment() {

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_privacy)

        val privateMode: SwitchPreference = findPreference("private_mode")!!
        val formData = findPreference<Preference>("save_formdata")!!
        val webDB = findPreference<Preference>("web_db")!!
        val webDom = findPreference<Preference>("web_dom_db")!!
        val geo = findPreference<Preference>("web_geolocation")!!
        val appCache = findPreference<Preference>("web_app_cache")!!

        val enableSettings = !privateMode.isChecked

        formData.isEnabled = enableSettings
        webDB.isEnabled = enableSettings
        webDom.isEnabled = enableSettings
        geo.isEnabled = enableSettings
        appCache.isEnabled = enableSettings

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            preferenceScreen.removePreference(formData)
        } else {
            findPreference<Preference>("safe_browsing")!!.run {
                isEnabled = false
                setSummary(R.string.pref_required_android_O)
            }
        }

        privateMode.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val newSettings = !(newValue as Boolean)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                formData.isEnabled = newSettings

            webDB.isEnabled = newSettings
            webDom.isEnabled = newSettings
            geo.isEnabled = newSettings
            appCache.isEnabled = newSettings
            true
        }

        findPreference<Preference>("contentSettings")!!.setOnPreferenceClickListener {
            startActivity<WebPermissionActivity>()
            return@setOnPreferenceClickListener true
        }
    }
}
