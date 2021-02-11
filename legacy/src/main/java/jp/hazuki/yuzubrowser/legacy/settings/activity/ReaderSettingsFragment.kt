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

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import jp.hazuki.yuzubrowser.core.READER_FONT_NAME
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ReaderSettingsFragment : YuzuPreferenceFragment() {
    private lateinit var readerTextFont: Preference

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_reader)
        readerTextFont = findPreference("reader_text_font")!!
        readerTextFont.setOnPreferenceClickListener {
            if (AppPrefs.reader_text_font.get().isEmpty()) {
                selectReaderFont()
            } else {
                showReaderDialog()
            }
            true
        }

        readerTextFont.summary = AppPrefs.reader_text_font.get()
    }

    private fun resetReaderFont() {
        setReaderFont("")
    }

    private fun selectReaderFont() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "font/*"
        }
        startActivityForResult(intent, RESULT)
    }

    private fun setReaderFont(path: String) {
        readerTextFont.summary = path
        AppPrefs.reader_text_font.set(path)
        AppPrefs.commit(requireContext(), AppPrefs.reader_text_font)
    }

    private fun showReaderDialog() {
        FontSelectDialog().show(childFragmentManager, "")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RESULT && resultCode == Activity.RESULT_OK) {
            data?.data?.also {
                val context = requireContext()
                val file = File(context.filesDir, READER_FONT_NAME)
                if (file.exists()) file.delete()

                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.toString()
                } else {
                    var path = Uri.fromFile(file).toString()
                    try {
                        context.contentResolver.openInputStream(it)?.use { input ->
                            FileOutputStream(file).use { os ->
                                input.copyTo(os)
                            }
                        }
                    } catch (e: IOException) {
                        path = ""
                    }
                    path
                }

                setReaderFont(uri)
            }
        }
    }

    companion object {
        private const val RESULT = 1
    }

    class FontSelectDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val items = arrayOf(getText(R.string.default_text), getText(R.string.select_file))
            return AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_reader_text_font)
                .setItems(items) { _, which ->
                    val fragment = parentFragment as? ReaderSettingsFragment ?: return@setItems
                    when (which) {
                        0 -> fragment.resetReaderFont()
                        1 -> fragment.selectReaderFont()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }
    }
}
