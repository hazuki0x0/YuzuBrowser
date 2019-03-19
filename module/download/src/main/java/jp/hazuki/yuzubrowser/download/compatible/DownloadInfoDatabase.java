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

package jp.hazuki.yuzubrowser.download.compatible;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DownloadInfoDatabase extends SQLiteOpenHelper {
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

    public DownloadInfoDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY" +
                ", " + COLUMN_URL + " TEXT NOT NULL" +
                ", " + COLUMN_FILEPATH + " TEXT NOT NULL" +
                ", " + COLUMN_START_TIME + " INTEGER DEFAULT (datetime('now','localtime'))" +
                ", " + COLUMN_STATE + " INTEGER DEFAULT 0" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public List<ConvertDownloadInfo> getConvertData() {
        List<ConvertDownloadInfo> infoList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_START_TIME + " DESC")) {
            while (c.moveToNext()) {
                infoList.add(new ConvertDownloadInfo(c.getString(COLUMN_URL_INDEX),
                        c.getString(COLUMN_FILEPATH_INDEX), c.getLong(COLUMN_START_TIME_INDEX), c.getInt(COLUMN_STATE_INDEX)));
            }
        }
        db.close();
        return infoList;
    }

    public void deleteDatabase(Context context) {
        context.deleteDatabase(DB_NAME);
    }
}
