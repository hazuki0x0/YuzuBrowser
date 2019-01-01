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

package jp.hazuki.yuzubrowser.legacy.action

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import java.io.File

class ActionDatabase(context: Context, file: File) {

    private val mOpenHelper = SQLiteHelper(context, file.absolutePath)

    fun add(action: Action): Long {
        val jsonStr = action.toJsonString()
        if (TextUtils.isEmpty(jsonStr)) return -1

        val values = ContentValues().apply {
            put(COLUMN_ACTION, jsonStr)
        }
        return mOpenHelper.writableDatabase.insert(TABLE_NAME, null, values)
    }

    fun update(id: Long, action: Action): Boolean {
        val jsonStr = action.toJsonString()
        if (TextUtils.isEmpty(jsonStr)) return false

        val values = ContentValues().apply {
            put(COLUMN_ACTION, jsonStr)
        }
        mOpenHelper.writableDatabase
                .update(TABLE_NAME, values, COLUMN_ID + " = ?", arrayOf(id.toString()))
        return true
    }

    fun remove(id: Long) {
        mOpenHelper.writableDatabase
                .delete(TABLE_NAME, COLUMN_ID + " = ?", arrayOf(id.toString()))
    }

    operator fun get(id: Long): Action? {
        val db = mOpenHelper.readableDatabase
        db.query(TABLE_NAME, null, COLUMN_ID + " = ?", arrayOf(id.toString()), null, null, null).use {
            if (it.moveToFirst()) {
                val action = Action()
                if (action.fromJsonString(it.getString(COLUMN_ACTION_INDEX))) {
                    return action
                } else {
                    Logger.e(TAG, "action.fromJsonString failed")
                }
            }
        }
        return null
    }

    private class SQLiteHelper(context: Context, name: String) : SQLiteOpenHelper(context, name, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY" +
                    ", " + COLUMN_ACTION + " TEXT NOT NULL" +
                    ")")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
            onCreate(db)
        }
    }

    companion object {
        private const val DB_VERSION = 1

        private const val TAG = "ActionDatabase"
        private const val TABLE_NAME = "main_table1"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_ACTION = "action"

        private const val COLUMN_ID_INDEX = 0
        private const val COLUMN_ACTION_INDEX = 1
    }
}
