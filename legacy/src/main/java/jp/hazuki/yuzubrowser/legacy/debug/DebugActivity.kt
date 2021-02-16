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

package jp.hazuki.yuzubrowser.legacy.debug

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import jp.hazuki.yuzubrowser.core.utility.extensions.getResColor
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.view.ActionStringActivity
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import java.io.File

class DebugActivity : ThemeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)
        title = "Debug mode"

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, DebugFragment())
            .commit()

        supportActionBar?.setBackgroundDrawable(ColorDrawable(getResColor(R.color.primary)))
    }

    class DebugFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.debug)

            findPreference<Preference>("file_list")!!.setOnPreferenceClickListener {
                startActivity(Intent(activity, DebugFileListActivity::class.java))
                true
            }

            findPreference<Preference>("activity_list")!!.setOnPreferenceClickListener {
                if (requireActivity().resources.getBoolean(R.bool.package_debug))
                    startActivity(Intent(activity, ActivityListActivity::class.java))
                else
                    Toast.makeText(activity, "This feature is only valid for debug builds", Toast.LENGTH_SHORT).show()
                true
            }

            findPreference<Preference>("action_json_string")!!.setOnPreferenceClickListener {
                startActivity(Intent(activity, ActionStringActivity::class.java).apply {
                    putExtra(ActionStringActivity.EXTRA_ACTIVITY, ActionStringActivity.ACTION_ACTIVITY)
                })
                true
            }

            findPreference<Preference>("action_list_json_string")!!.setOnPreferenceClickListener {
                startActivity(Intent(activity, ActionStringActivity::class.java).apply {
                    putExtra(ActionStringActivity.EXTRA_ACTIVITY, ActionStringActivity.ACTION_LIST_ACTIVITY)
                })
                true
            }

            findPreference<Preference>("language")!!.setOnPreferenceClickListener {
                LanguageFragment().show(childFragmentManager, "language")
                true
            }

            findPreference<Preference>("log_list")!!.setOnPreferenceClickListener {
                val context = requireContext()
                val path = context.getExternalFilesDir("error_log")!!
                startActivity(DebugFileListActivity.createIntent(context, path))
                true
            }

            findPreference<Preference>("delete_log")!!.setOnPreferenceClickListener {
                DeleteLogDialog().show(childFragmentManager, "delete")
                true
            }
        }
    }

    class DeleteLogDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.pref_delete_all_logs)
                .setMessage(R.string.pref_delete_log_mes)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val activity = activity ?: return@setPositiveButton
                    val file = File(activity.getExternalFilesDir(null), "./error_log/")
                    if (!file.exists()) {
                        Toast.makeText(activity, R.string.succeed, Toast.LENGTH_SHORT).show()
                    } else if (FileUtils.deleteFile(file)) {
                        Toast.makeText(activity, R.string.succeed, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, R.string.failed, Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
            return builder.create()
        }
    }

    class LanguageFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity ?: throw IllegalStateException()
            val names = activity.resources.getStringArray(R.array.language_list)
            val values = activity.resources.getStringArray(R.array.language_value)
            val checked = values.indexOf(AppPrefs.language.get())

            return AlertDialog.Builder(activity)
                .setTitle("Language")
                .setSingleChoiceItems(names, checked) { dialog, which ->
                    AppPrefs.language.set(values[which])
                    AppPrefs.commit(activity, AppPrefs.language)
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }
    }
}
