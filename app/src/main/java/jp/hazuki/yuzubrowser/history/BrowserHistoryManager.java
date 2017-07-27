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

package jp.hazuki.yuzubrowser.history;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.ImageUtils;
import jp.hazuki.yuzubrowser.utils.database.CursorLoadable;

public class BrowserHistoryManager implements CursorLoadable {
    private static final String DB_NAME = "webhistory1.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_NAME = "main_table1";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_FAVICON = "favicon";

    public static final int COLUMN_ID_INDEX = 0;
    public static final int COLUMN_URL_INDEX = 1;
    public static final int COLUMN_TITLE_INDEX = 2;
    public static final int COLUMN_TIME_INDEX = 3;
    public static final int COLUMN_FAVICON_INDEX = 4;

    private MyOpenHelper mOpenHelper;

    private static BrowserHistoryManager browserHistoryManager;

    public static BrowserHistoryManager getInstance(Context context) {
        if (browserHistoryManager == null)
            browserHistoryManager = new BrowserHistoryManager(context);
        return browserHistoryManager;
    }

    private BrowserHistoryManager(Context context) {
        mOpenHelper = new MyOpenHelper(context);
        int max_day = AppData.history_max_day.get();
        int max_count = AppData.history_max_count.get();
        if (max_day == 0 && max_count == 0)
            return;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (max_day != 0)
            db.delete(TABLE_NAME, COLUMN_TIME + " < " + (System.currentTimeMillis() - max_day * 24 * 60 * 60 * 1000), null);
        if (max_count != 0)
            db.execSQL("DELETE FROM " + TABLE_NAME +
                    " WHERE " + COLUMN_ID + " IN" +
                    " (SELECT " + COLUMN_ID + " FROM " + TABLE_NAME +
                    " ORDER BY " + COLUMN_TIME + " DESC" +
                    " LIMIT -1 OFFSET " + max_count + ")");
    }

    public void add(String url) {
        if (!checkUrl(url)) return;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_ID}, COLUMN_URL + " = ?", new String[]{url}, null, null, null, "1");
        if (c.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TIME, System.currentTimeMillis());
            db.update(TABLE_NAME, values, COLUMN_ID + " = " + c.getLong(0), null);
        } else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_URL, url);
            values.put(COLUMN_TIME, System.currentTimeMillis());
            db.insert(TABLE_NAME, null, values);
        }
        c.close();
    }

    public void update(String url, String title) {
        if (!checkUrl(url)) return;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        db.update(TABLE_NAME, values, COLUMN_URL + " = ?", new String[]{url});
    }

    public void update(String url, Bitmap favicon) {
        if (!checkUrl(url)) return;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVICON, ImageUtils.convertToByteArray(favicon));
        db.update(TABLE_NAME, values, COLUMN_URL + " = ?", new String[]{url});
    }

    public void deleteFavicon() {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(COLUMN_FAVICON);
        db.update(TABLE_NAME, values, COLUMN_FAVICON + " IS NOT NULL", null);
    }

    public void delete(String url) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_URL + " = ?", new String[]{url});
    }

    public void deleteWithSearch(String query) {
        query = query.replace("%", "$%").replace("_", "$_");
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_TITLE + " LIKE '%' || ? || '%' OR "
                        + COLUMN_URL + " LIKE '%' || ? || '%' ESCAPE '$'",
                new String[]{query, query});
    }

    public void deleteAll() {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public Bitmap getFavicon(String url) {
        Bitmap bitmap = null;
        byte[] icon = getFaviconImage(url);
        if (icon != null)
            bitmap = BitmapFactory.decodeByteArray(icon, 0, icon.length);
        return bitmap;
    }

    public byte[] getFaviconImage(String url) {
        if (url == null) return null;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_FAVICON}, COLUMN_URL + " = ?", new String[]{url}, null, null, null);
        byte[] icon = null;
        if (c.moveToFirst()) {
            icon = c.getBlob(c.getColumnIndex(COLUMN_FAVICON));
        }
        c.close();
        return icon;
    }

    public Bitmap getFavicon(long id) {
        Bitmap bitmap = null;
        byte[] icon = getFaviconImage(id);
        if (icon != null)
            bitmap = BitmapFactory.decodeByteArray(icon, 0, icon.length);
        return bitmap;
    }

    public byte[] getFaviconImage(long id) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_FAVICON}, COLUMN_ID + " = ?", new String[]{Long.toString(id)}, null, null, null);
        byte[] image = null;
        if (c.moveToFirst()) {
            image = c.getBlob(c.getColumnIndex(COLUMN_FAVICON));
        }
        c.close();
        return image;
    }

    public String[] getHistoryArray(int limit) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        ArrayList<String> histories = new ArrayList<>();
        Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_URL}, null, null, null, null, COLUMN_TIME + " DESC", Integer.toString(limit));
        if (c.moveToFirst()) {
            int urlIndex = c.getColumnIndex(COLUMN_URL);
            do {
                histories.add(c.getString(urlIndex));
            } while (c.moveToNext());
        }
        c.close();
        return histories.toArray(new String[histories.size()]);
    }

    public ArrayList<BrowserHistory> getList(int offset, int limit) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        ArrayList<BrowserHistory> histories = new ArrayList<>();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_TIME + " DESC", offset + ", " + limit);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                histories.add(new BrowserHistory(
                        c.getLong(COLUMN_ID_INDEX),
                        c.getString(COLUMN_TITLE_INDEX),
                        c.getString(COLUMN_URL_INDEX),
                        c.getLong(COLUMN_TIME_INDEX)));
            }
        }
        c.close();
        return histories;
    }

    public ArrayList<BrowserHistory> search(String query, int offset, int limit) {
        query = query.replace("%", "$%").replace("_", "$_");
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        ArrayList<BrowserHistory> histories = new ArrayList<>();
        Cursor c = db.query(TABLE_NAME, null,
                COLUMN_TITLE + " LIKE '%' || ? || '%' OR "
                        + COLUMN_URL + " LIKE '%' || ? || '%' ESCAPE '$'",
                new String[]{query, query}, null, null, COLUMN_TIME + " DESC", offset + ", " + limit);
        while (c.moveToNext()) {
            histories.add(new BrowserHistory(
                    c.getLong(COLUMN_ID_INDEX),
                    c.getString(COLUMN_TITLE_INDEX),
                    c.getString(COLUMN_URL_INDEX),
                    c.getLong(COLUMN_TIME_INDEX)));
        }
        c.close();
        return histories;
    }

    private static boolean checkUrl(String url) {
        return (!TextUtils.isEmpty(url) && !url.regionMatches(true, 0, "about:", 0, 6) && !url.regionMatches(true, 0, "yuzu:", 0, 5) && !url.regionMatches(true, 0, "data:", 0, 5));
    }

    @Override
    public Cursor getLoadableCursor() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, COLUMN_TIME + " DESC");
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
                        ", " + COLUMN_TIME + " INTEGER DEFAULT (datetime('now','localtime'))" +
                        ", " + COLUMN_FAVICON + " BLOB" +
                        ")");
                db.execSQL("CREATE UNIQUE INDEX url_index_1 ON " + TABLE_NAME + "(" + COLUMN_URL + ")");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                case 1:
                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_FAVICON + " BLOB");
                    break;
                default:
                    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                    onCreate(db);
                    break;
            }
        }
    }
}
