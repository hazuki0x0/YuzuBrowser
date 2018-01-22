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

package jp.hazuki.yuzubrowser.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.provider.DocumentFile
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
    if (root.findFile(fileName) == null) return fileName

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
    } while (root.findFile(newName) != null && root.findFile(tmpName) != null)

    return newName
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

fun Context.getPathFromUri(uri: Uri): String? {

    if (DocumentsContract.isDocumentUri(this, uri)) { // DocumentProvider

        if (uri.isExternalStorageDocument()) { // ExternalStorageProvider
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }

            // TODO handle non-primary volumes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val path = "/storage/$type/${split[1]}"
                if (File(path).exists()) {
                    return path
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

fun Uri.isExternalStorageDocument(): Boolean {
    return "com.android.externalstorage.documents" == authority
}

fun Uri.isDownloadsDocument(): Boolean {
    return "com.android.providers.downloads.documents" == authority
}

fun Uri.isMediaDocument(): Boolean {
    return "com.android.providers.media.documents" == authority
}

fun Uri.isGooglePhotosUri(): Boolean {
    return "com.google.android.apps.photos.content" == authority
}