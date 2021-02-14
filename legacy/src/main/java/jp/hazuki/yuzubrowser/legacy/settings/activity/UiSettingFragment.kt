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
package jp.hazuki.yuzubrowser.legacy.settings.activity

import android.os.Bundle
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import androidx.preference.Preference
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.settings.preference.ThemePreference
import jp.hazuki.yuzubrowser.ui.RestartActivity
import kotlinx.coroutines.delay

class UiSettingFragment : YuzuPreferenceFragment() {

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_ui_settings)
        findPreference<Preference>("theme_setting")!!.setOnPreferenceChangeListener { _, _ ->
            ui {
                delay(100L)
                startActivity(RestartActivity.createIntent(requireContext()))
            }
            true
        }

        findPreference<Preference>("theme_management")!!.setOnPreferenceClickListener {
            openFragment(ThemeManagementFragment())
            true
        }

        findPreference<Preference>("restart")!!.setOnPreferenceClickListener {
            startActivity(RestartActivity.createIntent(requireContext()))
            true
        }

        findPreference<Preference>("reader_settings")!!.setOnPreferenceClickListener {
            parentFragmentManager.commit {
                replace(R.id.container, ReaderSettingsFragment())
                addToBackStack("reader_settings")
            }
            true
        }

        setFragmentResultListener(ThemeManagementFragment.REQUEST_THEME_LIST_UPDATE) { _, bundle ->
            if (bundle.getBoolean(ThemeManagementFragment.REQUEST_THEME_LIST_UPDATE)) {
                findPreference<ThemePreference>("theme_setting")!!.load()
            }
        }
    }
}
