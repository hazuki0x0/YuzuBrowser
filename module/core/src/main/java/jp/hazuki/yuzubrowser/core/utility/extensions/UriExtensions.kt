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

package jp.hazuki.yuzubrowser.core.utility.extensions

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import jp.hazuki.yuzubrowser.core.utility.storage.getStorageList
import java.io.File
import java.net.IDN

fun Uri.getWritableFileOrNull(): File? {
    if (scheme == "file") return File(toString())

    when {
        isExternalStorageDocument() -> {
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(':').dropLastWhile { it.isEmpty() }.toTypedArray()

            if (split.size >= 2 && split[0] == "primary") {
                return File(buildExternalStoragePath(split[1]))
            }
        }
        isDownloadsDocument() -> {
            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (folder.canWrite()) {
                return folder
            }
        }
        isContentUri() && isTreeUri() -> {
            val split = pathSegments[1].split(':').dropLastWhile { it.isEmpty() }.toTypedArray()

            if (split.size >= 2 && split[0] == "primary") {
                return File(buildExternalStoragePath(split[1]))
            }
        }
    }

    return null
}

private fun buildExternalStoragePath(extPath: String): String {
    val builder = StringBuilder(Environment.getExternalStorageDirectory().path)
    if (builder.endsWith('/')) {
        builder.append(extPath)
    } else {
        builder.append('/').append(extPath)
    }
    return builder.toString()
}

fun Uri.resolvePath(context: Context): String? {

    if (DocumentsContract.isDocumentUri(context, this)) { // DocumentProvider

        if (isExternalStorageDocument()) { // ExternalStorageProvider
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(':').dropLastWhile { it.isEmpty() }.toTypedArray()

            if (split.size >= 2) {
                val type = split[0]
                val result = context.resolveFilePath(type, split[1])
                if (result != null && result.isNotEmpty()) {
                    return result
                }
            }

        } else if (isExternalStorageDocument()) { // Tree Uri
            val split = pathSegments[1].split(':').dropLastWhile { it.isEmpty() }.toTypedArray()

            if (split.size >= 2) {
                val type = split[0]
                val result = context.resolveFilePath(type, split[1])
                if (result != null && result.isNotEmpty()) {
                    return result
                }
            }

        } else if (isDownloadsDocument()) { // DownloadsProvider
            val id = DocumentsContract.getDocumentId(this)
            if (id.startsWith("raw:")) {
                return id.substring(4)
            }
            context.contentResolver.query(this, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val fileName = c.getString(0)
                    val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName).absolutePath
                    if (path.isNotEmpty()) return path
                }
            }
            val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong())

            return context.getDataColumn(contentUri, null, null)

        } else if (isMediaDocument()) { // MediaProvider
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(':').dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            val contentUri = when (type) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> null
            }

            if (contentUri != null) {
                return context.getDataColumn(contentUri, "_id=?", arrayOf(split[1]))
            }
        } else if (isContentUri() && isTreeUri()) { // Fallback
            val split = pathSegments[1].split(':').dropLastWhile { it.isEmpty() }.toTypedArray()

            if (split.size >= 2) {
                val type = split[0]
                val result = context.resolveFilePath(type, split[1])
                if (result != null && result.isNotEmpty()) {
                    return result
                }
            }
        }
    } else if ("content".equals(scheme, ignoreCase = true)) { // MediaStore (and general)

        if (isGooglePhotosUri()) return lastPathSegment

        return context.getDataColumn(this, null, null)

    } else if ("file".equals(scheme, ignoreCase = true)) { // File
        return path
    }
    return null
}

private fun Context.resolveFilePath(uuid: String, extPath: String): String? {
    val path = resolveStoragePath(uuid)
    if (path != null) {
        return if (path.endsWith('/'))
            "$path$extPath"
        else
            "$path/$extPath"
    }
    return null
}

fun Uri.resolveDirectoryPath(context: Context): String? {
    return if (isFileUri()) {
        toString().substring(7) // trim "file://"
    } else if (isDownloadsDocument()) {
        "${Environment.getExternalStorageDirectory()}/Download"
    } else if (isContentUri() && isExternalStorageDocument() && isTreeUri()) {
        val split = pathSegments[1].split(':').dropLastWhile { it.isEmpty() }.toTypedArray()
        if (split.size == 1) {
            context.resolveStoragePath(split[0])
        } else {
            "${context.resolveStoragePath(split[0])}/${split[1]}"
        }
    } else {
        null
    }
}

fun Uri.canResolvePath(context: Context): Boolean {
    val path = resolveDirectoryPath(context)
    if (path != null) {
        return File(path).exists()
    }
    return false
}

private fun Context.resolveStoragePath(uuid: String): String? {
    getStorageList().forEach {
        if (it.uuid == uuid) return it.path
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
private fun Context.getDataColumn(uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {

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

private fun Uri.isFileUri(): Boolean {
    return "file" == scheme
}

fun Uri.decodeUrl(): String {
    if (isOpaque) {
        val builder = StringBuilder(scheme!!).append(":")
        if (isValid(schemeSpecificPart)) {
            builder.append(schemeSpecificPart)
        } else {
            builder.append(encodedSchemeSpecificPart)
        }
        val fragment = fragment
        if (!TextUtils.isEmpty(fragment)) {
            builder.append("#")
            if (isValid(fragment)) {
                builder.append(fragment)
            } else {
                builder.append(encodedFragment)
            }
        }
        return builder.toString()
    } else {
        val decode = buildUpon()
        if (isValid(query))
            decode.encodedQuery(query)
        if (isValid(fragment))
            decode.encodedFragment(fragment)
        if (isValid(path))
            decode.encodedPath(path)
        decode.encodedAuthority(decodeAuthority())
        return decode.build().toString()
    }
}


private fun Uri.decodeAuthority(): String? {
    var host = host
    if (TextUtils.isEmpty(host)) {
        return encodedAuthority
    } else {
        host = IDN.toUnicode(host)
    }

    val userInfo = userInfo
    val noUserInfo = TextUtils.isEmpty(userInfo)
    val port = port

    if (noUserInfo && port == -1)
        return host

    val builder = StringBuilder()

    if (!noUserInfo) {
        if (isValid(userInfo)) {
            builder.append(userInfo).append('@')
        } else {
            builder.append(encodedUserInfo).append('@')
        }
    }

    builder.append(host)

    if (port > -1) {
        builder.append(":").append(port)
    }

    return builder.toString()
}

fun Uri.toEncodePunyCodeUri(): Uri {
    return host?.let { buildUpon().authority(encodeAuthority()).build() } ?: this
}

fun Uri.encodeAuthority(): String {
    if (host.isNullOrEmpty()) return encodeAuthority()

    val host = IDN.toASCII(host)

    val userInfo = userInfo
    val port = port

    if (userInfo.isNullOrEmpty() && port < 0) return host

    val builder = StringBuilder()

    if (!userInfo.isNullOrEmpty()) {
        if (isValid(userInfo)) {
            builder.append(userInfo).append('@')
        } else {
            builder.append(encodedUserInfo).append('@')
        }
    }

    builder.append(host)

    if (port > -1) {
        builder.append(":").append(port)
    }
    return authority ?: ""
}

private fun isValid(str: String?): Boolean {
    return str != null && str.indexOf(INVALID_CHAR) <= -1
}

private const val INVALID_CHAR = '\uFFFD'
