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

package jp.hazuki.yuzubrowser.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import jp.hazuki.yuzubrowser.core.MIME_TYPE_UNKNOWN
import jp.hazuki.yuzubrowser.core.utility.extensions.resolvePath
import jp.hazuki.yuzubrowser.core.utility.utils.getMimeType
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.ui.BrowserApplication
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs


fun createFileOpenIntent(context: Context, uri: Uri, mimeType: String, name: String): Intent {
    val target = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val provider = (context.applicationContext as BrowserApplication).providerManager.downloadFileProvider
        val path = uri.path ?: ""
        if (uri.scheme == "file") provider.getUriFromPath(path) else uri
    } else {
        if (uri.scheme == "file") {
            uri
        } else {
            val path = uri.resolvePath(context)
            if (path != null) Uri.parse("file://$path") else uri
        }
    }

    var resolvedMineType = getMimeType(name)
    if (resolvedMineType == MIME_TYPE_UNKNOWN) {
        resolvedMineType = mimeType
    }

    return Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        setDataAndType(target, resolvedMineType)
    }
}

fun getDownloadFolderUri(context: Context): Uri {
    return Uri.parse(AppPrefs.download_folder.get())
}

fun Context.registerDownloadNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val service = NotificationChannel(
                NOTIFICATION_CHANNEL_DOWNLOAD_SERVICE,
                getString(R.string.download_service),
                NotificationManager.IMPORTANCE_MIN)

        service.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val notify = NotificationChannel(
                NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY,
                getString(R.string.download_notify),
                NotificationManager.IMPORTANCE_LOW)

        notify.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        manager.createNotificationChannels(listOf(service, notify))
    }
}

fun DownloadFileInfo.createFileOpenIntent(context: Context, downloadedFile: DocumentFile): Intent {
    val uri = if (downloadedFile.uri.scheme == "file") {
        downloadedFile.uri
    } else {
        Uri.parse("file://${downloadedFile.uri.resolvePath(context)}") ?: downloadedFile.uri
    }
    return createFileOpenIntent(context, uri, mimeType, name)
}

fun Context.getBlobDownloadJavaScript(url: String, secretKey: String, type: Int = 0): String {
    return "var xhr=new XMLHttpRequest;xhr.open('GET','$url',!0),xhr." +
            "responseType='blob',xhr.onload=function(){if(200==this.status){var e=this.re" +
            "sponse,n=new FileReader;n.onloadend=function(){base64data=n.result,window.lo" +
            "cation.href='yuzu:download-file/$secretKey&$type&'+encodeURIComponent(base64" +
            "data)},n.readAsDataURL(e)}},xhr.onerror=function(){alert('" +
            "${getString(R.string.js_download_cross_origin)}')},xhr.send();"
}
