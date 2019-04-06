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

package jp.hazuki.yuzubrowser.legacy.settings.activity

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import jp.hazuki.asyncpermissions.AsyncPermissions
import jp.hazuki.yuzubrowser.core.utility.extensions.startActivity
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.browser.checkLocationPermission
import jp.hazuki.yuzubrowser.legacy.browser.requestLocationPermission
import jp.hazuki.yuzubrowser.legacy.webrtc.ui.WebPermissionActivity

class PrivacyFragment : YuzuPreferenceFragment() {
    private val asyncPermissions by lazy { AsyncPermissions(activity as AppCompatActivity) }

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_privacy)

        findPreference("web_geolocation").setOnPreferenceClickListener {
            val activity = activity ?: return@setOnPreferenceClickListener false

            if (activity.checkLocationPermission()) {
                return@setOnPreferenceClickListener true
            } else {
                ui { (activity as AppCompatActivity).requestLocationPermission(asyncPermissions) }
                return@setOnPreferenceClickListener false
            }
        }

        val privateMode = findPreference("private_mode") as SwitchPreference

        val formData = findPreference("save_formdata")
        val webDB = findPreference("web_db")
        val webDom = findPreference("web_dom_db")
        val geo = findPreference("web_geolocation")
        val appCache = findPreference("web_app_cache")

        val enableSettings = !privateMode.isChecked

        formData.isEnabled = enableSettings
        webDB.isEnabled = enableSettings
        webDom.isEnabled = enableSettings
        geo.isEnabled = enableSettings
        appCache.isEnabled = enableSettings

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            preferenceScreen.removePreference(formData)
        } else {
            val safeBrowsing = findPreference("safe_browsing")
            safeBrowsing.isEnabled = false
            safeBrowsing.setSummary(R.string.pref_required_android_O)
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

        findPreference("contentSettings").setOnPreferenceClickListener {
            startActivity<WebPermissionActivity>()
            return@setOnPreferenceClickListener true
        }
    }
}
