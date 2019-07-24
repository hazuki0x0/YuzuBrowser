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

import android.app.SearchManager
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.AbstractCursor
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.BaseColumns
import android.text.TextUtils
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import dagger.android.DaggerContentProvider
import jp.hazuki.yuzubrowser.BuildConfig
import jp.hazuki.yuzubrowser.ErrorReportServer
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.search.model.SearchSuggestModel
import jp.hazuki.yuzubrowser.search.model.suggest.ISuggest
import jp.hazuki.yuzubrowser.search.model.suggest.SuggestBing
import jp.hazuki.yuzubrowser.search.model.suggest.SuggestDuckDuckGo
import jp.hazuki.yuzubrowser.search.model.suggest.SuggestGoogle
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SuggestProvider : DaggerContentProvider() {

    private lateinit var mOpenHelper: DatabaseHelper
    private lateinit var mSuggestEngine: ISuggest
    @Inject
    lateinit var okHttpClient: OkHttpClient

    private var mSuggestType: Int = 0

    override fun onCreate(): Boolean {
        super.onCreate()
        val context = context ?: throw IllegalStateException()
        mOpenHelper = DatabaseHelper(context)
        mSuggestType = AppPrefs.search_suggest_engine.get()
        mSuggestEngine = getSuggestEngine(mSuggestType)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val type = sUriMatcher.match(uri)
        if (type == UriMatcher.NO_MATCH) {
            Logger.e(TAG, "UriMatcher.NO_MATCH")
            return null
        }

        var query = uri.getQueryParameter("q")
        if (query != null) query = query.trim { it <= ' ' }
        when (type) {
            TYPE_NET_ALL, TYPE_NET -> return queryNet(query)
            TYPE_LOCAL_ALL, TYPE_LOCAL -> return queryLocal(query)
            TYPE_NORMAL_ALL, TYPE_NORMAL -> return queryBoth(query)
        }
        return null
    }

    private fun queryBoth(query: String?): Cursor? {
        if (query.isNullOrEmpty()) {
            return queryLocal(query)
        }

        try {
            val net = getSuggests(query)
            if (net != null) {
                val dbQuery = query.replace("%", "$%").replace("_", "\$_")
                val suggestions = ArrayList<SearchSuggestModel.SuggestModel>()

                val db = mOpenHelper.readableDatabase
                val c = db.query(TABLE_NAME, null, SearchManager.SUGGEST_COLUMN_QUERY + " LIKE '%' || ? || '%' ESCAPE '$'", arrayOf(dbQuery), null, null, BaseColumns._ID + " DESC", "3")
                val colQuery = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY)
                while (c.moveToNext()) {
                    val suggestion = SearchSuggestModel.SuggestModel(c.getString(colQuery), true)

                    suggestions.add(suggestion)
                    net.remove(suggestion)
                }
                c.close()

                suggestions.addAll(net)

                for (prefix in yuzuPrefix) {
                    if (prefix.startsWith(query)) {
                        suggestions.add(SearchSuggestModel.SuggestModel(prefix))
                    }
                }

                return SuggestionsCursor(suggestions)
            }
        } catch (e: UnknownHostException) {
            return queryLocal(query)
        }

        return null
    }

    private fun queryNet(query: String?): Cursor? {
        if (query.isNullOrEmpty()) {
            return null
        }

        try {
            val list = getSuggests(query)
            if (list != null) {
                for (prefix in yuzuPrefix) {
                    if (prefix.startsWith(query)) {
                        list.add(SearchSuggestModel.SuggestModel(prefix))
                    }
                }
                return SuggestionsCursor(list)
            }
        } catch (e: UnknownHostException) {
            return queryLocal(query)
        }

        return null
    }

    @Throws(UnknownHostException::class)
    private fun getSuggests(query: String): MutableList<SearchSuggestModel.SuggestModel>? {
        if (AppPrefs.search_suggest_engine.get() != mSuggestType) {
            mSuggestType = AppPrefs.search_suggest_engine.get()
            mSuggestEngine = getSuggestEngine(mSuggestType)
        }

        val url = mSuggestEngine.getUrl(query)
        val client = okHttpClient.newBuilder()
                .connectTimeout(2000, TimeUnit.MILLISECONDS)
                .build()
        val request = Request.Builder()
                .url(url)
                .get()
                .build()

        val call = client.newCall(request)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body
                if (body != null) {
                    JsonReader.of(body.source()).use {
                        return mSuggestEngine.getSuggestions(it)
                    }
                }
            }
        } catch (e: Exception) {
            when (e) {
                is IOException, is JsonDataException -> {
                    ErrorReportServer.printAndWriteLog(e)
                }
                else -> throw e
            }
        }
        return null
    }

    private fun queryLocal(query: String?): Cursor {
        val db = mOpenHelper.readableDatabase
        return if (TextUtils.isEmpty(query))
            wrapCursor(db.query(TABLE_NAME, null, null, null, null, null, BaseColumns._ID + " DESC"))
        else {
            val dbQuery = query!!.replace("%", "$%").replace("_", "\$_")
            addYuzuPrefix(query, db.query(TABLE_NAME, null, SearchManager.SUGGEST_COLUMN_QUERY + " LIKE '%' || ? || '%' ESCAPE '$'", arrayOf(dbQuery), null, null, BaseColumns._ID + " DESC"))
        }
    }

    private fun addYuzuPrefix(query: String, c: Cursor?): Cursor {
        val suggestions = ArrayList<SearchSuggestModel.SuggestModel>()
        if (!TextUtils.isEmpty(query)) {
            for (prefix in yuzuPrefix) {
                if (prefix.startsWith(query)) {
                    suggestions.add(SearchSuggestModel.SuggestModel(prefix))
                }
            }
        }
        if (c != null) {
            val colQuery = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY)
            while (c.moveToNext()) {
                suggestions.add(SearchSuggestModel.SuggestModel(c.getString(colQuery), true))
            }
            c.close()
        }
        return SuggestionsCursor(suggestions)
    }

    private fun wrapCursor(c: Cursor?): Cursor {
        val suggestions = ArrayList<SearchSuggestModel.SuggestModel>()
        if (c != null) {
            val colQuery = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY)
            while (c.moveToNext()) {
                suggestions.add(SearchSuggestModel.SuggestModel(c.getString(colQuery), true))
            }
            c.close()
        }
        return SuggestionsCursor(suggestions)
    }

    private fun getSuggestEngine(type: Int): ISuggest {
        return when (type) {
            1 -> SuggestBing()
            2 -> SuggestDuckDuckGo()
            else -> SuggestGoogle()
        }
    }

    class SuggestionsCursor(private val mList: List<SearchSuggestModel.SuggestModel>) : AbstractCursor() {

        override fun getColumnNames(): Array<String> {
            return COLUMNS
        }

        override fun getCount(): Int {
            return mList.size
        }

        override fun getString(column: Int): String? {
            if (position == -1) return null
            when (column) {
                COL_ID -> return position.toString()
                //case COL_TEXT_1:
                COL_QUERY -> return mList[position].suggest
            }
            return null
        }

        override fun getLong(column: Int): Long {
            if (column == COL_ID) {
                return position.toLong()
            }
            throw UnsupportedOperationException()
        }

        override fun getDouble(column: Int): Double {
            throw UnsupportedOperationException()
        }

        override fun getFloat(column: Int): Float {
            throw UnsupportedOperationException()
        }

        override fun getInt(column: Int): Int {
            if (position == -1) return 0
            return if (column == COL_HISTORY) {
                if (mList[position].suggestHistory) 1 else 0
            } else {
                0
            }
        }

        override fun getShort(column: Int): Short {
            throw UnsupportedOperationException()
        }

        override fun isNull(column: Int): Boolean {
            throw UnsupportedOperationException()
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val type = sUriMatcher.match(uri)
        if (type == TYPE_NET || type == TYPE_NET_ALL)
            return 0
        val db = mOpenHelper.writableDatabase
        return db.delete(TABLE_NAME, selection, selectionArgs)
    }

    override fun getType(uri: Uri): String? {
        return SearchManager.SUGGEST_MIME_TYPE
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val type = sUriMatcher.match(uri)
        if (type == TYPE_NET || type == TYPE_NET_ALL)
            return null
        val db = mOpenHelper.writableDatabase
        db.insert(TABLE_NAME, null, values)
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException()
    }

    private class DatabaseHelper internal constructor(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.beginTransaction()
            try {
                db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY" +
                        ", " + SearchManager.SUGGEST_COLUMN_QUERY + " TEXT UNIQUE ON CONFLICT REPLACE" +
                        ")")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    companion object {
        private const val TAG = "GoogleSuggestProvider"
        private const val AUTHORITY = BuildConfig.APPLICATION_ID + ".search.SuggestProvider"
        val URI_NET: Uri = Uri.parse("content://$AUTHORITY/net")
        val URI_LOCAL: Uri = Uri.parse("content://$AUTHORITY/local")
        val URI_NORMAL: Uri = Uri.parse("content://$AUTHORITY/normal")
        val URI_NONE: Uri = Uri.EMPTY
        const val SUGGEST_HISTORY = "suggest_history"
        private const val TYPE_NET_ALL = 1
        private const val TYPE_LOCAL_ALL = 2
        private const val TYPE_NORMAL_ALL = 3
        private const val TYPE_NET = 4
        private const val TYPE_LOCAL = 5
        private const val TYPE_NORMAL = 6
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            sUriMatcher.addURI(AUTHORITY, "net", TYPE_NET)
            sUriMatcher.addURI(AUTHORITY, "local", TYPE_LOCAL)
            sUriMatcher.addURI(AUTHORITY, "normal", TYPE_NORMAL)
            sUriMatcher.addURI(AUTHORITY, "net/*", TYPE_NET_ALL)
            sUriMatcher.addURI(AUTHORITY, "local/*", TYPE_LOCAL_ALL)
            sUriMatcher.addURI(AUTHORITY, "normal/*", TYPE_NORMAL_ALL)
        }

        private val yuzuPrefix = arrayOf("yuzu:bookmarks", "yuzu:debug", "yuzu:downloads", "yuzu:history", "yuzu:home", "yuzu:readItLater", "yuzu:resBlock", "yuzu:settings", "yuzu:speeddial")

        private const val COL_ID = 0
        //private static final int COL_TEXT_1 = 1;
        //private static final int COL_TEXT_2 = 2;
        //private static final int COL_ICON_1 = 3;
        //private static final int COL_ICON_2 = 4;
        private const val COL_QUERY = 5
        private const val COL_HISTORY = 6

        private val COLUMNS = arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_ICON_2, SearchManager.SUGGEST_COLUMN_QUERY, SUGGEST_HISTORY)

        private const val DB_NAME = "searchsuggest.db"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "main_table1"
    }
}
