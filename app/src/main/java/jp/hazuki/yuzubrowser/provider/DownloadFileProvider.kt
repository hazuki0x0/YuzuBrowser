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

package jp.hazuki.yuzubrowser.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import jp.hazuki.yuzubrowser.BuildConfig
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class DownloadFileProvider : ContentProvider() {

    private var dataDir: String? = null

    override fun onCreate(): Boolean {
        dataDir = context!!.applicationInfo.dataDir
        return true
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val file = getFileForUri(uri)
        if (!file.checkPath()) return super.openFile(uri, mode)
        return ParcelFileDescriptor.open(file, modeToMode(mode))
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var colRequest = projection
        val file = getFileForUri(uri)
        if (!file.checkPath()) return null

        if (colRequest == null) {
            colRequest = COLUMNS
        }

        var cols = arrayOfNulls<String>(colRequest.size)
        var values = arrayOfNulls<Any>(colRequest.size)
        var i = 0
        for (col in colRequest) {
            if (OpenableColumns.DISPLAY_NAME == col) {
                cols[i] = OpenableColumns.DISPLAY_NAME
                values[i++] = file.name
            } else if (OpenableColumns.SIZE == col) {
                cols[i] = OpenableColumns.SIZE
                values[i++] = file.length()
            }
        }

        cols = ArrayUtils.copyOf(cols, i)
        values = ArrayUtils.copyOf(values, i)

        val cursor = MatrixCursor(cols, 1)
        cursor.addRow(values)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        val file = getFileForUri(uri)
        if (!file.checkPath()) return null
        return FileUtils.getMineType(file)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException()
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val file = getFileForUri(uri)
        return if (file.delete()) 1 else 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    private fun getFileForUri(uri: Uri): File {
        var path = uri.encodedPath

        val splitIndex = path!!.indexOf('/', 1)
        val tag = path.substring(1, splitIndex)
        path = Uri.decode(path.substring(splitIndex + 1))

        if (PATH != tag) {
            throw IllegalArgumentException("No files supported by provider at $uri")
        }

        var file = File(path!!)

        try {
            file = file.canonicalFile
        } catch (e: IOException) {
            throw IllegalArgumentException("Failed to resolve canonical path for $file")
        }

        if (file.absolutePath.startsWith(dataDir!!)) {
            throw IllegalArgumentException("No files supported by provider at $uri")
        }
        return file
    }

    private fun File.checkPath(): Boolean {
        return isFile &&
                path.startsWith("/storage/") ||
                path.startsWith(Environment.getExternalStorageDirectory().absolutePath) ||
                path.startsWith("/mnt/")
    }

    companion object {
        private val COLUMNS = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)

        const val PATH = "file"

        private const val SCHEME = "content"

        private const val AUTHORITY = BuildConfig.APPLICATION_ID + ".downloadFileProvider"

        fun getUriFromPath(filePath: String): Uri {
            val path = PATH + '/'.toString() + Uri.encode(filePath, "/")
            return Uri.Builder().scheme(SCHEME).authority(AUTHORITY).encodedPath(path).build()
        }

        fun getUriForFile(file: File): Uri {
            val path: String
            try {
                path = file.canonicalPath
            } catch (e: IOException) {
                throw IllegalArgumentException("Failed to resolve canonical path for $file")
            }

            return getUriFromPath(path)
        }

        /**
         * Copied from ContentResolver.java
         */
        private fun modeToMode(mode: String): Int {
            val modeBits: Int
            if ("r" == mode) {
                modeBits = ParcelFileDescriptor.MODE_READ_ONLY
            } else if ("w" == mode || "wt" == mode) {
                modeBits = (ParcelFileDescriptor.MODE_WRITE_ONLY
                        or ParcelFileDescriptor.MODE_CREATE
                        or ParcelFileDescriptor.MODE_TRUNCATE)
            } else if ("wa" == mode) {
                modeBits = (ParcelFileDescriptor.MODE_WRITE_ONLY
                        or ParcelFileDescriptor.MODE_CREATE
                        or ParcelFileDescriptor.MODE_APPEND)
            } else if ("rw" == mode) {
                modeBits = ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
            } else if ("rwt" == mode) {
                modeBits = (ParcelFileDescriptor.MODE_READ_WRITE
                        or ParcelFileDescriptor.MODE_CREATE
                        or ParcelFileDescriptor.MODE_TRUNCATE)
            } else {
                throw IllegalArgumentException("Invalid mode: $mode")
            }
            return modeBits
        }
    }
}
