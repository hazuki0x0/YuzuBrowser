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

package jp.hazuki.yuzubrowser.legacy.download.core.utils

import android.content.ContentResolver
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.support.v4.provider.DocumentFile
import android.text.format.Formatter
import android.util.Base64
import android.webkit.MimeTypeMap
import jp.hazuki.yuzubrowser.legacy.Constants
import jp.hazuki.yuzubrowser.legacy.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData
import jp.hazuki.yuzubrowser.legacy.utils.FileUtils
import jp.hazuki.yuzubrowser.legacy.utils.createUniqueFileName
import jp.hazuki.yuzubrowser.legacy.utils.extensions.createFileOpenIntent
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.util.*
import java.util.regex.Pattern

fun ContentResolver.saveBase64Image(imageData: Base64Image, info: DownloadFileInfo): DocumentFile? {
    if (imageData.isValid) {
        try {
            val image = Base64.decode(imageData.getData(), Base64.DEFAULT)

            openOutputStream(info.root.createFile(imageData.mimeType, info.name)!!.uri).use { outputStream ->
                checkNotNull(outputStream)
                outputStream.write(image)
                outputStream.flush()
            }
            return info.root.findFile(info.name)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return null
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

    var filename = guessFileName(url, contentDisposition, guessType)
    if (filename.isEmpty()) {
        filename = "index.html"

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

private fun guessFileName(url: String, contentDisposition: String?, mimeType: String?): String {
    var fileName = if (contentDisposition != null) guessFileNameFromContentDisposition(contentDisposition) else null

    if (fileName != null) return fileName

    // If all the other http-related approaches failed, use the plain uri
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
                fileName = decodedUrl.substring(index)
            }
        }
    }

    // Finally, if couldn't get filename from URI, get a generic filename
    if (fileName == null) {
        fileName = "downloadfile"
    }

    val dotIndex = fileName.indexOf('.')
    var extension: String? = null

    if (dotIndex < 0) {
        if (mimeType != null) {
            extension = getExtensionFromMimeType(mimeType)
            if (extension != null) {
                extension = ".$extension"
            }
        }
        if (extension == null) {
            extension = if (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("text/")) {
                if (mimeType.equals("text/html", ignoreCase = true)) {
                    ".html"
                } else {
                    ".txt"
                }
            } else {
                ".bin"
            }
        }
    } else {
        if (mimeType != null) {
            // Compare the last segment of the extension against the mime type.
            // If there's a mismatch, discard the entire extension.
            val lastDotIndex = fileName.lastIndexOf('.')
            val typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileName.substring(lastDotIndex + 1))
            if (typeFromExt != null && !typeFromExt.equals(mimeType, ignoreCase = true)) {
                extension = getExtensionFromMimeType(mimeType)
                if (extension != null) {
                    extension = ".$extension"
                }
            }
        }
        if (extension == null) {
            extension = fileName.substring(dotIndex)
        }
        fileName = fileName.substring(0, dotIndex)
    }

    if (extension == ".htm") {
        extension = ".html"
    }

    return fileName + extension
}

private fun getExtensionFromMimeType(mimeType: String): String? {
    return when (mimeType) {
        "multipart/related", "message/rfc822", "application/x-mimearchive" -> ".mhtml"
        "application/javascript", "application/x-javascript", "text/javascript" -> ".js"
        else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }
}

private const val NAME_UTF_8 = "filename\\*=UTF-8''(\\S+)"
private const val NAME_NORMAL = "filename=\"(.*)\""
private const val NAME_NO_QUOT = "filename=(\\S+)"

fun guessFileNameFromContentDisposition(contentDisposition: String): String? {
    val utf8 = NAME_UTF_8.toRegex().find(contentDisposition)
    if (utf8 != null) {
        /** RFC 6266 */
        return URLDecoder.decode(utf8.groupValues[1], "UTF-8")
    }

    val normal = NAME_NORMAL.toRegex().find(contentDisposition)
    if (normal != null) {
        return try {
            URLDecoder.decode(normal.groupValues[1], "UTF-8")
        } catch (e: IllegalArgumentException) {
            FileUtils.replaceProhibitionWord(normal.groupValues[1])
        }
    }
    val noQuot = NAME_NO_QUOT.toRegex().find(contentDisposition)
    if (noQuot != null) {
        return try {
            URLDecoder.decode(noQuot.groupValues[1], "UTF-8")
        } catch (e: IllegalArgumentException) {
            FileUtils.replaceProhibitionWord(noQuot.groupValues[1])
        }
    }

    return null
}

fun getDownloadFolderUri(): Uri {
    return Uri.parse(AppData.download_folder.get())
}

fun DownloadFileInfo.createFileOpenIntent(context: Context, downloadedFile: DocumentFile) = createFileOpenIntent(context, downloadedFile.uri, mimeType, name)

fun DownloadFileInfo.getNotificationString(context: Context): String {
    return if (size > 0) {
        "${currentSize * 100 / size}% (${Formatter.formatFileSize(context, currentSize)}" +
                " / ${Formatter.formatFileSize(context, size)}" +
                "  ${Formatter.formatFileSize(context, transferSpeed)}/s)"
    } else {
        Formatter.formatFileSize(context, currentSize) +
                "  ${Formatter.formatFileSize(context, transferSpeed)}/s"
    }
}

fun DownloadFileInfo.getFile(): DocumentFile? =
        if (state == DownloadFileInfo.STATE_DOWNLOADED) root.findFile(name) else null

fun DownloadFileInfo.checkFlag(flag: Int): Boolean = (state and flag) == flag

fun Uri.toDocumentFile(context: Context): DocumentFile {
    return when (scheme) {
        ContentResolver.SCHEME_CONTENT -> DocumentFile.fromTreeUri(context, this)!!
        "file" -> DocumentFile.fromFile(File(path))
        else -> throw IllegalStateException("unknown scheme :$scheme, Uri:$this")
    }
}

fun Context.registerMediaScanner(vararg path: String) {
    MediaScannerConnection.scanFile(applicationContext, path, null, null)
}