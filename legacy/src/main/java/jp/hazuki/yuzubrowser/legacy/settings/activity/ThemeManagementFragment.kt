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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.preference.Preference
import jp.hazuki.yuzubrowser.core.THEME_DIR
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.theme.Result
import jp.hazuki.yuzubrowser.legacy.theme.importTheme
import jp.hazuki.yuzubrowser.legacy.theme.importThemeDirectory
import jp.hazuki.yuzubrowser.ui.dialog.ConfirmDialog
import jp.hazuki.yuzubrowser.ui.extensions.registerForStartActivityForResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ThemeManagementFragment : YuzuPreferenceFragment(), ConfirmDialog.OnConfirmedListener {

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_theme_managment)

        findPreference<Preference>("import_theme")!!.setOnPreferenceClickListener {
            ImportThemeDialog().show(childFragmentManager, "")
            true
        }

        findPreference<Preference>("delete_theme")!!.setOnPreferenceClickListener {
            ManageThemeDialog().show(childFragmentManager, "")
            true
        }
    }

    private fun importThemeFromFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
        }
        try {
            importThemeFromFileLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun importThemeFromDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            importThemeFromDirectoryLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
        }
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
        val context = context ?: return
        if (result.isSuccess) {
            Toast.makeText(context, getString(R.string.theme_imported, result.message), Toast.LENGTH_SHORT).show()
            setFragmentResult(REQUEST_THEME_LIST_UPDATE, bundleOf(REQUEST_THEME_LIST_UPDATE to true))
        } else {
            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteThemes(themes: List<String>) {
        if (themes.isEmpty()) return

        val title = getString(R.string.delete_theme)
        val message = resources.getQuantityString(R.plurals.confirm_delete_multiple, themes.size)
        val data = Bundle().apply {
            putStringArray(CONFIRM_DATA, themes.toTypedArray())
        }
        ConfirmDialog(DELETE_THEME_CONFIRM, title, message, data)
            .show(childFragmentManager, "")
    }

    override fun onConfirmed(id: Int, data: Bundle?) {
        if (data == null) return
        when (id) {
            DELETE_THEME_CONFIRM -> {
                val root = requireContext().getExternalFilesDir(THEME_DIR)!!
                val themes = data.getStringArray(CONFIRM_DATA)!!
                    .map { File(root, it) }
                themes.forEach {
                    it.deleteRecursively()
                }
                Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show()
                setFragmentResult(REQUEST_THEME_LIST_UPDATE, bundleOf(REQUEST_THEME_LIST_UPDATE to true))
            }
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
                    val files = items.asSequence()
                        .filterIndexed { index, _ -> checked[index] }
                        .toList()
                    (parentFragment as ThemeManagementFragment).deleteThemes(files)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }
    }

    companion object {
        private const val DELETE_THEME_CONFIRM = 1
        private const val CONFIRM_DATA = "data"

        const val REQUEST_THEME_LIST_UPDATE = "request_theme_list_update"
    }
}
