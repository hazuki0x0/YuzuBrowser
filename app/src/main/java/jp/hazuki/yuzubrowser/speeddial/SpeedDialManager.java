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

package jp.hazuki.yuzubrowser.speeddial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class SpeedDialManager {
    public static final String DB_NAME = "speeddial1.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_NAME = "main_table";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_ORDER = "item_order";
    private static final String COLUMN_ICON = "icon";
    private static final String COLUMN_FAVICON = "favicon";
    private static final String COLUMN_LAST_UPDATE = "last_update";

    public static final int COLUMN_ID_INDEX = 0;
    public static final int COLUMN_URL_INDEX = 1;
    public static final int COLUMN_TITLE_INDEX = 2;
    public static final int COLUMN_ORDER_INDEX = 3;
    public static final int COLUMN_ICON_INDEX = 4;
    public static final int COLUMN_FAVICON_INDEX = 5;
    private static final int COLUMN_LAST_UPDATE_INDEX = 6;

    private MyOpenHelper mOpenHelper;

    private static SpeedDialManager speedDialManager;

    public static SpeedDialManager getInstance(Context context) {
        if (speedDialManager == null)
            speedDialManager = new SpeedDialManager(context);
        return speedDialManager;
    }

    private SpeedDialManager(Context context) {
        mOpenHelper = new MyOpenHelper(context);
    }

    public void update(SpeedDial speedDial) {
        if (speedDial.getId() >= 0) {
            _update(speedDial);
        } else {
            _add(speedDial);
        }
    }

    private synchronized void _add(SpeedDial speedDial) {
        if (!checkUrl(speedDial.getUrl())) return;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_URL, speedDial.getUrl());
        values.put(COLUMN_TITLE, speedDial.getTitle());
        values.put(COLUMN_ORDER, getNowId() + 1);
        values.put(COLUMN_ICON, speedDial.getIcon().getIconBytes());
        values.put(COLUMN_FAVICON, speedDial.isFavicon());
        values.put(COLUMN_LAST_UPDATE, 0);
        long id = db.insert(TABLE_NAME, null, values);
        speedDial.setId((int) id);
    }

    private synchronized void _update(SpeedDial speedDial) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_URL, speedDial.getUrl());
        values.put(COLUMN_TITLE, speedDial.getTitle());
        values.put(COLUMN_ICON, speedDial.getIcon().getIconBytes());
        values.put(COLUMN_FAVICON, speedDial.isFavicon());
        values.put(COLUMN_LAST_UPDATE, 0);
        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{Integer.toString(speedDial.getId())});
    }

    public synchronized void update(String url, Bitmap icon) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long time = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        Cursor c = db.query(TABLE_NAME, null,
                COLUMN_FAVICON + " = 1 AND " +
                        COLUMN_URL + " = ? AND " +
                        COLUMN_LAST_UPDATE + " <= ?", new String[]{url, Long.toString(time)}, null, null, null);
        if (c.moveToFirst()) {
            do {
                ContentValues values = new ContentValues();
                values.put(COLUMN_ICON, WebIcon.createIcon(icon).getIconBytes());
                db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{Integer.toString(c.getInt(COLUMN_ID_INDEX))});
            } while (c.moveToNext());
        }
        c.close();
    }


    public synchronized SpeedDial get(int id) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{Long.toString(id)}, null, null, null);
        SpeedDial speedDial = null;
        if (c.moveToFirst()) {
            speedDial = new SpeedDial(c.getInt(COLUMN_ID_INDEX),
                    c.getString(COLUMN_URL_INDEX),
                    c.getString(COLUMN_TITLE_INDEX),
                    new WebIcon(c.getBlob(COLUMN_ICON_INDEX)),
                    c.getInt(COLUMN_FAVICON_INDEX) == 1);
        }
        c.close();
        return speedDial;
    }

    public synchronized ArrayList<SpeedDial> getAll() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_ORDER + " asc");
        ArrayList<SpeedDial> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(new SpeedDial(c.getInt(COLUMN_ID_INDEX),
                    c.getString(COLUMN_URL_INDEX),
                    c.getString(COLUMN_TITLE_INDEX),
                    new WebIcon(c.getBlob(COLUMN_ICON_INDEX)),
                    c.getInt(COLUMN_FAVICON_INDEX) == 1));
        }
        c.close();
        return list;
    }

    public synchronized void updateOrder(List<SpeedDial> speedDials) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        SpeedDial data;
        ContentValues values;
        for (int i = 0; speedDials.size() > i; i++) {
            data = speedDials.get(i);
            values = new ContentValues();
            values.put(COLUMN_ORDER, i);
            db.update(TABLE_NAME, values, COLUMN_ID + "=" + data.getId(), null);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    public synchronized void delete(int id) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{Integer.toString(id)});
    }

    private static boolean checkUrl(String url) {
        return (!TextUtils.isEmpty(url) && !url.regionMatches(true, 0, "about:", 0, 6));
    }

    public synchronized int getNowId() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int id = -1;
        Cursor c = db.rawQuery("SELECT max(" + COLUMN_ID + ") FROM " + TABLE_NAME, null);
        if (c.moveToFirst()) {
            id = c.getInt(0);
        }
        c.close();
        return id;
    }

    private static final class MyOpenHelper extends SQLiteOpenHelper {
        public MyOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY" +
                        ", " + COLUMN_URL + " TEXT NOT NULL" +
                        ", " + COLUMN_TITLE + " TEXT" +
                        ", " + COLUMN_ORDER + " INTEGER" +
                        ", " + COLUMN_ICON + " BLOB" +
                        ", " + COLUMN_FAVICON + " INTEGER DEFAULT 0" +
                        ", " + COLUMN_LAST_UPDATE + " INTEGER DEFAULT 0" +
                        ")");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                case 1:
                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LAST_UPDATE + " INTEGER DEFAULT 0");
                    break;
                default:
                    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                    onCreate(db);
                    break;
            }
        }
    }
}
