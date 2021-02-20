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

package jp.hazuki.yuzubrowser.download.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import jp.hazuki.asyncpermissions.AsyncPermissions
import jp.hazuki.asyncpermissions.PermissionResult
import jp.hazuki.yuzubrowser.core.MIME_TYPE_UNKNOWN
import jp.hazuki.yuzubrowser.core.utility.storage.toDocumentFile
import jp.hazuki.yuzubrowser.core.utility.utils.getMimeTypeFromExtension
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.download.R
import jp.hazuki.yuzubrowser.download.checkValidRootDir
import jp.hazuki.yuzubrowser.download.core.data.DownloadFile
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import jp.hazuki.yuzubrowser.download.core.data.MetaData
import jp.hazuki.yuzubrowser.download.core.downloader.Downloader
import jp.hazuki.yuzubrowser.download.getDownloadDocumentFile
import jp.hazuki.yuzubrowser.ui.app.DaggerThemeActivity
import jp.hazuki.yuzubrowser.ui.dialog.ProgressDialog
import jp.hazuki.yuzubrowser.ui.utils.checkStoragePermission
import jp.hazuki.yuzubrowser.ui.utils.openRequestPermissionSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class FastDownloadActivity : DaggerThemeActivity() {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent == null) {
            finish()
            return
        }

        val url = intent.getStringExtra(EXTRA_FILE_URL)

        if (url == null) {
            finish()
            return
        }

        ui {
            if (!checkStoragePermission()) {
                val permissions = AsyncPermissions(this@FastDownloadActivity)
                if (!requestStoragePermission(permissions)) {
                    finish()
                    return@ui
                }
            }

            val dialog = ProgressDialog(getString(R.string.now_downloading))
            dialog.show(supportFragmentManager, "dialog")
            val uri = withContext(Dispatchers.Default) {
                download(url,
                    intent.getStringExtra(EXTRA_FILE_REFERER),
                    intent.getStringExtra(EXTRA_USER_AGENT)!!,
                    intent.getStringExtra(EXTRA_DEFAULT_EXTENSION)!!)
            }
            dialog.dismiss()
            if (uri != null) {
                val result = Intent()
                result.data = uri

                val index = uri.toString().lastIndexOf(".")
                if (index > -1) {
                    val extension = uri.toString().substring(index + 1)
                    val mimeType = getMimeTypeFromExtension(extension)
                    if (mimeType != MIME_TYPE_UNKNOWN) {
                        result.putExtra(EXTRA_MINE_TYPE, mimeType)
                    }
                }

                setResult(RESULT_OK, result)
            } else {
                Toast.makeText(applicationContext, R.string.failed, Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
            }
            finish()
        }
    }

    private fun download(url: String, referrer: String?, ua: String, defExt: String): Uri? {
        val root = applicationContext.getDownloadDocumentFile()
        val file = DownloadFile(url, null, DownloadRequest(referrer, ua, defExt))
        val meta = MetaData(applicationContext, okHttpClient, root, file.url, file.request)
        val info = DownloadFileInfo(root.uri, file, meta)

        if (!checkValidRootDir(root.uri)) {
            file.request.isScopedStorageMode = true
            info.resumable = false

            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, info.name)
                put(MediaStore.Downloads.MIME_TYPE, info.mimeType)
                put(MediaStore.Downloads.IS_PENDING, 1)
                put(MediaStore.Downloads.IS_DOWNLOAD, 1)
            }

            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = contentResolver.insert(collection, values) ?: return null
            info.root = itemUri
        }

        val downloader = Downloader.getDownloader(applicationContext, okHttpClient, info, file.request)

        val result = downloader.download()

        if (file.request.isScopedStorageMode) {
            return if (result) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                contentResolver.update(info.root, values, null, null)

                info.root
            } else {
                val failedFile = info.root.toDocumentFile(this)
                if (failedFile.exists()) {
                    contentResolver.delete(failedFile.uri, null, null)
                }
                null
            }
        }

        return if (result) info.root.toDocumentFile(this).findFile(info.name)?.uri else null
    }

    private suspend fun requestStoragePermission(asyncPermissions: AsyncPermissions): Boolean {
        return handleStoragePermissionResult(
            asyncPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        )
    }

    private suspend fun handleStoragePermissionResult(result: PermissionResult): Boolean {
        return when (result) {
            is PermissionResult.Granted -> true
            is PermissionResult.ShouldShowRationale -> {
                return handleStoragePermissionResult(result.proceed())
            }
            is PermissionResult.NeverAskAgain -> {
                openRequestPermissionSettings(getString(R.string.request_permission_storage_setting))
                false
            }
            else -> false
        }
    }

    companion object {
        private const val EXTRA_FILE_URL = "fileURL"
        private const val EXTRA_FILE_REFERER = "fileReferer"
        private const val EXTRA_DEFAULT_EXTENSION = "defExt"
        private const val EXTRA_USER_AGENT = "ua"
        const val EXTRA_MINE_TYPE = "mineType"

        fun intent(context: Context, url: String, referrer: String?, ua: String, defExt: String): Intent {
            return Intent(context, FastDownloadActivity::class.java).apply {
                putExtra(EXTRA_FILE_URL, url)
                putExtra(EXTRA_FILE_REFERER, referrer)
                putExtra(EXTRA_USER_AGENT, ua)
                putExtra(EXTRA_DEFAULT_EXTENSION, defExt)
            }
        }
    }
}
