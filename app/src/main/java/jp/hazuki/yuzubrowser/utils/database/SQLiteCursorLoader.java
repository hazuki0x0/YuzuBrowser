package jp.hazuki.yuzubrowser.utils.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteCursorLoader extends CursorLoaderBase {
    private final SQLiteOpenHelper mHelper;
    private final String table;
    private final String[] columns;
    private final String selection;
    private final String[] selectionArgs;
    private final String groupBy;
    private final String having;
    private final String orderBy;

    public SQLiteCursorLoader(Context context, SQLiteOpenHelper helper, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        super(context);
        mHelper = helper;
        this.table = table;
        this.columns = columns;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.groupBy = groupBy;
        this.having = having;
        this.orderBy = orderBy;
    }

    @Override
    public Cursor loadInBackground() {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

}
