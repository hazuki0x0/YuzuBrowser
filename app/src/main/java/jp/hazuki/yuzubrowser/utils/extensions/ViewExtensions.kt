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

package jp.hazuki.yuzubrowser.utils.extensions

import android.app.NotificationManager
import android.content.Context
import android.support.v4.app.NotificationCompat
import android.support.v4.provider.DocumentFile
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import jp.hazuki.yuzubrowser.Constants
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.download2.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download2.core.data.MetaData
import jp.hazuki.yuzubrowser.download2.service.DownloadDatabase
import jp.hazuki.yuzubrowser.download2.service.DownloadFile
import org.jetbrains.anko.toast
import java.io.File

inline fun ViewGroup.forEach(action: (View) -> Unit) {
    for (i in 0 until childCount) action(getChildAt(i))
}

fun WebView.saveArchive(root: DocumentFile, file: DownloadFile) {
    val tmpFile = File(context.cacheDir, "webArchive.tmp")
    saveWebArchive(tmpFile.absolutePath)

    var saveTo: DocumentFile? = null
    var size = 0L
    val name = file.name!!
    var success = tmpFile.exists()


    if (success) {
        saveTo = root.createFile(Constants.mimeType.MHTML, name)
        tmpFile.inputStream().use { input ->
            context.contentResolver.openOutputStream(saveTo.uri, "w").use { out ->
                input.copyTo(out)
            }
        }
        size = tmpFile.length()
        tmpFile.delete()

        success = saveTo.exists()
    }

    val info = DownloadFileInfo(root, file, MetaData(file.name!!, Constants.mimeType.MHTML, size, false))
    info.state = if (success) DownloadFileInfo.STATE_DOWNLOADED else DownloadFileInfo.STATE_UNKNOWN_ERROR
    DownloadDatabase.getInstance(context).insert(info)

    if (success && saveTo != null) {
        context.toast(context.getString(R.string.saved_file) + name)

        val notify = NotificationCompat.Builder(context, Constants.notification.CHANNEL_DOWNLOAD_NOTIFY)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(name)
                .setContentText(context.getText(R.string.download_success))
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(info.id.toInt(), notify)
    }
}