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
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.preference.Preference
import dagger.android.support.AndroidSupportInjection
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkFolder
import jp.hazuki.yuzubrowser.bookmark.netscape.exportHtmlBookmark
import jp.hazuki.yuzubrowser.bookmark.netscape.importHtmlBookmark
import jp.hazuki.yuzubrowser.bookmark.repository.BookmarkManager
import jp.hazuki.yuzubrowser.bookmark.util.BookmarkIdGenerator
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.backup.*
import jp.hazuki.yuzubrowser.legacy.speeddial.io.backupSpeedDial
import jp.hazuki.yuzubrowser.legacy.speeddial.io.restoreSpeedDial
import jp.hazuki.yuzubrowser.ui.RestartActivity
import jp.hazuki.yuzubrowser.ui.extensions.registerForStartActivityForResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ImportExportFragment : YuzuPreferenceFragment() {
    @Inject
    internal lateinit var faviconManager: FaviconManager

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        AndroidSupportInjection.inject(this)
        addPreferencesFromResource(R.xml.pref_import_export)

        findPreference<Preference>("import_sd_bookmark")!!.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
            }
            importBookmarkLauncher.launch(intent)
            false
        }

        findPreference<Preference>("export_sd_bookmark")!!.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_TITLE, "bookmark_${FileUtils.getTimeFileName()}.dat")
            }
            exportBookmarkLauncher.launch(intent)
            false
        }

        findPreference<Preference>("import_html_bookmark")!!.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "text/html"
            }
            importHtmlBookmarkLauncher.launch(intent)
            false
        }

        findPreference<Preference>("export_html_bookmark")!!.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_TITLE, "bookmark_${FileUtils.getTimeFileName()}.html")
            }
            exportHtmlBookmarkLauncher.launch(intent)
            false
        }

        findPreference<Preference>("restore_speed_dial")!!.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
            }
            restoreSpeeddialLauncher.launch(intent)
            true
        }

        findPreference<Preference>("backup_speed_dial")!!.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_TITLE, "speedDial_${FileUtils.getTimeFileName()}$EXT_SPEED_DIAL")
            }
            backupSpeeddialLauncher.launch(intent)
            false
        }

        findPreference<Preference>("restore_settings")!!.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
            }
            restoreSettingsLauncher.launch(intent)
            true
        }

        findPreference<Preference>("backup_settings")!!.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_TITLE, "yuzu_backup_${FileUtils.getTimeFileName()}$EXT")
            }
            backupSettingsLauncher.launch(intent)
            false
        }
    }

    private val importBookmarkLauncher = registerForStartActivityForResult {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data!!.data ?: return@registerForStartActivityForResult
            val file = DocumentFile.fromSingleUri(requireContext(), uri)!!
            if (file.exists() && file.name?.endsWith(".dat") == true) {
                val manager = BookmarkManager.getInstance(requireContext())
                val internalFile = manager.file

                GlobalScope.launch(Dispatchers.Main) {
                    val result = withContext(Dispatchers.IO) { uri.copyTo(internalFile) }
                    if (result) {
                        manager.load()
                        manager.save()
                        Toast.makeText(activity, R.string.succeed, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, R.string.failed, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(activity, R.string.failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    private val exportBookmarkLauncher = registerForStartActivityForResult {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data!!.data ?: return@registerForStartActivityForResult
            val manager = BookmarkManager.getInstance(requireContext())
            val internalFile = manager.file

            GlobalScope.launch(Dispatchers.Main) {
                val result = withContext(Dispatchers.IO) { internalFile.copyTo(uri) }
                requireContext().actionMessage(result)
            }
        } else {
            Toast.makeText(activity, R.string.failed, Toast.LENGTH_LONG).show()
        }
    }

    private val importHtmlBookmarkLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult
        val uri = it.data!!.data ?: return@registerForStartActivityForResult
        val file = DocumentFile.fromSingleUri(requireContext(), uri)!!
        val manager = BookmarkManager.getInstance(requireContext())

        val root = BookmarkFolder(file.name, manager.root, BookmarkIdGenerator.getNewId())
        manager.add(manager.root, root)

        GlobalScope.launch(Dispatchers.Main) {
            val context = requireContext()
            val message = when (manager.importHtmlBookmark(context, uri, faviconManager, root)) {
                0 -> R.string.failed
                1 -> R.string.succeed
                -1 -> R.string.not_bookmark_file
                else -> throw IllegalStateException("Html bookmark result state error")
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    private val exportHtmlBookmarkLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult
        val uri = it.data!!.data ?: return@registerForStartActivityForResult
        val manager = BookmarkManager.getInstance(requireContext())

        GlobalScope.launch(Dispatchers.Main) {
            val context = requireContext()
            val result = manager.exportHtmlBookmark(context, uri)
            context.actionMessage(result)
        }
    }

    private val backupSpeeddialLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult
        val uri = it.data!!.data ?: return@registerForStartActivityForResult

        GlobalScope.launch(Dispatchers.Main) {
            val context = requireContext()
            val result = withContext(Dispatchers.IO) { context.backupSpeedDial(uri) }
            context.actionMessage(result)
        }
    }

    private val restoreSpeeddialLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult
        val uri = it.data!!.data ?: return@registerForStartActivityForResult
        val file = DocumentFile.fromSingleUri(requireContext(), uri)!!
        val name = file.name
        if (name == null || !name.endsWith(EXT_SPEED_DIAL)) {
            requireContext().actionMessage(false)
            return@registerForStartActivityForResult
        }

        GlobalScope.launch(Dispatchers.Main) {
            val context = requireContext()
            val result = withContext(Dispatchers.IO) { context.restoreSpeedDial(uri) }
            context.actionMessage(result)
        }
    }

    private val backupSettingsLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult
        val uri = it.data!!.data ?: return@registerForStartActivityForResult

        GlobalScope.launch(Dispatchers.Main) {
            val context = requireContext()
            val manager = BackupManager(context)
            val result = withContext(Dispatchers.IO) { manager.backup(context, uri) }
            context.actionMessage(result)
        }
    }

    private val restoreSettingsLauncher = registerForStartActivityForResult {
        if (it.resultCode != Activity.RESULT_OK) return@registerForStartActivityForResult
        val uri = it.data!!.data ?: return@registerForStartActivityForResult
        val file = DocumentFile.fromSingleUri(requireContext(), uri)!!
        val name = file.name
        if (name == null || !name.endsWith(EXT)) {
            requireContext().actionMessage(false)
            return@registerForStartActivityForResult
        }

        GlobalScope.launch(Dispatchers.Main) {
            val context = requireContext()
            val manager = BackupManager(context)
            val result = withContext(Dispatchers.IO) { manager.restore(context, uri) }
            context.actionMessage(result)
            if (result) {
                startActivity(RestartActivity.createIntent(context))
            }
        }
    }

    private fun Uri.copyTo(file: File): Boolean {
        try {
            requireContext().contentResolver.openInputStream(this)?.use { input ->
                file.outputStream().use { os ->
                    input.copyTo(os)
                    return true
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    private fun File.copyTo(uri: Uri): Boolean {
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { os ->
                inputStream().use { input ->
                    input.copyTo(os)
                    return true
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    private fun Context.actionMessage(state: Boolean) {
        val message = if (state) R.string.succeed else R.string.failed
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val EXT = ".yuzubackup"
        private const val EXT_SPEED_DIAL = ".yuzudial"
    }

}
