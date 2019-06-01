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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import jp.hazuki.yuzubrowser.adblock.ui.original.AdBlockActivity
import jp.hazuki.yuzubrowser.core.utility.extensions.canResolvePath
import jp.hazuki.yuzubrowser.download.ui.FallbackFolderSelectActivity
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.preference.StrToIntListPreference
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import org.jetbrains.anko.longToast

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

        findPreference("ad_block_settings").setOnPreferenceClickListener {
            startActivity(Intent(activity, AdBlockActivity::class.java))
            true
        }

        findPreference("download_folder").setOnPreferenceClickListener {
            try {
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_FOLDER)
            } catch (e: Exception) {
                startActivityForResult(Intent(activity, FallbackFolderSelectActivity::class.java), REQUEST_FOLDER)
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_FOLDER -> {
                val activity = activity ?: return
                if (resultCode != Activity.RESULT_OK || data == null) return

                val uri = data.data ?: return

                if (uri.scheme == "content") {
                    activity.contentResolver.takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }

                AppPrefs.download_folder.set(uri.toString())
                AppPrefs.commit(activity, AppPrefs.download_folder)

                if (!uri.canResolvePath(activity)) {
                    activity.longToast(R.string.pref_storage_location_warn)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        replaceFragment = activity as ReplaceFragmentListener
    }

    override fun onDetach() {
        super.onDetach()

        replaceFragment = null
    }

    companion object {
        private const val REQUEST_FOLDER = 1
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
