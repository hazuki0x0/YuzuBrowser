/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.download

import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.utils.HttpUtils
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity
import jp.hazuki.yuzubrowser.utils.async
import jp.hazuki.yuzubrowser.utils.net.HttpClientBuilder
import jp.hazuki.yuzubrowser.utils.ui
import jp.hazuki.yuzubrowser.utils.view.ProgressDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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
            val file = async { download(url, intent.getStringExtra(EXTRA_FILE_REFERER), intent.getStringExtra(EXTRA_DEFAULT_EXTENSION)) }.await()
            dialog.dismiss()
            if (file != null) {
                val result = Intent()
                result.data = DownloadFileProvider.getUriForFIle(file)

                val index = file.path.lastIndexOf(".")
                if (index > -1) {
                    val extension = file.path.substring(index + 1)
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

    private fun download(url: String, referrer: String?, defExt: String): File? {
        return if (url.startsWith("data:")) {
            DownloadUtils.saveBase64Image(url)
        } else {
            normalDownload(url, referrer, defExt)
        }
    }

    private fun normalDownload(url: String, referrer: String?, defExt: String): File? {
        val httpClient = HttpClientBuilder.createInstance(url) ?: return null

        val cookie = CookieManager.getInstance().getCookie(url)
        if (!cookie.isNullOrEmpty()) {
            httpClient.setHeader("Cookie", cookie)
        }

        if (!referrer.isNullOrEmpty()) {
            httpClient.setHeader("Referer", referrer)
        }

        val response = httpClient.connect()

        if (response == null) {
            httpClient.destroy()
            return null
        }

        val file = HttpUtils.getFileName(url, defExt, response.headerFields)
        if (file.parentFile != null) {
            file.parentFile.mkdirs()
        }

        try {
            FileOutputStream(file).use { outputStream ->
                response.inputStream.use { inputStream ->

                    var n: Int = -1
                    val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)

                    while (inputStream.read(buffer).let { n = it; n } >= 0) {
                        outputStream.write(buffer, 0, n)
                    }

                    outputStream.flush()

                    return file
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            httpClient.destroy()
        }
        return null
    }

    companion object {
        const val EXTRA_FILE_URL = "fileURL"
        const val EXTRA_FILE_REFERER = "fileReferer"
        const val EXTRA_DEFAULT_EXTENSION = "defExt"
        const val EXTRA_MINE_TYPE = "mineType"

        private const val DOWNLOAD_BUFFER_SIZE = 1024 * 10
    }
}
