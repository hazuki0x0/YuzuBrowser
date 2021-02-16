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

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import jp.hazuki.yuzubrowser.adblock.ui.original.AdBlockActivity
import jp.hazuki.yuzubrowser.core.utility.storage.DEFAULT_DOWNLOAD_PATH
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.extensions.registerForStartActivityForResult
import jp.hazuki.yuzubrowser.ui.preference.StrToIntListPreference
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs

class BrowserSettingsFragment : YuzuPreferenceFragment() {

    private var replaceFragment: ReplaceFragmentListener? = null

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_browser_settings)

        val pref = findPreference<StrToIntListPreference>("search_suggest")!!
        val suggest = findPreference<Preference>("search_suggest_engine")!!

        suggest.isEnabled = pref.value != 2

        pref.setOnPreferenceChangeListener { _, newValue ->
            suggest.isEnabled = newValue as Int != 2
            true
        }

        val savePinned = findPreference<Preference>("save_pinned_tabs")!!
        val saveLast = findPreference<SwitchPreference>("save_last_tabs")!!

        savePinned.isEnabled = !saveLast.isChecked
        saveLast.setOnPreferenceChangeListener { _, newValue ->
            savePinned.isEnabled = !(newValue as Boolean)
            true
        }

        findPreference<Preference>("ad_block_settings")!!.setOnPreferenceClickListener {
            startActivity(Intent(activity, AdBlockActivity::class.java))
            true
        }

        findPreference<Preference>("download_folder")!!.setOnPreferenceClickListener {
            DownloadFolderDialog(AppPrefs.download_folder.get()).show(childFragmentManager, "")
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

    private fun selectDownloadDirectory(isDefault: Boolean) {
        if (isDefault) {
            saveDownloadFolder(DEFAULT_DOWNLOAD_PATH)
        } else {
            try {
                downloadDirectoryLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val downloadDirectoryLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult

        val uri = it.data?.data ?: return@registerForStartActivityForResult

        val context = requireContext()
        if (uri.scheme == "content") {
            context.contentResolver.takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        saveDownloadFolder(uri.toString())
    }

    private fun saveDownloadFolder(directory: String) {
        AppPrefs.download_folder.set(directory)
        AppPrefs.commit(context, AppPrefs.download_folder)
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

            val suggestMode = findPreference<StrToIntListPreference>("search_suggest")!!
            suggestEngine = findPreference("search_suggest_engine")!!
            suggestHistory = findPreference("search_suggest_histories")!!
            suggestBookmark = findPreference("search_suggest_bookmarks")!!

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

    class DownloadFolderDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val uri = requireArguments().getString(ARG_URI)
            val selection = if (uri.isNullOrEmpty() || uri == DEFAULT_DOWNLOAD_PATH) 0 else 1
            val items = arrayOf(getString(R.string.default_text), getString(R.string.custom))

            return AlertDialog.Builder(requireActivity())
                .setTitle(R.string.pref_download_folder)
                .setSingleChoiceItems(items, selection) { dialog, which ->
                    (parentFragment as BrowserSettingsFragment).selectDownloadDirectory(which == 0)
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }

        companion object {
            private const val ARG_URI = "uri"

            operator fun invoke(uri: String) = DownloadFolderDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_URI, uri)
                }
            }
        }
    }
}
