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

package jp.hazuki.yuzubrowser.favicon

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import java.io.File

internal class FaviconCacheIndex(context: Context, dir: String) {
    private val mOpenHelper: MyOpenHelper

    init {
        val name = File(context.getDir(dir, Context.MODE_PRIVATE), DB_NAME).absolutePath
        mOpenHelper = MyOpenHelper.getInstance(context, name)
    }

    fun add(url: String, hash: Long) {
        val db = mOpenHelper.writableDatabase
        db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_HASH), "$COLUMN_URL = ?", arrayOf(url), null, null, null, "1").use { c ->
            if (c.moveToFirst()) {
                if (hash != c.getLong(1)) {
                    val values = ContentValues()
                    values.put(COLUMN_HASH, hash)
                    db.update(TABLE_NAME, values, COLUMN_ID + " = " + c.getLong(0), null)
                }
            } else {
                val values = ContentValues()
                values.put(COLUMN_URL, url)
                values.put(COLUMN_HASH, hash)
                db.insert(TABLE_NAME, null, values)
            }
            return
        }
    }

    fun remove(hash: Long) {
        val db = mOpenHelper.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_HASH = ?", arrayOf(java.lang.Long.toString(hash)))
    }

    operator fun get(url: String): Result {
        val db = mOpenHelper.readableDatabase
        return db.query(TABLE_NAME, arrayOf(COLUMN_HASH), "$COLUMN_URL = ?", arrayOf(url), null, null, null, "1").use { c ->
            if (c.moveToFirst()) {
                Result(true, c.getLong(0))
            } else {
                Result(false, 0)
            }
        }
    }

    fun clear() {
        val db = mOpenHelper.writableDatabase
        db.delete(TABLE_NAME, null, null)
    }

    fun close() {
        mOpenHelper.close()
    }

    internal class Result(val exists: Boolean, val hash: Long)

    private class MyOpenHelper internal constructor(context: Context, name: String) : SQLiteOpenHelper(context, name, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.beginTransaction()
            try {
                db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY" +
                        ", " + COLUMN_URL + " TEXT NOT NULL UNIQUE" +
                        ", " + COLUMN_HASH + " INTEGER" +
                        ")")
                db.execSQL("CREATE UNIQUE INDEX url_index ON $TABLE_NAME($COLUMN_URL)")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }

        companion object {
            private var mOpenHelper: MyOpenHelper? = null

            fun getInstance(context: Context, name: String): MyOpenHelper {
                if (mOpenHelper == null) {
                    mOpenHelper = MyOpenHelper(context, name)
                }
                return mOpenHelper!!
            }
        }
    }

    companion object {
        private const val DB_NAME = "indexTable"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "hashTable"

        private const val COLUMN_ID = "_id"
        private const val COLUMN_URL = "url"
        private const val COLUMN_HASH = "hash"
    }
}
