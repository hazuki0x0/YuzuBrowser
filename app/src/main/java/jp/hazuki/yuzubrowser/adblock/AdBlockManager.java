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

package jp.hazuki.yuzubrowser.adblock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.utils.fastmatch.FastMatcher;
import jp.hazuki.yuzubrowser.utils.fastmatch.FastMatcherCache;
import jp.hazuki.yuzubrowser.utils.fastmatch.FastMatcherList;
import jp.hazuki.yuzubrowser.utils.fastmatch.ItemDecoder;

public class AdBlockManager {
    private static final String DB_NAME = "adblock.db";
    private static final int DB_VERSION = 1;

    static final String BLACK_TABLE_NAME = "black";
    static final String WHITE_TABLE_NAME = "white";
    static final String WHITE_PAGE_TABLE_NAME = "white_page";

    public static final int TYPE_BLACK_TABLE = 1;
    public static final int TYPE_WHITE_TABLE = 2;
    public static final int TYPE_WHITE_PAGE_TABLE = 3;

    private static final String INFO_TABLE_NAME = "info";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_MATCH = "match";
    private static final String COLUMN_ENABLE = "enable";
    private static final String COLUMN_COUNT = "count";
    private static final String COLUMN_TIME = "time";

    private static final String INFO_COLUMN_NAME = "name";
    private static final String INFO_COLUMN_LAST_TIME = "time";

    private MyOpenHelper mOpenHelper;
    private Context appContext;

    AdBlockManager(Context context) {
        mOpenHelper = MyOpenHelper.getInstance(context);
        appContext = context.getApplicationContext();
    }

    private void update(String table, AdBlock adBlock) {
        if (adBlock.getId() > -1) {
            _update(table, adBlock);
        } else {
            _add(table, adBlock);
        }
        updateListTime(table);
    }

