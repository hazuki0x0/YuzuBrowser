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

package jp.hazuki.yuzubrowser.download.core.utils

import android.content.ContentResolver
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.support.v4.provider.DocumentFile
import android.text.format.Formatter
import android.util.Base64
import android.webkit.URLUtil
import jp.hazuki.yuzubrowser.Constants
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.createUniqueFileName
import jp.hazuki.yuzubrowser.utils.extensions.createFileOpenIntent
import java.io.File
import java.io.IOException
import java.util.regex.Pattern

fun ContentResolver.saveBase64Image(imageData: Base64Image, info: DownloadFileInfo): Boolean {
    if (imageData.isValid) {
        try {
            val image = Base64.decode(imageData.getData(), Base64.DEFAULT)

            openOutputStream(info.root.createFile(imageData.mimeType, info.name).uri).use { outputStream ->

                outputStream.write(image)
                outputStream.flush()
            }
            return true

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return false
}

fun decodeBase64Image(url: String): Base64Image {
    return Base64Image(url)
}

class Base64Image(url: String) {
    val data: Array<String> = url.split(',').dropLastWhile { it.isEmpty() }.toTypedArray()

    val isValid: Boolean
        get() = data.size >= 2

    val header: String
        get() = data[0]

    val mimeType: String
        get() = data[0].split(':')[1]

    fun getData(): String {
        return data[1]
    }
}

fun guessDownloadFileName(root: DocumentFile, url: String, contentDisposition: String?, mimetype: String?, defaultExt: String?): String {
    var guessType = mimetype
    if (url.startsWith("data:")) {
        val data = url.split(',').dropLastWhile { it.isEmpty() }.toTypedArray()
        if (data.size > 1) {
            guessType = data[0].split(Pattern.quote(";").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].substring(5)
        }
    }

    if ("application/octet-stream" == guessType) {
        guessType = null
    }

    var filename = URLUtil.guessFileName(url, contentDisposition, guessType)
    if (filename.isNullOrEmpty()) {
        filename = "index.html"

    } else if (filename.endsWith(".htm")) {
        filename += "l"
    }

    var extension = defaultExt
    if (filename.endsWith(".bin") && mimetype != null && defaultExt == null) {
        when (mimetype) {
            "multipart/related", "message/rfc822", "application/x-mimearchive" -> extension = ".mhtml"
            "application/javascript", "application/x-javascript", "text/javascript" -> extension = ".js"
        }
    }

    if (filename.endsWith(".bin") && extension != null) {
        var decodedUrl: String? = Uri.decode(url)
        if (decodedUrl != null) {
            val queryIndex = decodedUrl.indexOf('?')
            // If there is a query string strip it, same as desktop browsers
            if (queryIndex > 0) {
                decodedUrl = decodedUrl.substring(0, queryIndex)
            }
            if (!decodedUrl.endsWith("/")) {
                val index = decodedUrl.lastIndexOf('/') + 1
                if (index > 0) {
                    filename = decodedUrl.substring(index)
                    if (filename.indexOf('.') < 0) {
                        filename += defaultExt
                    }
                }
            }
        }
    }

    return createUniqueFileName(root, filename, Constants.download.TMP_FILE_SUFFIX)
}

fun getDownloadFolderUri(): Uri {
    return Uri.parse(AppData.download_folder.get())
}

fun DownloadFileInfo.createFileOpenIntent(context: Context) = createFileOpenIntent(context, root.findFile(name).uri, mimeType)

fun DownloadFileInfo.getNotificationString(context: Context): String {
    return if (size > 0) {
        "${currentSize * 100 / size}% (${Formatter.formatFileSize(context, currentSize)}" +
                " / ${Formatter.formatFileSize(context, size)}" +
                "  ${Formatter.formatFileSize(context, currentSize)}/s)"
    } else {
        Formatter.formatFileSize(context, currentSize) +
                "  ${Formatter.formatFileSize(context, currentSize)}/s"
    }
}

fun DownloadFileInfo.getFile(): DocumentFile? =
        if (state == DownloadFileInfo.STATE_DOWNLOADED) root.findFile(name) else null

fun DownloadFileInfo.checkFlag(flag: Int): Boolean = (state and flag) == flag

fun Uri.toDocumentFile(context: Context): DocumentFile {
    return when (scheme) {
        ContentResolver.SCHEME_CONTENT -> DocumentFile.fromTreeUri(context, this)
        "file" -> DocumentFile.fromFile(File(path))
        else -> throw IllegalStateException("unknown scheme :$scheme")
    }
}

fun Context.registerMediaScanner(vararg path: String) {
    MediaScannerConnection.scanFile(applicationContext, path, null, null)
}