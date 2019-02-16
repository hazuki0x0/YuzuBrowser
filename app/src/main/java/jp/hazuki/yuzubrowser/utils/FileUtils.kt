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

package jp.hazuki.yuzubrowser.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.provider.DocumentFile
import android.webkit.MimeTypeMap
import jp.hazuki.yuzubrowser.Constants
import java.io.File


fun createUniqueFileName(root: DocumentFile, fileName: String): String {
    if (root.findFile(fileName) == null) return fileName

    val parsedName = getParsedFileName(FileUtils.replaceProhibitionWord(fileName))
    var i = 1
    val builder = StringBuilder()
    val isSuffix = parsedName.suffix != null
    var newName: String
    do {
        builder.append(parsedName.prefix).append('-').append(i++)
        if (isSuffix) {
            builder.append('.').append(parsedName.suffix)
        }
        newName = builder.toString()
        builder.delete(0, builder.length)
    } while (root.findFile(newName) != null)

    return newName
}

fun createUniqueFileName(root: DocumentFile, fileName: String, suffix: String): String {
    if (root.findFile(fileName) == null && root.findFile(fileName + suffix) == null) return fileName

    val checkName = CheckName(root)
    val parsedName = getParsedFileName(FileUtils.replaceProhibitionWord(fileName))
    var i = 1
    val builder = StringBuilder()
    val isSuffix = parsedName.suffix != null
    var newName: String
    var tmpName: String
    do {
        builder.append(parsedName.prefix).append('-').append(i++)
        if (isSuffix) {
            builder.append('.').append(parsedName.suffix)
        }
        newName = builder.toString()
        builder.append(suffix)
        tmpName = builder.toString()
        builder.delete(0, builder.length)
    } while (checkName.exists(newName) || root.findFile(tmpName) != null)

    return newName
}

private fun DocumentFile.sortedFileName(): List<String> {
    val files = listFiles()
    return files.map { it.name ?: "" }
            .sortedWith(Comparator { s1, s2 ->
                val compared = compareLength(s1, s2)
                if (compared == 0) {
                    s1.compareTo(s2)
                } else {
                    compared
                }
            })
}

private class CheckName(root: DocumentFile) {
    private val files = root.sortedFileName()
    private val length = files.size
    private var index = 0

    fun exists(name: String): Boolean {
        while (length > index) {
            if (files[index] == name) return true
            index++
        }
        return false
    }
}

private fun compareLength(s1: String, s2: String): Int {
    return when {
        s1.length > s2.length -> 1
        s1.length < s2.length -> -1
        else -> 0
    }
}

class ParsedFileName internal constructor(var prefix: String, var suffix: String?)

fun getParsedFileName(filename: String): ParsedFileName {
    val point = filename.lastIndexOf(".")
    return if (point >= 0) {
        ParsedFileName(filename.substring(0, point), filename.substring(point + 1))
    } else {
        ParsedFileName(filename, null)
    }
}

fun getMimeType(fileName: String): String {
    val lastDot = fileName.lastIndexOf('.')
    if (lastDot >= 0) {
        val extension = fileName.substring(lastDot + 1).toLowerCase()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mime != null) {
            return mime
        }

        when (extension) {
            "mht", "mhtml" -> return "multipart/related"
            "js" -> return "application/javascript"
        }
    }
    return Constants.mimeType.UNKNOWN
}

fun Uri.isAlwaysConvertible(): Boolean {
    if (scheme == "file") return true

    if (isContentUri() && isTreeUri()) {
        val place = pathSegments
        val split = place[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]

        return "primary".equals(type, ignoreCase = true)
    }

    return false
}

fun Context.getPathFromUri(uri: Uri): String? {

    if (DocumentsContract.isDocumentUri(this, uri)) { // DocumentProvider

        if (uri.isExternalStorageDocument()) { // ExternalStorageProvider
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (split.size >= 2) {
                val type = split[0]
                val result = resolveStorage(type, split[1])
                if (result != null && result.isNotEmpty()) {
                    return result
                }
            }

        } else if (uri.isContentUri() && uri.isTreeUri()) { // Tree Uri
            val place = uri.pathSegments
            val split = place[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (split.size >= 2) {
                val type = split[0]
                val result = resolveStorage(type, split[1])
                if (result != null && result.isNotEmpty()) {
                    return result
                }
            }

        } else if (uri.isDownloadsDocument()) { // DownloadsProvider
            val id = DocumentsContract.getDocumentId(uri)
            val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong())

            return getDataColumn(contentUri, null, null)

        } else if (uri.isMediaDocument()) { // MediaProvider
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            val contentUri = when (type) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> null
            }

            if (contentUri != null) {
                return getDataColumn(contentUri, "_id=?", arrayOf(split[1]))
            }
        }
    } else if ("content".equals(uri.scheme, ignoreCase = true)) { // MediaStore (and general)

        if (uri.isGooglePhotosUri()) return uri.lastPathSegment

        return getDataColumn(uri, null, null)

    } else if ("file".equals(uri.scheme, ignoreCase = true)) { // File
        return uri.path
    }
    return null
}

private fun Context.resolveStorage(type: String, extPath: String): String? {
    if ("primary".equals(type, ignoreCase = true)) {
        return Environment.getExternalStorageDirectory().toString() + "/" + extPath
    }

    // TODO handle non-primary volumes
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val path = "/storage/$type/$extPath"
        if (File(path).exists()) {
            return path
        }
    }
    val storages = getExternalStorageDirectories()
    for (storage in storages) {
        val path = (if (storage.endsWith("/")) storage else "$storage/") + extPath
        if (File(path).exists()) {
            return path
        }
    }

    return null
}

/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param uri The Uri to query.
 * @param selection (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
fun Context.getDataColumn(uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {

    val column = "_data"
    val projection = arrayOf(column)

    contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { c ->
        if (c.moveToFirst()) {
            val index = c.getColumnIndex(column)
            if (index >= 0) {
                return c.getString(index)
            }
        }
    }
    return null
}

private fun Uri.isExternalStorageDocument(): Boolean {
    return "com.android.externalstorage.documents" == authority
}

private fun Uri.isDownloadsDocument(): Boolean {
    return "com.android.providers.downloads.documents" == authority
}

private fun Uri.isMediaDocument(): Boolean {
    return "com.android.providers.media.documents" == authority
}

private fun Uri.isGooglePhotosUri(): Boolean {
    return "com.google.android.apps.photos.content" == authority
}

//Copy from DocumentsContract.java
private const val PATH_TREE = "tree"

private fun Uri.isTreeUri(): Boolean {
    val paths = pathSegments
    return paths.size >= 2 && PATH_TREE == paths[0]
}

private fun Uri.isContentUri(): Boolean {
    return ContentResolver.SCHEME_CONTENT == scheme
}