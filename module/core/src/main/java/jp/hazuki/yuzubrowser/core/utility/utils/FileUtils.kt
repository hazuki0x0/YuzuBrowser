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

package jp.hazuki.yuzubrowser.core.utility.utils

import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import jp.hazuki.yuzubrowser.core.MIME_TYPE_UNKNOWN
import jp.hazuki.yuzubrowser.core.utility.extensions.binarySearch
import jp.hazuki.yuzubrowser.core.utility.extensions.toSortedList
import java.io.File

val externalUserDirectory: File
    get() = File(Environment.getExternalStorageDirectory().toString() + File.separator + "YuzuBrowser" + File.separator)

fun createUniqueFileName(root: DocumentFile, fileName: String, suffix: String): String {
    val checkName = CheckName(root.listFiles())

    if (!checkName.exists(fileName) && !checkName.exists(fileName)) return fileName

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
    } while (checkName.exists(newName) || checkName.exists(tmpName))

    return newName
}

private fun Array<DocumentFile>.sortedFileName(): List<String> {
    return asSequence()
            .map { it.name }
            .filterNotNull()
            .toSortedList()
}

private class CheckName(items: Array<DocumentFile>) {
    private val files = items.sortedFileName()

    fun exists(name: String): Boolean {
        return files.binarySearch { it.compareTo(name) } >= 0
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
        return getMimeTypeFromExtension(extension)
    }
    return "application/octet-stream"
}

fun getMimeTypeFromExtension(extension: String): String {
    return when (extension) {
        "js" -> "application/javascript"
        "mhtml", "mht" -> "multipart/related"
        "json" -> "application/json"
        else -> {
            val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (type.isNullOrEmpty()) {
                MIME_TYPE_UNKNOWN
            } else {
                type
            }
        }
    }
}

fun getExtensionFromMimeType(mimeType: String): String? {
    return when (mimeType) {
        "multipart/related", "message/rfc822", "application/x-mimearchive" -> ".mhtml"
        "application/javascript", "application/x-javascript", "text/javascript" -> ".js"
        "application/json" -> ".json"
        else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }
}
