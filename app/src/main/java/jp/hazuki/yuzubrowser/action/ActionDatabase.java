package jp.hazuki.yuzubrowser.action;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.io.File;

import jp.hazuki.yuzubrowser.utils.Logger;

public class ActionDatabase {
    private static final String TAG = "ActionDatabase";
    private static final String TABLE_NAME = "main_table1";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ACTION = "action";

    public static final int COLUMN_ID_INDEX = 0;
    public static final int COLUMN_ACTION_INDEX = 1;

    private final SQLiteHelper mOpenHelper;

    public ActionDatabase(Context context, String name) {
        mOpenHelper = new SQLiteHelper(context, name);
    }

    public ActionDatabase(Context context, File file) {
        mOpenHelper = new SQLiteHelper(context, file.getAbsolutePath());//before 2.1, this throws exception
    }

    public long add(Action action) {
        String jsonstr = action.toJsonString();
        if (TextUtils.isEmpty(jsonstr))
            return -1;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ACTION, jsonstr);
        return db.insert(TABLE_NAME, null, values);
    }

    public boolean update(long id, Action action) {
        String jsonstr = action.toJsonString();
        if (TextUtils.isEmpty(jsonstr))
            return false;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ACTION, jsonstr);
        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        //db.close();
        return true;
    }

    public void remove(long id) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        //db.close();
    }

    public Action get(long id) {
        Action action = null;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst()) {
            action = new Action();
            if (!action.fromJsonString(cursor.getString(COLUMN_ACTION_INDEX))) {
                Logger.e(TAG, "action.fromJsonString failed");
                action = null;
            }
        }
        cursor.close();
        //db.close();
        return action;
    }

    private static final class SQLiteHelper extends SQLiteOpenHelper {
        private static final int DB_VERSION = 1;

        public SQLiteHelper(Context context, String name) {
            super(context, name, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY" +
                    ", " + COLUMN_ACTION + " TEXT NOT NULL" +
                    ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
