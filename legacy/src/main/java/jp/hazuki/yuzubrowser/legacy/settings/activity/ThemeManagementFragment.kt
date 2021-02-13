/*
 * Copyright (C) 2017-2021 Hazuki
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
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import com.google.android.material.snackbar.Snackbar
import jp.hazuki.yuzubrowser.core.THEME_DIR
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.settings.preference.ThemePreference
import jp.hazuki.yuzubrowser.legacy.theme.Result
import jp.hazuki.yuzubrowser.legacy.theme.importTheme
import jp.hazuki.yuzubrowser.legacy.theme.importThemeDirectory
import jp.hazuki.yuzubrowser.ui.extensions.registerForStartActivityForResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ThemeManagementFragment : YuzuPreferenceFragment() {
    private var snackbar: Snackbar? = null

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_theme_managment)

        findPreference<Preference>("import_theme")!!.setOnPreferenceClickListener {
            ImportThemeDialog().show(childFragmentManager, "")
            true
        }

        findPreference<Preference>("delete_theme")!!.setOnPreferenceClickListener {
            snackbar?.dismiss()
            ManageThemeDialog().show(childFragmentManager, "")
            true
        }
    }

    private fun importThemeFromFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
        }
        importThemeFromFileLauncher.launch(intent)
    }

    private fun importThemeFromDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        importThemeFromDirectoryLauncher.launch(intent)
    }

    private val importThemeFromFileLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult
        val uri = it.data!!.data ?: return@registerForStartActivityForResult

        ui {
            val result = withContext(Dispatchers.IO) { importTheme(requireContext(), uri) }
            showImportResult(result)
        }
    }

    private val importThemeFromDirectoryLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult
        val uri = it.data!!.data ?: return@registerForStartActivityForResult

        ui {
            val result = withContext(Dispatchers.IO) {
                importThemeDirectory(requireContext(), uri)
            }
            showImportResult(result)
        }
    }

    private fun showImportResult(result: Result) {
        if (result.isSuccess) {
            Toast.makeText(requireContext(), getString(R.string.theme_imported, result.message), Toast.LENGTH_SHORT).show()
            findPreference<ThemePreference>("theme_setting")!!.load()
        } else {
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteThemes(themes: List<File>) {
        if (themes.isEmpty()) return

        snackbar = Snackbar.make(requireView(), R.string.deleted, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.undo) {}
            addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == DISMISS_EVENT_ACTION) return

                    themes.forEach {
                        it.deleteRecursively()
                    }
                }
            })
            show()
        }
    }

    class ImportThemeDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val items = arrayOf(getText(R.string.from_theme_file), getText(R.string.from_theme_folder))
            return AlertDialog.Builder(requireContext())
                .setTitle(R.string.theme_import)
                .setItems(items) { _, which ->
                    when (which) {
                        0 -> (parentFragment as ThemeManagementFragment).importThemeFromFile()
                        1 -> (parentFragment as ThemeManagementFragment).importThemeFromDirectory()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }
    }

    class ManageThemeDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val items = requireContext()
                .getExternalFilesDir(THEME_DIR)!!
                .listFiles()!!
                .asSequence()
                .filter { it.name != ".nomedia" }
                .map { it.name }
                .toList()
                .toTypedArray()

            val checked = BooleanArray(items.size)

            return AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_theme)
                .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                    checked[which] = isChecked
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val root = requireContext().getExternalFilesDir(THEME_DIR)!!
                    val files = items.asSequence()
                        .filterIndexed { index, _ -> checked[index] }
                        .map { File(root, it) }
                        .toList()
                    (parentFragment as ThemeManagementFragment).deleteThemes(files)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }
    }
}
