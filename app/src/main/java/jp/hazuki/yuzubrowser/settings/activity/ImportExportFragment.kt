/*
 * Copyright (C) 2017-2018 Hazuki
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

package jp.hazuki.yuzubrowser.settings.activity

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import jp.hazuki.asyncpermissions.AsyncPermissions
import jp.hazuki.yuzubrowser.BrowserApplication
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.backup.BackupTask
import jp.hazuki.yuzubrowser.backup.RestoreTask
import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder
import jp.hazuki.yuzubrowser.bookmark.BookmarkManager
import jp.hazuki.yuzubrowser.bookmark.netscape.BookmarkHtmlExportTask
import jp.hazuki.yuzubrowser.bookmark.netscape.BookmarkHtmlImportTask
import jp.hazuki.yuzubrowser.bookmark.util.BookmarkIdGenerator
import jp.hazuki.yuzubrowser.browser.checkStoragePermission
import jp.hazuki.yuzubrowser.browser.openRequestPermissionSettings
import jp.hazuki.yuzubrowser.browser.requestStoragePermission
import jp.hazuki.yuzubrowser.settings.preference.common.AlertDialogPreference
import jp.hazuki.yuzubrowser.speeddial.io.SpeedDialBackupTask
import jp.hazuki.yuzubrowser.speeddial.io.SpeedDialRestoreTask
import jp.hazuki.yuzubrowser.utils.AppUtils
import jp.hazuki.yuzubrowser.utils.FileUtils
import jp.hazuki.yuzubrowser.utils.ui
import jp.hazuki.yuzubrowser.utils.view.ProgressDialog
import jp.hazuki.yuzubrowser.utils.view.filelist.FileListDialog
import jp.hazuki.yuzubrowser.utils.view.filelist.FileListViewController
import java.io.File
import java.lang.ref.WeakReference

class ImportExportFragment : YuzuPreferenceFragment(), LoaderManager.LoaderCallbacks<Boolean> {
    private var progress: DialogFragment? = null
    private val asyncPermissions by lazy { AsyncPermissions(appCompatActivity) }

    override fun onCreateYuzuPreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_import_export)
        val activity = activity ?: return

        findPreference("import_sd_bookmark").setOnPreferenceClickListener {
            val manager = BookmarkManager.getInstance(activity)
            val internalFile = manager.file

            var defFolder = File(BrowserApplication.externalUserDirectory, internalFile.parentFile.name + File.separator)
            if (!defFolder.exists())
                defFolder = Environment.getExternalStorageDirectory()

            FileListDialog(activity)
                    .setFilePath(defFolder)
                    .setOnFileSelectedListener(object : FileListViewController.OnFileSelectedListener {
                        override fun onFileSelected(file: File) {
                            AlertDialog.Builder(activity)
                                    .setTitle(R.string.pref_import_bookmark)
                                    .setMessage(R.string.pref_import_bookmark_confirm)
                                    .setPositiveButton(android.R.string.ok) { _, _ ->
                                        if (file.exists())
                                            if (FileUtils.copySingleFile(file, internalFile)) {
                                                manager.load()
                                                manager.save()
                                                Toast.makeText(activity, R.string.succeed, Toast.LENGTH_LONG).show()
                                                return@setPositiveButton
                                            }
                                        Toast.makeText(activity, R.string.failed, Toast.LENGTH_LONG).show()
                                    }
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .show()
                        }

                        override fun onDirectorySelected(file: File): Boolean {
                            return false
                        }
                    })
                    .show()

            false
        }

        (findPreference("export_sd_bookmark") as AlertDialogPreference).setOnPositiveButtonListener {
            if (activity.checkStoragePermission()) {
                val manager = BookmarkManager.getInstance(activity)
                val internalFile = manager.file
                val externalFile = File(BrowserApplication.externalUserDirectory, internalFile.parentFile.name + File.separator + FileUtils.getTimeFileName() + ".dat")
                if (!externalFile.parentFile.exists()) {
                    if (!externalFile.parentFile.mkdirs()) {
                        Toast.makeText(activity, R.string.failed, Toast.LENGTH_LONG).show()
                        return@setOnPositiveButtonListener
                    }
                }
                if (internalFile.exists())
                    if (FileUtils.copySingleFile(internalFile, externalFile)) {
                        Toast.makeText(activity, R.string.succeed, Toast.LENGTH_LONG).show()
                        return@setOnPositiveButtonListener
                    }
                Toast.makeText(activity, R.string.failed, Toast.LENGTH_LONG).show()
            } else {
                ui { appCompatActivity.requestStoragePermission(asyncPermissions) }

            }
        }

        findPreference("import_html_bookmark").setOnPreferenceClickListener {
            val manager = BookmarkManager.getInstance(activity)
            val defFolder = Environment.getExternalStorageDirectory()

            FileListDialog(activity)
                    .setFilePath(defFolder)
                    .setOnFileSelectedListener(object : FileListViewController.OnFileSelectedListener {
                        override fun onFileSelected(file: File) {
                            AlertDialog.Builder(activity)
                                    .setTitle(R.string.pref_import_html_bookmark)
                                    .setMessage(R.string.pref_import_html_bookmark_confirm)
                                    .setPositiveButton(android.R.string.ok) { _, _ ->
                                        if (file.exists()) {
                                            val root = BookmarkFolder(file.name, manager.root, BookmarkIdGenerator.getNewId())
                                            manager.add(manager.root, root)
                                            val bundle = Bundle()
                                            bundle.putSerializable("file", file)
                                            bundle.putSerializable("manager", manager)
                                            bundle.putSerializable("folder", root)
                                            loaderManager.restartLoader(2, bundle, this@ImportExportFragment)
                                            progress = ProgressDialog(getString(R.string.restoring)).also { dialog ->
                                                dialog.show(childFragmentManager, "progress")
                                            }
                                            handler.setDialog(progress)
                                        }
                                    }
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .show()
                        }

                        override fun onDirectorySelected(file: File): Boolean {
                            return false
                        }
                    })
                    .show()

            false
        }

        (findPreference("export_html_bookmark") as AlertDialogPreference).setOnPositiveButtonListener {
            if (activity.checkStoragePermission()) {
                val manager = BookmarkManager.getInstance(activity)
                val externalFile = File(BrowserApplication.externalUserDirectory, manager.file.parentFile.name + File.separator + FileUtils.getTimeFileName() + ".html")
                if (!externalFile.parentFile.exists()) {
                    if (!externalFile.parentFile.mkdirs()) {
                        Toast.makeText(activity, R.string.failed, Toast.LENGTH_LONG).show()
                        return@setOnPositiveButtonListener
                    }
                }

                val bundle = Bundle()
                bundle.putSerializable("file", externalFile)
                bundle.putSerializable("folder", manager.root)
                loaderManager.restartLoader(3, bundle, this@ImportExportFragment)
                progress = ProgressDialog(getString(R.string.restoring)).also {
                    it.show(childFragmentManager, "progress")
                }
                handler.setDialog(progress)
            } else {
                ui { appCompatActivity.requestStoragePermission(asyncPermissions) }
            }
        }

        findPreference("restore_speed_dial").setOnPreferenceClickListener {
            val dir = File(BrowserApplication.externalUserDirectory, "speedDial")
            if (!dir.exists())
                dir.mkdirs()
            FileListDialog(activity)
                    .setFilePath(dir)
                    .setShowExtensionOnly(EXT_SPEED_DIAL)
                    .setOnFileSelectedListener(object : FileListViewController.OnFileSelectedListener {
                        override fun onFileSelected(file: File) {
                            if (file.exists()) {
                                val bundle = Bundle()
                                bundle.putSerializable("file", file)
                                loaderManager.restartLoader(4, bundle, this@ImportExportFragment)
                                progress = ProgressDialog(getString(R.string.restoring)).also { dialog ->
                                    dialog.show(childFragmentManager, "progress")
                                }
                                handler.setDialog(progress)
                            }
                        }

                        override fun onDirectorySelected(file: File): Boolean {
                            return false
                        }
                    })
                    .show()
            true
        }

        (findPreference("backup_speed_dial") as AlertDialogPreference).setOnPositiveButtonListener {
            if (activity.checkStoragePermission()) {
                val file = File(BrowserApplication.externalUserDirectory, "speedDial" + File.separator + FileUtils.getTimeFileName() + EXT_SPEED_DIAL)
                val bundle = Bundle()
                bundle.putSerializable("file", file)
                loaderManager.restartLoader(5, bundle, this@ImportExportFragment)
                progress = ProgressDialog(getString(R.string.restoring)).also {
                    it.show(childFragmentManager, "progress")
                }
                handler.setDialog(progress)
            } else {
                ui { appCompatActivity.requestStoragePermission(asyncPermissions) }
            }
        }

        findPreference("restore_settings").setOnPreferenceClickListener {
            val dir = File(BrowserApplication.externalUserDirectory, "backup")
            if (!dir.exists())
                dir.mkdirs()
            FileListDialog(activity)
                    .setFilePath(dir)
                    .setShowExtensionOnly(EXT)
                    .setOnFileSelectedListener(object : FileListViewController.OnFileSelectedListener {
                        override fun onFileSelected(file: File) {
                            AlertDialog.Builder(activity)
                                    .setTitle(R.string.restore_settings)
                                    .setMessage(R.string.pref_restore_settings_confirm)
                                    .setPositiveButton(android.R.string.ok) { _, _ ->
                                        if (file.exists()) {
                                            val bundle = Bundle()
                                            bundle.putSerializable("file", file)
                                            loaderManager.restartLoader(0, bundle, this@ImportExportFragment)
                                            progress = ProgressDialog(getString(R.string.restoring)).also { dialog ->
                                                dialog.show(childFragmentManager, "progress")
                                            }
                                            handler.setDialog(progress)
                                        }

                                    }
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .show()
                        }

                        override fun onDirectorySelected(file: File): Boolean {
                            return false
                        }
                    })
                    .show()
            true
        }

        (findPreference("backup_settings") as AlertDialogPreference).setOnPositiveButtonListener {
            if (activity.checkStoragePermission()) {
                val file = File(BrowserApplication.externalUserDirectory, "backup" + File.separator + FileUtils.getTimeFileName() + EXT)
                val bundle = Bundle()
                bundle.putSerializable("file", file)
                loaderManager.restartLoader(1, bundle, this@ImportExportFragment)
                progress = ProgressDialog(getString(R.string.backing_up)).also {
                    it.show(childFragmentManager, "progress")
                }
                handler.setDialog(progress)
            } else {
                ui { appCompatActivity.requestStoragePermission(asyncPermissions) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = activity ?: return

        if (!activity.checkStoragePermission()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                PermissionDialog().show(childFragmentManager, "permission")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                }
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Boolean> {
        if (args == null) throw IllegalArgumentException("args must not be null")

        return when (id) {
            0 -> RestoreTask(activity, args.getSerializable("file") as File)
            1 -> BackupTask(activity, args.getSerializable("file") as File)
            2 -> BookmarkHtmlImportTask(activity,
                    args.getSerializable("file") as File,
                    args.getSerializable("manager") as BookmarkManager,
                    args.getSerializable("folder") as BookmarkFolder,
                    Handler())
            3 -> BookmarkHtmlExportTask(activity,
                    args.getSerializable("file") as File,
                    args.getSerializable("folder") as BookmarkFolder)
            4 -> SpeedDialRestoreTask(activity, args.getSerializable("file") as File)
            5 -> SpeedDialBackupTask(activity, args.getSerializable("file") as File)
            else -> throw IllegalArgumentException("unknown id:$id")
        }
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<Boolean>, data: Boolean) {
        handler.sendEmptyMessage(0)

        if (data) {
            Toast.makeText(activity, R.string.succeed, Toast.LENGTH_SHORT).show()
            if (loader is RestoreTask) {
                AppUtils.restartApp(activity, true)
            }
        } else {
            Toast.makeText(activity, R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<Boolean>) {

    }

    private val appCompatActivity: AppCompatActivity
        get() = activity as AppCompatActivity

    private class DialogHandler : Handler() {
        private var dialogRef: WeakReference<DialogFragment>? = null

        override fun handleMessage(msg: Message) {
            if (dialogRef == null) return

            dialogRef!!.get()?.run {
                dismiss()
                dialogRef!!.clear()
            }
        }

        internal fun setDialog(dialog: DialogFragment?) {
            dialogRef = WeakReference<DialogFragment>(dialog)
        }
    }

    class PermissionDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity ?: throw IllegalStateException()

            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.permission_probrem)
                    .setMessage(R.string.confirm_permission_storage)
                    .setPositiveButton(android.R.string.ok
                    ) { _, _ ->
                        activity.openRequestPermissionSettings(getString(R.string.request_permission_storage_setting))
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> activity.onBackPressed() }
            isCancelable = false
            return builder.create()
        }
    }

    companion object {
        private const val EXT = ".yuzubackup"
        private const val EXT_SPEED_DIAL = ".yuzudial"

        private val handler = DialogHandler()
    }

}
