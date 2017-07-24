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

package jp.hazuki.yuzubrowser.download;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import jp.hazuki.yuzubrowser.utils.database.CursorLoadable;

public class DownloadInfoDatabase extends SQLiteOpenHelper implements CursorLoadable {
    private static final String DB_NAME = "downloadinfolist1.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "main_table1";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_FILEPATH = "filepath";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_STATE = "state";

    public static final int COLUMN_ID_INDEX = 0;
    public static final int COLUMN_URL_INDEX = 1;
    public static final int COLUMN_FILEPATH_INDEX = 2;
    public static final int COLUMN_START_TIME_INDEX = 3;
    public static final int COLUMN_STATE_INDEX = 4;

    private static DownloadInfoDatabase instance;

    public static DownloadInfoDatabase getInstance(Context context) {
        if (instance == null)
            instance = new DownloadInfoDatabase(context);
        return instance;
    }

    private DownloadInfoDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY" +
                ", " + COLUMN_URL + " TEXT NOT NULL" +
                ", " + COLUMN_FILEPATH + " TEXT NOT NULL" +
                ", " + COLUMN_START_TIME + " INTEGER DEFAULT (datetime('now','localtime'))" +
                ", " + COLUMN_STATE + " INTEGER DEFAULT " + DownloadInfo.STATE_DOWNLOADING +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insert(DownloadInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        long id = info.getId();
        if (id >= 0)
            values.put(COLUMN_ID, id);
        values.put(COLUMN_URL, info.getUrl());
        values.put(COLUMN_FILEPATH, info.getFile().getAbsolutePath());
        values.put(COLUMN_START_TIME, info.getStartTime());
        values.put(COLUMN_STATE, info.getState());
        id = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        info.setId(id);
        //db.close();
        return id;
    }

    public void updateState(DownloadInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATE, info.getState());
        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(info.getId())});
        //db.close();
    }

    public void delete(long id) {
        if (id < 0)
            throw new IllegalArgumentException("id must be greater than or equal to 0");
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        //db.close();
    }

    public void deleteAllHistory() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_STATE + " <> " + DownloadInfo.STATE_DOWNLOADING, null);
        //db.close();
    }

    public void fixData() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATE, DownloadInfo.STATE_UNKNOWN_ERROR);
        db.update(TABLE_NAME, values, COLUMN_STATE + " = ?", new String[]{String.valueOf(DownloadInfo.STATE_DOWNLOADING)});
        //db.close();
    }

    @Override
    public Cursor getLoadableCursor() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, COLUMN_START_TIME + " DESC");
    }
}
