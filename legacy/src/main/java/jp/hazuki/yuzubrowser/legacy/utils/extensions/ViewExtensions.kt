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

package jp.hazuki.yuzubrowser.legacy.utils.extensions

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import jp.hazuki.yuzubrowser.legacy.Constants
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.legacy.download.core.data.MetaData
import jp.hazuki.yuzubrowser.legacy.download.core.utils.createFileOpenIntent
import jp.hazuki.yuzubrowser.legacy.download.service.DownloadDatabase
import jp.hazuki.yuzubrowser.legacy.download.service.DownloadFile
import jp.hazuki.yuzubrowser.legacy.utils.ui
import jp.hazuki.yuzubrowser.webview.CustomWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import java.io.File

fun CustomWebView.saveArchive(root: androidx.documentfile.provider.DocumentFile, file: DownloadFile) {
    ui {
        val context = webView.context
        val tmpFile = File(context.cacheDir, "page.tmp")
        saveWebArchiveMethod(tmpFile.absolutePath)

        delay(1000)
        var success = tmpFile.exists()
        if (success) {
            var size = 0L
            do {
                delay(200)
                val oldSize = size
                size = tmpFile.length()
            } while (oldSize != size)

            val name = file.name!!

            val saveTo = root.createFile(Constants.mimeType.MHTML, name)
            if (saveTo == null) {
                context.toast(R.string.failed)
                return@ui
            }
            withContext(Dispatchers.Default) {
            tmpFile.inputStream().use { input ->
                context.contentResolver.openOutputStream(saveTo.uri, "w").use { out ->
                    checkNotNull(out)
                    input.copyTo(out)
                }
            }
            tmpFile.delete()
        }

            success = saveTo.exists()

            val info = DownloadFileInfo(root, file, MetaData(file.name!!, Constants.mimeType.MHTML, size, false))
            info.state = if (success) DownloadFileInfo.STATE_DOWNLOADED else DownloadFileInfo.STATE_UNKNOWN_ERROR
            DownloadDatabase.getInstance(context).insert(info)

            if (success) {
                context.toast(context.getString(R.string.saved_file) + name)

                val notify = NotificationCompat.Builder(context, Constants.notification.CHANNEL_DOWNLOAD_NOTIFY)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setContentTitle(name)
                        .setContentText(context.getText(R.string.download_success))
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentIntent(PendingIntent.getActivity(context.applicationContext, 0, info.createFileOpenIntent(context, saveTo), 0))
                        .build()

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(info.id.toInt(), notify)
            }
        }
    }
}