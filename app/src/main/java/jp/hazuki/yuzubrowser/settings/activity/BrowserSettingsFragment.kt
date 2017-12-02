/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.settings.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceScreen

import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.adblock.AdBlockActivity
import jp.hazuki.yuzubrowser.settings.preference.common.StrToIntListPreference

class BrowserSettingsFragment : YuzuPreferenceFragment() {

    private var replaceFragment: ReplaceFragmentListener? = null

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_browser_settings)

        val pref = findPreference("search_suggest") as StrToIntListPreference
        val suggest = findPreference("search_suggest_engine")

        suggest.isEnabled = pref.value != 2

        pref.setOnPreferenceChangeListener { _, newValue ->
            suggest.isEnabled = newValue as Int != 2
            true
        }

        val savePinned = findPreference("save_pinned_tabs")
        val saveLast = findPreference("save_last_tabs") as SwitchPreference

        savePinned.isEnabled = !saveLast.isChecked
        saveLast.setOnPreferenceChangeListener { _, newValue ->
            savePinned.isEnabled = !(newValue as Boolean)
            true
        }

        findPreference("ad_block_settings").setOnPreferenceClickListener { _ ->
            startActivity(Intent(activity, AdBlockActivity::class.java))
            true
        }
    }

    override fun onPreferenceStartScreen(pref: PreferenceScreen): Boolean {
        if (pref.key == "ps_search") {
            replaceFragment?.replaceFragment(SuggestScreen(), "ps_search")
            return true
        }
        return false
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        replaceFragment = activity as ReplaceFragmentListener
    }

    override fun onDetach() {
        super.onDetach()

        replaceFragment = null
    }

    class SuggestScreen : YuzuPreferenceFragment() {

        private lateinit var suggestEngine: Preference
        private lateinit var suggestHistory: Preference
        private lateinit var suggestBookmark: Preference

        override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_browser_settings, "ps_search")

            val suggestMode = findPreference("search_suggest") as StrToIntListPreference
            suggestEngine = findPreference("search_suggest_engine")
            suggestHistory = findPreference("search_suggest_histories")
            suggestBookmark = findPreference("search_suggest_bookmarks")

            setSuggestEnable(suggestMode.value)
            suggestMode.setOnPreferenceChangeListener { _, newValue ->
                setSuggestEnable(newValue as Int)
                true
            }
        }

        private fun setSuggestEnable(type: Int) {
            val enable = type != 3
            suggestEngine.isEnabled = enable
            suggestHistory.isEnabled = enable
            suggestBookmark.isEnabled = enable
        }
    }
}
