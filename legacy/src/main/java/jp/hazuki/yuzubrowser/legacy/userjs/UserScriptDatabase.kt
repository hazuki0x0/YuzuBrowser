/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.userjs

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class UserScriptDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    val allList: ArrayList<UserScript>
        get() {
            val db = readableDatabase
            val list = ArrayList<UserScript>()
            var offset = 0
            do {
                val c = db.query(TABLE_NAME, null, null, null, null, null, null, offset.toString() + ", 10")
                if (c.moveToFirst()) {
                    do {
                        val data = UserScript(c.getLong(COLUMN_ID_INDEX), c.getString(COLUMN_DATA_INDEX), c.getInt(COLUMN_ENABLED_INDEX) != 0)
                        list.add(data)
                    } while (c.moveToNext())
                }
                c.close()

                offset += 10
            } while (list.size == offset)

            return list
        }

    val enableJsDataList: ArrayList<UserScript>
        get() {
            val db = readableDatabase
            val list = ArrayList<UserScript>()
            var offset = 0
            do {
                val c = db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_DATA), COLUMN_ENABLED + " <> 0", null, null, null, null, offset.toString() + ", 10")
                if (c.moveToFirst()) {
                    do {
                        val data = UserScript(c.getLong(0), c.getString(1), true)
                        list.add(data)
                    } while (c.moveToNext())
                }
                c.close()

                offset += 10
            } while (list.size == offset)

            return list
        }

    override fun onCreate(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY" +
                    ", " + COLUMN_DATA + " TEXT NOT NULL" +
                    ", " + COLUMN_ENABLED + " INTEGER DEFAULT 1" +
                    ")")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when (oldVersion) {
            1 -> {
                db.execSQL("ALTER TABLE main_table1 RENAME TO temp_table1")
                onCreate(db)
                db.execSQL("INSERT INTO $TABLE_NAME($COLUMN_DATA, $COLUMN_ENABLED) SELECT jsdata, enabled FROM temp_table1")
                db.execSQL("DROP TABLE temp_table1")
            }
            else -> {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
                onCreate(db)
            }
        }
    }

    fun add(js: UserScript) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_DATA, js.data)
        values.put(COLUMN_ENABLED, 1)
        js.id = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun add(js: UserScriptInfo) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_DATA, js.data)
        values.put(COLUMN_ENABLED, 1)
        js.id = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun update(js: UserScript) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ID, js.id)
        values.put(COLUMN_DATA, js.data)
        values.put(COLUMN_ENABLED, js.isEnabled)
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun update(js: UserScriptInfo) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ID, js.id)
        values.put(COLUMN_DATA, js.data)
        values.put(COLUMN_ENABLED, js.isEnabled)
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    operator fun get(id: Long): UserScriptInfo? {
        val db = readableDatabase
        db.query(TABLE_NAME, null, COLUMN_ID + " = " + id, null, null, null, null).use { c ->
            if (c.moveToFirst()) {
                return UserScriptInfo(c.getLong(COLUMN_ID_INDEX), c.getString(COLUMN_DATA_INDEX), c.getInt(COLUMN_ENABLED_INDEX) != 0)
            }
        }
        return null
    }

    fun set(js: UserScriptInfo) {
        if (js.id >= 0) {
            update(js)
        } else {
            add(js)
        }
    }

    fun addAll(list: Collection<UserScript>) {
        val db = writableDatabase
        db.beginTransaction()
        for (js in list) {
            val values = ContentValues()
            values.put(COLUMN_DATA, js.data)
            values.put(COLUMN_ENABLED, js.isEnabled)
            js.id = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun delete(js: UserScript) {
        val db = writableDatabase
        db.delete(TABLE_NAME, COLUMN_ID + " = " + js.id, null)
    }

    fun deleteAll() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
    }

    fun move(positionFrom: Int, positionTo: Int) {
        val list = allList
        val item = list.removeAt(positionFrom)
        list.add(positionTo, item)
        deleteAll()
        addAll(list)
    }

    fun saveAll(list: List<UserScript>) {
        deleteAll()
        addAll(list)
    }

    companion object {
        private const val DB_NAME = "userjs1.db"
        private const val DB_VERSION = 2
        private const val TABLE_NAME = "main_table1"

        const val COLUMN_ID = "_id"
        const val COLUMN_DATA = "data"
        const val COLUMN_ENABLED = "enabled"

        const val COLUMN_ID_INDEX = 0
        const val COLUMN_DATA_INDEX = 1
        const val COLUMN_ENABLED_INDEX = 2

        var instance: UserScriptDatabase? = null

        fun getInstance(context: Context): UserScriptDatabase {
            if (instance == null) {
                instance = UserScriptDatabase(context)
            }
            return instance!!
        }
    }
}
