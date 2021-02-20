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

package jp.hazuki.yuzubrowser.legacy.utils.extensions

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import jp.hazuki.yuzubrowser.core.MIME_TYPE_MHTML
import jp.hazuki.yuzubrowser.core.utility.extensions.getWritableFileOrNull
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.utils.getMimeType
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.download.NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY
import jp.hazuki.yuzubrowser.download.core.data.DownloadFile
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.data.MetaData
import jp.hazuki.yuzubrowser.download.createFileOpenIntent
import jp.hazuki.yuzubrowser.download.repository.DownloadsDao
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.widget.toast
import jp.hazuki.yuzubrowser.webview.CustomWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

fun CustomWebView.saveArchive(downloadsDao: DownloadsDao, root: DocumentFile, file: DownloadFile) {
    ui {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val outFile = root.uri.getWritableFileOrNull()

            if (outFile != null && outFile.exists()) {
                val downloadedFile = File(outFile, file.name!!)
                saveWebArchiveMethod(downloadedFile.toString())
                onDownload(webView.context, downloadsDao, root, file, DocumentFile.fromFile(downloadedFile), true, downloadedFile.length())
                return@ui
            }
        }

        val context = webView.context
        val tmpFile = File(context.cacheDir, "page.tmp")
        saveWebArchiveMethod(tmpFile.absolutePath)

        delay(1000)
        var success = tmpFile.exists()
        if (success) {
            val size = withContext(Dispatchers.IO) {
                var size = 0L
                do {
                    delay(500)
                    val oldSize = size
                    size = tmpFile.length()
                } while (size == 0L || oldSize != size)
                return@withContext size
            }

            val name = file.name!!

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && root.uri.scheme == "file") {
                context.copyArchive(downloadsDao, tmpFile, name, file)
                return@ui
            }
            val saveTo = root.createFile(MIME_TYPE_MHTML, name)
            if (saveTo == null) {
                context.toast(R.string.failed)
                return@ui
            }

            withContext(Dispatchers.IO) {
                tmpFile.inputStream().use { input ->
                    context.contentResolver.openOutputStream(saveTo.uri, "w")!!.use { out ->
                        input.copyTo(out)
                    }
                }
                tmpFile.delete()
            }

            success = saveTo.exists()

            onDownload(context, downloadsDao, root, file, saveTo, success, size)
        }
    }
}

private suspend fun Context.copyArchive(downloadsDao: DownloadsDao, tmpFile: File, name: String, file: DownloadFile) {
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, name)
        put(MediaStore.Downloads.MIME_TYPE, getMimeType(name))
        put(MediaStore.Downloads.IS_DOWNLOAD, 1)
        put(MediaStore.Downloads.IS_PENDING, 1)
    }

    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val uri = contentResolver.insert(collection, values)

    if (uri == null) {
        tmpFile.delete()
        return
    }

    val result = withContext(Dispatchers.IO) {
        try {
            contentResolver.openOutputStream(uri)?.use { os ->
                tmpFile.inputStream().use {
                    it.copyTo(os)
                    values.apply {
                        clear()
                        put(MediaStore.Downloads.IS_PENDING, 0)
                    }
                    val dFile = DocumentFile.fromSingleUri(this@copyArchive, uri)!!
                    val size = dFile.length()
                    val info = DownloadFileInfo(uri, file, MetaData(name, MIME_TYPE_MHTML, size, false))
                    info.state = DownloadFileInfo.STATE_DOWNLOADED
                    downloadsDao.insert(info)
                    contentResolver.update(uri, values, null, null)
                    return@withContext true
                }
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        } finally {
            tmpFile.delete()
        }
        return@withContext false
    }

    if (!result) {
        contentResolver.delete(uri, null, null)
    }
}

private suspend fun onDownload(
    context: Context,
    dao: DownloadsDao,
    root: DocumentFile,
    file: DownloadFile,
    downloadedFile: DocumentFile,
    success: Boolean,
    size: Long
) {
    val name = file.name!!

    val info = DownloadFileInfo(root.uri, file, MetaData(name, MIME_TYPE_MHTML, size, false)).also {
        it.state = if (success) {
            DownloadFileInfo.STATE_DOWNLOADED
        } else {
            DownloadFileInfo.STATE_UNKNOWN_ERROR
        }
        it.id = dao.insertAsync(it)
    }

    if (success) {
        context.toast(context.getString(R.string.saved_file) + name)

        val notify = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setContentTitle(name)
            .setContentText(context.getText(R.string.download_success))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(PendingIntent.getActivity(context.applicationContext, 0, info.createFileOpenIntent(context, downloadedFile), 0))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(info.id.toInt(), notify)
    }
}
