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

package jp.hazuki.yuzubrowser.favicon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

class FaviconCacheIndex {
    private static final String DB_NAME = "indexTable";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "hashTable";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_HASH = "hash";

    private static MyOpenHelper mOpenHelper;

    FaviconCacheIndex(Context context, String dir) {
        if (mOpenHelper == null) {
            String name = new File(context.getDir(dir, Context.MODE_PRIVATE), DB_NAME).getAbsolutePath();
            mOpenHelper = new MyOpenHelper(context, name);
        }
    }

    public void add(String url, long hash) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_HASH}, COLUMN_URL + " = ?", new String[]{url}, null, null, null, "1");
        if (c.moveToFirst()) {
            if (hash != c.getLong(1)) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_HASH, hash);
                db.update(TABLE_NAME, values, COLUMN_ID + " = " + c.getLong(0), null);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_URL, url);
            values.put(COLUMN_HASH, hash);
            db.insert(TABLE_NAME, null, values);
        }
        c.close();
    }

    public void remove(long hash) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (db.isReadOnly()) {
            db.close();
            db = mOpenHelper.getWritableDatabase();
        }
        db.delete(TABLE_NAME, COLUMN_HASH + " = ?", new String[]{Long.toString(hash)});
    }

    public Result get(String url) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_HASH}, COLUMN_URL + " = ?", new String[]{url}, null, null, null, "1");
        Result result;
        if (c.moveToFirst()) {
            result = new Result(true, c.getLong(0));
        } else {
            result = new Result(false, 0);
        }
        c.close();
        return result;
    }

    public void close() {
        mOpenHelper.close();
    }

    static class Result {
        final boolean exists;
        final long hash;

        Result(boolean exists, long hash) {
            this.exists = exists;
            this.hash = hash;
        }
    }

    private static final class MyOpenHelper extends SQLiteOpenHelper {
        MyOpenHelper(Context context, String name) {
            super(context, name, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY" +
                        ", " + COLUMN_URL + " TEXT NOT NULL UNIQUE" +
                        ", " + COLUMN_HASH + " INTEGER" +
                        ")");
                db.execSQL("CREATE UNIQUE INDEX url_index ON " + TABLE_NAME + "(" + COLUMN_URL + ")");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                default:
                    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                    onCreate(db);
                    break;
            }
        }
    }
}
