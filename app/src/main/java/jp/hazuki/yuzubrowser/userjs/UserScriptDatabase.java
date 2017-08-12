package jp.hazuki.yuzubrowser.userjs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collection;

public class UserScriptDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "userjs1.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_NAME = "main_table1";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_ENABLED = "enabled";

    public static final int COLUMN_ID_INDEX = 0;
    public static final int COLUMN_DATA_INDEX = 1;
    public static final int COLUMN_ENABLED_INDEX = 2;

    public UserScriptDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY" +
                    ", " + COLUMN_DATA + " TEXT NOT NULL" +
                    ", " + COLUMN_ENABLED + " INTEGER DEFAULT 1" +
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
                db.execSQL("ALTER TABLE main_table1 RENAME TO temp_table1");
                onCreate(db);
                db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_DATA + ", " + COLUMN_ENABLED + ") SELECT jsdata, enabled FROM temp_table1");
                db.execSQL("DROP TABLE temp_table1");
                break;
            default:
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
                break;
        }
    }

    public void add(UserScript js) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATA, js.getData());
        values.put(COLUMN_ENABLED, 1);
        js.setId(db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE));
    }

    public void add(UserScriptInfo js) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATA, js.getData());
        values.put(COLUMN_ENABLED, 1);
        js.setId(db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE));
    }

    public void update(UserScript js) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, js.getId());
        values.put(COLUMN_DATA, js.getData());
        values.put(COLUMN_ENABLED, js.isEnabled());
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void update(UserScriptInfo js) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, js.getId());
        values.put(COLUMN_DATA, js.getData());
        values.put(COLUMN_ENABLED, js.isEnabled());
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void addAll(Collection<UserScript> list) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        for (UserScript js : list) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DATA, js.getData());
            values.put(COLUMN_ENABLED, js.isEnabled());
            js.setId(db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE));
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void delete(UserScript js) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = " + js.getId(), null);
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public ArrayList<UserScript> getAllList() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<UserScript> list = new ArrayList<>();
        int offset = 0;
        do {
            Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null, offset + ", 10");
            if (c.moveToFirst()) {
                do {
                    UserScript data = new UserScript(c.getLong(COLUMN_ID_INDEX), c.getString(COLUMN_DATA_INDEX), c.getInt(COLUMN_ENABLED_INDEX) != 0);
                    list.add(data);
                } while (c.moveToNext());
            }
            c.close();

            offset += 10;
        } while (list.size() == offset);

        return list;
    }

    public ArrayList<UserScript> getEnableJsDataList() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<UserScript> list = new ArrayList<>();
        int offset = 0;
        do {
            Cursor c = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_DATA}, COLUMN_ENABLED + " <> 0", null, null, null, null, offset + ", 10");
            if (c.moveToFirst()) {
                do {
                    UserScript data = new UserScript(c.getLong(0), c.getString(1), true);
                    list.add(data);
                } while (c.moveToNext());
            }
            c.close();

            offset += 10;
        } while (list.size() == offset);

        return list;
    }

    public void move(int positionFrom, int positionTo) {
        ArrayList<UserScript> list = getAllList();
        UserScript item = list.remove(positionFrom);
        list.add(positionTo, item);
        deleteAll();
        addAll(list);
    }
}
