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

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import jp.hazuki.yuzubrowser.core.utility.extensions.getVersionName
import jp.hazuki.yuzubrowser.core.utility.extensions.intentFor
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils
import jp.hazuki.yuzubrowser.legacy.Constants
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.licenses.LicensesActivity
import jp.hazuki.yuzubrowser.legacy.utils.AppUtils
import jp.hazuki.yuzubrowser.legacy.utils.extensions.setClipboardWithToast
import java.io.File

class AboutFragment : YuzuPreferenceFragment() {

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val activity = activity ?: throw IllegalStateException()
        addPreferencesFromResource(R.xml.pref_about)
        val version = findPreference("version")
        version.setOnPreferenceClickListener {
            activity.setClipboardWithToast(AppUtils.getVersionDeviceInfo(activity))
            true
        }

        version.summary = activity.getVersionName()
        findPreference("build").summary = activity.getString(R.string.package_build)
        findPreference("build_time").summary = activity.getString(R.string.package_build_time)

        findPreference("osl").setOnPreferenceClickListener {
            startActivity(intentFor<LicensesActivity>())
            true
        }
        findPreference("translation").setOnPreferenceClickListener {
            TranslationDialog().show(childFragmentManager, "translation")
            true
        }

        findPreference("privacy_policy").setOnPreferenceClickListener {
            startActivity(intentFor(Constants.activity.MAIN_BROWSER).apply {
                action = Constants.intent.ACTION_OPEN_DEFAULT
                data = Uri.parse("https://github.com/hazuki0x0/YuzuBrowser/wiki/Privacy-policy")
            })
            true
        }

        findPreference("delete_log").setOnPreferenceClickListener {
            DeleteLogDialog().show(childFragmentManager, "delete")
            true
        }
    }

    class TranslationDialog : androidx.fragment.app.DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(activity)
                    .setTitle(R.string.pref_translation)
                    .setView(WebView(activity).apply { loadUrl("file:///android_asset/translators.html") })
                    .create()
        }
    }

    class DeleteLogDialog : androidx.fragment.app.DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.pref_delete_all_logs)
                    .setMessage(R.string.pref_delete_log_mes)
                    .setPositiveButton(android.R.string.yes) { _, _ ->
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
                    .setNegativeButton(android.R.string.no, null)
            return builder.create()
        }
    }
}
