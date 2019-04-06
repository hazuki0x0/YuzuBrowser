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

import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.AbstractCursor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import com.squareup.moshi.Moshi
import dagger.android.DaggerContentProvider
import jp.hazuki.yuzubrowser.BuildConfig
import jp.hazuki.yuzubrowser.legacy.readitlater.ReadItLaterIndex
import jp.hazuki.yuzubrowser.legacy.readitlater.ReadItem
import jp.hazuki.yuzubrowser.ui.provider.IReadItLaterProvider
import java.io.File
import javax.inject.Inject

class ReadItLaterProvider : DaggerContentProvider() {

    private lateinit var directory: File
    private lateinit var index: ReadItLaterIndex
    @Inject
    lateinit var moshi: Moshi

    companion object {
        const val TIME = IReadItLaterProvider.TIME
        const val URL = IReadItLaterProvider.URL
        const val TITLE = IReadItLaterProvider.TITLE
        const val PATH = IReadItLaterProvider.PATH

        const val COL_TIME = IReadItLaterProvider.COL_TIME
        const val COL_URL = IReadItLaterProvider.COL_URL
        const val COL_TITLE = IReadItLaterProvider.COL_TITLE

        private const val SCHEME = "content"
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.readItLaterProvider"

        private const val TYPE_READ = 1
        private const val TYPE_EDIT = 2
        private const val TYPE_PATH = 3

        private val ITEM_COLUMNS: Array<String>
        private val FILE_COLUMNS: Array<String>
        private val URI = Uri.parse("$SCHEME://$AUTHORITY")
        val EDIT_URI = Uri.parse("$SCHEME://$AUTHORITY/index")!!
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(AUTHORITY, "read", TYPE_READ)
            uriMatcher.addURI(AUTHORITY, "index", TYPE_EDIT)
            uriMatcher.addURI(AUTHORITY, "path", TYPE_PATH)
            uriMatcher.addURI(AUTHORITY, "read/*", TYPE_READ)
            uriMatcher.addURI(AUTHORITY, "index/*", TYPE_EDIT)
            uriMatcher.addURI(AUTHORITY, "path/*", TYPE_PATH)
            ITEM_COLUMNS = kotlin.arrayOf(TIME, URL, TITLE)
            FILE_COLUMNS = kotlin.arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        }

        fun getReadUri(time: Long): Uri {
            return URI.buildUpon().appendPath("read").appendPath(time.toString()).build()
        }

        fun getEditUri(time: Long): Uri {
            return EDIT_URI.buildUpon().appendPath(time.toString()).build()
        }

        fun convertToEdit(uri: Uri): Uri {
            return EDIT_URI.buildUpon().appendPath(uri.lastPathSegment).build()
        }
    }

    override fun onCreate(): Boolean {
        super.onCreate()
        directory = context!!.getDir("readItLater", Context.MODE_PRIVATE)
        index = ReadItLaterIndex(moshi, directory)
        return true
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri {
        if (uriMatcher.match(p0) == TYPE_EDIT && p1 != null) {
            val name = p1.getAsString(TITLE)
            val url = p1.getAsString(URL)
            val time = System.currentTimeMillis()
            index.add(ReadItem(time, url, name))
            index.save()
            return getUri(time)
        }
        throw IllegalArgumentException("Unknown URI $p0")
    }

    override fun query(p0: Uri, projection: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor? {
        val type = uriMatcher.match(p0)
        val file = getFileForUri(p0)
        val name = file.name
        val item = index.firstOrNull { item -> item.time.toString() == name }
        return when (type) {
            TYPE_READ -> if (item != null) queryFile(item, file, projection) else null
            TYPE_EDIT -> queryList(item, projection)
            TYPE_PATH -> queryPath(file)
            else -> null
        }
    }

    private fun queryList(item: ReadItem?, projection: Array<out String>?): Cursor {
        if (item != null) {
            val cols = ArrayList<String>()
            val values = ArrayList<kotlin.Any>()
            projection ?: ITEM_COLUMNS.forEach {
                when (it) {
                    TIME -> {
                        cols.add(TIME)
                        values.add(item.time)
                    }
                    URL -> {
                        cols.add(URL)
                        values.add(item.url)
                    }
                    TITLE -> {
                        cols.add(TITLE)
                        values.add(item.title)
                    }
                }
            }

            val cursor = MatrixCursor(cols.toTypedArray(), 1)
            cursor.addRow(values.toArray())
            return cursor
        } else {
            return ListCursor(index)
        }
    }

    private class ListCursor(val items: List<ReadItem>) : AbstractCursor() {


        override fun getCount(): Int {
            return items.size
        }

        override fun getColumnNames(): Array<String> {
            return ITEM_COLUMNS
        }

        override fun getShort(p0: Int): Short {
            throw UnsupportedOperationException()
        }

        override fun getFloat(p0: Int): Float {
            throw UnsupportedOperationException()
        }

        override fun getDouble(p0: Int): Double {
            throw UnsupportedOperationException()
        }

        override fun isNull(p0: Int): Boolean {
            throw UnsupportedOperationException()
        }

        override fun getInt(p0: Int): Int {
            throw UnsupportedOperationException()
        }

        override fun getLong(p0: Int): Long {
            if (p0 == COL_TIME) {
                return items[position].time
            }
            throw UnsupportedOperationException()
        }

        override fun getString(p0: Int): String {
            return when (p0) {
                COL_URL -> items[position].url
                COL_TITLE -> items[position].title
                else -> throw UnsupportedOperationException()
            }
        }
    }

    private fun queryFile(item: ReadItem, file: File, projection: Array<out String>?): Cursor {
        val cols = ArrayList<String>()
        val values = ArrayList<kotlin.Any>()
        projection ?: FILE_COLUMNS.forEach { s ->
            if (OpenableColumns.DISPLAY_NAME == s) {
                cols.add(OpenableColumns.DISPLAY_NAME)
                values.add(item.title)
            } else if (OpenableColumns.SIZE == s) {
                cols.add(OpenableColumns.SIZE)
                values.add(file.length())
            }
        }

        val cursor = MatrixCursor(cols.toTypedArray(), 1)
        cursor.addRow(values.toArray())
        return cursor
    }

    private fun queryPath(file: File): Cursor {
        val cursor = MatrixCursor(kotlin.arrayOf(PATH), 1)
        cursor.addRow(kotlin.arrayOf(file.absolutePath))
        return cursor
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        val file = getFileForUri(p0)
        if (file.delete()) {
            val it = index.iterator()
            while (it.hasNext()) {
                if (it.next().time.toString() == file.name) {
                    it.remove()
                }
            }
            index.save()
            return 1
        }
        return 0
    }

    override fun openFile(uri: Uri, mode: String?): ParcelFileDescriptor {
        return ParcelFileDescriptor.open(getFileForUri(uri), ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(p0: Uri?): String {
        return "multipart/related"
    }

    private fun getFileForUri(uri: Uri): File {
        return File(directory, uri.lastPathSegment)
    }

    private fun getUriForFile(file: File): Uri {
        return URI.buildUpon().appendPath(file.canonicalFile.name).build()
    }

    private fun getUri(time: Long) = URI.buildUpon().appendPath("path").appendPath(time.toString()).build()
}