package jp.hazuki.yuzubrowser.search;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.Logger;

public class SuggestProvider extends ContentProvider {
    private static final String TAG = "GoogleSuggestProvider";
    private static final String AUTHORITY = "jp.hazuki.yuzubrowser.search.SuggestProvider";
    public static final Uri URI_NET = Uri.parse("content://" + AUTHORITY + "/net");
    public static final Uri URI_LOCAL = Uri.parse("content://" + AUTHORITY + "/local");
    public static final Uri URI_NORMAL = Uri.parse("content://" + AUTHORITY + "/normal");
    private static final int TYPE_NET_ALL = 1;
    private static final int TYPE_LOCAL_ALL = 2;
    private static final int TYPE_NORMAL_ALL = 3;
    private static final int TYPE_NET = 4;
    private static final int TYPE_LOCAL = 5;
    private static final int TYPE_NORMAL = 6;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "net", TYPE_NET);
        sUriMatcher.addURI(AUTHORITY, "local", TYPE_LOCAL);
        sUriMatcher.addURI(AUTHORITY, "normal", TYPE_NORMAL);
        sUriMatcher.addURI(AUTHORITY, "net/*", TYPE_NET_ALL);
        sUriMatcher.addURI(AUTHORITY, "local/*", TYPE_LOCAL_ALL);
        sUriMatcher.addURI(AUTHORITY, "normal/*", TYPE_NORMAL_ALL);
    }

    private static final String SUGGEST_URL_BASE = "http://suggestqueries.google.com/complete/search?output=firefox&oe=utf-8&hl={{LANG}}&qu={{TERMS}}";

    private static final String[] yuzuPrefix = {
            "yuzu:bookmarks", "yuzu:debug", "yuzu:downloads", "yuzu:history", "yuzu:home", "yuzu:settings", "yuzu:speeddial"
    };

    private static final int COL_ID = 0;
    //private static final int COL_TEXT_1 = 1;
    //private static final int COL_TEXT_2 = 2;
    //private static final int COL_ICON_1 = 3;
    //private static final int COL_ICON_2 = 4;
    private static final int COL_QUERY = 5;

    private static final String[] COLUMNS = new String[]{
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_ICON_2,
            SearchManager.SUGGEST_COLUMN_QUERY,
    };

    private static final String DB_NAME = "searchsuggest.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "main_table1";

    private final JsonFactory mJsonFactory = new JsonFactory();
    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int type = sUriMatcher.match(uri);
        if (type == UriMatcher.NO_MATCH) {
            Logger.e(TAG, "UriMatcher.NO_MATCH");
            return null;
        }

        List<String> list = uri.getPathSegments();
        String query = (list.size() > 1) ? list.get(1) : null;
        switch (type) {
            case TYPE_NET_ALL:
            case TYPE_NET:
                return queryNet(query);
            case TYPE_LOCAL_ALL:
            case TYPE_LOCAL:
                return queryLocal(query);
            case TYPE_NORMAL_ALL:
            case TYPE_NORMAL:
                if (TextUtils.isEmpty(query))
                    return queryLocal(query);
                else
                    return queryNet(query);
        }
        return null;
    }

    private Cursor queryNet(String query) {
        if (TextUtils.isEmpty(query)) {
            return null;
        }
        JsonParser parser = null;
        try {
            URL url = new URL(SUGGEST_URL_BASE.replace("{{LANG}}", Locale.getDefault().getLanguage()).replace("{{TERMS}}", URLEncoder.encode(query, "UTF-8")));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == 200) {
                ArrayList<Suggestion> list = new ArrayList<>();

                parser = mJsonFactory.createParser(connection.getInputStream());

                if (parser.nextToken() != JsonToken.START_ARRAY) return null;
                parser.nextToken(); //query
                if (parser.nextToken() != JsonToken.START_ARRAY) return null;
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    list.add(new Suggestion(parser.getText()));
                }

                for (String prefix : yuzuPrefix) {
                    if (prefix.startsWith(query)) {
                        list.add(new Suggestion(prefix));
                    }
                }

                return new SuggestionsCursor(list);
            }
        } catch (UnknownHostException e) {
            return queryLocal(query);
        } catch (IOException | IllegalStateException e) {
            ErrorReport.printAndWriteLog(e);
        } finally {
            if (parser != null)
                try {
                    parser.close();
                } catch (IOException e) {
                    ErrorReport.printAndWriteLog(e);
                }
        }
        return null;
    }

    private Cursor queryLocal(String query) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (TextUtils.isEmpty(query))
            return db.query(TABLE_NAME, null, null, null, null, null, BaseColumns._ID + " DESC");
        else
            return addYuzuPrefix(query, db.query(TABLE_NAME, null, SearchManager.SUGGEST_COLUMN_QUERY + " like ?", new String[]{"%" + query + "%"}, null, null, BaseColumns._ID + " DESC"));
    }

    private Cursor addYuzuPrefix(String query, Cursor c) {
        ArrayList<Suggestion> suggestions = new ArrayList<>();
        if (!TextUtils.isEmpty(query)) {
            for (String prefix : yuzuPrefix) {
                if (prefix.startsWith(query)) {
                    suggestions.add(new Suggestion(prefix));
                }
            }
        }
        if (c != null) {
            final int COL_QUERY = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY);
            while (c.moveToNext()) {
                suggestions.add(new Suggestion(c.getString(COL_QUERY)));
            }
            c.close();
        }
        return new SuggestionsCursor(suggestions);
    }

    private static class Suggestion {
        public Suggestion(String word) {
            this.word = word;
        }

        public final String word;
    }

    private static class SuggestionsCursor extends AbstractCursor {
        private ArrayList<Suggestion> mList;

        public SuggestionsCursor(ArrayList<Suggestion> list) {
            mList = list;
        }

        @Override
        public String[] getColumnNames() {
            return COLUMNS;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public String getString(int column) {
            if (getPosition() == -1) return null;
            switch (column) {
                case COL_ID:
                    return String.valueOf(getPosition());
                //case COL_TEXT_1:
                case COL_QUERY:
                    return mList.get(getPosition()).word;
            }
            return null;
        }

        @Override
        public long getLong(int column) {
            if (column == COL_ID) {
                return getPosition();
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public double getDouble(int column) {
            throw new UnsupportedOperationException();
        }

        @Override
        public float getFloat(int column) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getInt(int column) {
            throw new UnsupportedOperationException();
        }

        @Override
        public short getShort(int column) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isNull(int column) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int type = sUriMatcher.match(uri);
        if (type == TYPE_NET || type == TYPE_NET_ALL)
            return 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.delete(TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return SearchManager.SUGGEST_MIME_TYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int type = sUriMatcher.match(uri);
        if (type == TYPE_NET || type == TYPE_NET_ALL)
            return null;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY" +
                        ", " + SearchManager.SUGGEST_COLUMN_QUERY + " TEXT UNIQUE ON CONFLICT REPLACE" +
                        ")");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
