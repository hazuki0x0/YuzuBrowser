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

package jp.hazuki.yuzubrowser.download.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import jp.hazuki.yuzubrowser.download.core.data.MetaData
import jp.hazuki.yuzubrowser.download.core.downloader.Downloader
import jp.hazuki.yuzubrowser.download.core.utils.toDocumentFile
import jp.hazuki.yuzubrowser.download.service.DownloadFile
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity
import jp.hazuki.yuzubrowser.utils.ui
import jp.hazuki.yuzubrowser.utils.view.ProgressDialog
import kotlinx.coroutines.async

class FastDownloadActivity : ThemeActivity() {

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
            val dialog = ProgressDialog(getString(R.string.now_downloading))
            dialog.show(supportFragmentManager, "dialog")
            val uri = async { download(url, intent.getStringExtra(EXTRA_FILE_REFERER), intent.getStringExtra(EXTRA_DEFAULT_EXTENSION)) }.await()
            dialog.dismiss()
            if (uri != null) {
                val result = Intent()
                result.data = uri

                val index = uri.toString().lastIndexOf(".")
                if (index > -1) {
                    val extension = uri.toString().substring(index + 1)
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    if (mimeType != null) {
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

    override fun lightThemeResource(): Int {
        return R.style.BrowserMinThemeLight_Transparent
    }

    private fun download(url: String, referrer: String?, defExt: String): Uri? {
        val root = Uri.parse(AppData.download_folder.get()).toDocumentFile(applicationContext)
        val file = DownloadFile(url, null, DownloadRequest(referrer, null, defExt))
        val meta = MetaData(applicationContext, root, file.url, file.request)
        val info = DownloadFileInfo(root, file, meta)
        val downloader = Downloader.getDownloader(applicationContext, info, file.request)

        val result = downloader.download()

        return if (result) info.root.findFile(info.name)?.uri else null
    }

    companion object {
        const val EXTRA_FILE_URL = "fileURL"
        const val EXTRA_FILE_REFERER = "fileReferer"
        const val EXTRA_DEFAULT_EXTENSION = "defExt"
        const val EXTRA_MINE_TYPE = "mineType"
    }
}