    private void _add(String table, AdBlock adBlock) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MATCH, adBlock.getMatch());
        values.put(COLUMN_ENABLE, adBlock.isEnable() ? 1 : 0);
        values.put(COLUMN_COUNT, adBlock.getCount());
        values.put(COLUMN_TIME, adBlock.getTime());
        long id = db.insert(table, null, values);
        adBlock.setId((int) id);
    }

    private void _update(String table, AdBlock adBlock) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MATCH, adBlock.getMatch());
        values.put(COLUMN_ENABLE, adBlock.isEnable() ? 1 : 0);
        values.put(COLUMN_COUNT, adBlock.getCount());
        values.put(COLUMN_TIME, adBlock.getTime());
        db.update(table, values, COLUMN_ID + " = ?", new String[]{Integer.toString(adBlock.getId())});
    }

    private void delete(String table, int id) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(table, COLUMN_ID + " = ?", new String[]{Integer.toString(id)});
        updateListTime(table);
    }

    private void addAll(String table, List<AdBlock> adBlocks) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (AdBlock adBlock : adBlocks) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_MATCH, adBlock.getMatch());
                values.put(COLUMN_ENABLE, adBlock.isEnable() ? 1 : 0);
                db.insert(table, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        updateListTime(table);
    }

    private ArrayList<AdBlock> getAllItems(String table) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(table, null, null, null, null, null, COLUMN_COUNT + " DESC");
        int id = c.getColumnIndex(COLUMN_ID);
        int match = c.getColumnIndex(COLUMN_MATCH);
        int enable = c.getColumnIndex(COLUMN_ENABLE);
        int count = c.getColumnIndex(COLUMN_COUNT);
        int time = c.getColumnIndex(COLUMN_TIME);
        ArrayList<AdBlock> adBlocks = new ArrayList<>();
        while (c.moveToNext())
            adBlocks.add(new AdBlock(c.getInt(id), c.getString(match), c.getInt(enable) != 0, c.getInt(count), c.getLong(time)));
        c.close();
        return adBlocks;
    }

    void updateOrder(String table, FastMatcherList list) {
        if (list == null) return;
        list.sort();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (FastMatcher matcher : list.getMatcherList()) {
                if (matcher.isUpdate()) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_COUNT, matcher.getFrequency());
                    values.put(COLUMN_TIME, matcher.getTime());
                    db.update(table, values, COLUMN_ID + "=" + matcher.getId(), null);
                    matcher.saved();
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        list.setDbTime(System.currentTimeMillis());
        FastMatcherCache.save(appContext, table, list);
    }

    FastMatcherList getFastMatcherCachedList(String table) {
        if (getListUpdateTime(table) > FastMatcherCache.getLastTime(appContext, table))
            return getFastMatcherList(table);
        else
            return FastMatcherCache.getMatcher(appContext, table);
    }

    private FastMatcherList getFastMatcherList(String table) {
        FastMatcherList list = new FastMatcherList();
        list.setDbTime(getListUpdateTime(table));
        ArrayList<FastMatcher> matcherList = list.getMatcherList();
        ItemDecoder decoder = new ItemDecoder();
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(table, null, COLUMN_ENABLE + " = 1", null, null, null, COLUMN_COUNT + " DESC");
        int id = c.getColumnIndex(COLUMN_ID);
        int match = c.getColumnIndex(COLUMN_MATCH);
        int count = c.getColumnIndex(COLUMN_COUNT);
        int time = c.getColumnIndex(COLUMN_TIME);
        while (c.moveToNext()) {
            FastMatcher matcher = decoder.singleDecode(c.getString(match), c.getInt(id), c.getInt(count), c.getLong(time));
            if (matcher != null)
                matcherList.add(matcher);
        }
        c.close();
        return list;
    }

    private long getListUpdateTime(String table) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(INFO_TABLE_NAME, null, INFO_COLUMN_NAME + " = ?", new String[]{table}, null, null, null, "1");
        long time = -1;
        if (c.moveToFirst())
            time = c.getLong(c.getColumnIndex(INFO_COLUMN_LAST_TIME));
        c.close();
        return time;
    }

    private void updateListTime(String table) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(INFO_COLUMN_LAST_TIME, System.currentTimeMillis());
        db.update(INFO_TABLE_NAME, values, INFO_COLUMN_NAME + " = ?", new String[]{table});
    }

    public static AdBlockItemProvider getProvider(Context context, int type) {
        switch (type) {
            case TYPE_BLACK_TABLE:
                return new AdBlockItemProvider(context, BLACK_TABLE_NAME);
            case TYPE_WHITE_TABLE:
                return new AdBlockItemProvider(context, WHITE_TABLE_NAME);
            case TYPE_WHITE_PAGE_TABLE:
                return new AdBlockItemProvider(context, WHITE_PAGE_TABLE_NAME);
            default:
                throw new IllegalArgumentException("unknown type");
        }
    }

    public static final class AdBlockItemProvider {

        private final AdBlockManager manager;
        private final String table;

        private AdBlockItemProvider(Context context, String table) {
            manager = new AdBlockManager(context);
            this.table = table;
        }

        public void update(AdBlock adBlock) {
            manager.update(table, adBlock);
        }

        public void delete(int id) {
            manager.delete(table, id);
        }

        public ArrayList<AdBlock> getAllItems() {
            return manager.getAllItems(table);
        }

        public void addAll(List<AdBlock> adBlocks) {
            manager.addAll(table, adBlocks);
        }
    }

    private static final class MyOpenHelper extends SQLiteOpenHelper {
        private static MyOpenHelper instance;

        public static synchronized MyOpenHelper getInstance(Context context) {
            if (instance == null)
                instance = new MyOpenHelper(context);
            return instance;
        }

        MyOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL("CREATE TABLE " + INFO_TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY" +
                        ", " + INFO_COLUMN_NAME + " TEXT NOT NULL" +
                        ", " + INFO_COLUMN_LAST_TIME + " INTEGER DEFAULT 0" +
                        ")");
                db.execSQL("CREATE TABLE " + BLACK_TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY" +
                        ", " + COLUMN_MATCH + " TEXT NOT NULL UNIQUE" +
                        ", " + COLUMN_ENABLE + " INTEGER DEFAULT 0" +
                        ", " + COLUMN_COUNT + " INTEGER DEFAULT 0" +
                        ", " + COLUMN_TIME + " INTEGER DEFAULT 0" +
                        ")");
                db.execSQL("CREATE TABLE " + WHITE_TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY" +
                        ", " + COLUMN_MATCH + " TEXT NOT NULL UNIQUE" +
                        ", " + COLUMN_ENABLE + " INTEGER DEFAULT 0" +
                        ", " + COLUMN_COUNT + " INTEGER DEFAULT 0" +
                        ", " + COLUMN_TIME + " INTEGER DEFAULT 0" +
                        ")");
                db.execSQL("CREATE TABLE " + WHITE_PAGE_TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY" +
                        ", " + COLUMN_MATCH + " TEXT NOT NULL UNIQUE" +
                        ", " + COLUMN_ENABLE + " INTEGER DEFAULT 0" +
                        ", " + COLUMN_COUNT + " INTEGER DEFAULT 0" +
                        ", " + COLUMN_TIME + " INTEGER DEFAULT 0" +
                        ")");

                // init info table
                ContentValues values = new ContentValues();
                values.put(INFO_COLUMN_LAST_TIME, System.currentTimeMillis());

                values.put(INFO_COLUMN_NAME, BLACK_TABLE_NAME);
                db.insert(INFO_TABLE_NAME, null, values);

                values.put(INFO_COLUMN_NAME, WHITE_TABLE_NAME);
                db.insert(INFO_TABLE_NAME, null, values);

                values.put(INFO_COLUMN_NAME, WHITE_PAGE_TABLE_NAME);
                db.insert(INFO_TABLE_NAME, null, values);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                default:
                    db.execSQL("DROP TABLE IF EXISTS " + BLACK_TABLE_NAME);
                    db.execSQL("DROP TABLE IF EXISTS " + WHITE_TABLE_NAME);
                    db.execSQL("DROP TABLE IF EXISTS " + WHITE_PAGE_TABLE_NAME);
                    db.execSQL("DROP TABLE IF EXISTS " + INFO_TABLE_NAME);
                    onCreate(db);
                    break;
            }
        }
    }
}
