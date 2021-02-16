/*
 * Copyright (C) 2017-2021 Hazuki
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

package jp.hazuki.yuzubrowser.core.utility.storage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import java.io.File

const val DEFAULT_DOWNLOAD_PATH = "yuzu://download"

fun Uri.toDocumentFile(context: Context): DocumentFile {
    if (toString() == DEFAULT_DOWNLOAD_PATH) {
        return DocumentFile.fromFile(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File("/Downloads")
        } else {
            @Suppress("DEPRECATION")
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        })
    }
    return when (scheme) {
        ContentResolver.SCHEME_CONTENT -> if (isTreeUri()) {
            DocumentFile.fromTreeUri(context, this)!!
        } else {
            DocumentFile.fromSingleUri(context, this)!!
        }
        "file" -> DocumentFile.fromFile(File(path!!))
        else -> throw IllegalStateException("unknown scheme :$scheme, Uri:$this")
    }
}

private const val PATH_TREE = "tree"

private fun Uri.isTreeUri(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        DocumentsContract.isTreeUri(this)
    } else {
        val paths = pathSegments
        paths.size >= 2 && PATH_TREE == paths[0]
    }
}
