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

package jp.hazuki.yuzubrowser.provider;

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

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import jp.hazuki.yuzubrowser.BuildConfig;
import jp.hazuki.yuzubrowser.ErrorReportServer;
import jp.hazuki.yuzubrowser.core.utility.log.Logger;
import jp.hazuki.yuzubrowser.legacy.search.suggest.ISuggest;
import jp.hazuki.yuzubrowser.legacy.search.suggest.SuggestBing;
import jp.hazuki.yuzubrowser.legacy.search.suggest.SuggestDuckDuckGo;
import jp.hazuki.yuzubrowser.legacy.search.suggest.SuggestGoogle;
import jp.hazuki.yuzubrowser.legacy.search.suggest.Suggestion;
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData;
import jp.hazuki.yuzubrowser.legacy.utils.JsonUtils;

public class SuggestProvider extends ContentProvider {
    private static final String TAG = "GoogleSuggestProvider";
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".search.SuggestProvider";
    public static final Uri URI_NET = Uri.parse("content://" + AUTHORITY + "/net");
    public static final Uri URI_LOCAL = Uri.parse("content://" + AUTHORITY + "/local");
    public static final Uri URI_NORMAL = Uri.parse("content://" + AUTHORITY + "/normal");
    public static final Uri URI_NONE = Uri.EMPTY;
    public static final String SUGGEST_HISTORY = "suggest_history";
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

    private static final String[] yuzuPrefix = {
            "yuzu:bookmarks", "yuzu:debug", "yuzu:downloads", "yuzu:history", "yuzu:home", "yuzu:readItLater", "yuzu:resBlock", "yuzu:settings", "yuzu:speeddial"
    };

    private static final int COL_ID = 0;
    //private static final int COL_TEXT_1 = 1;
    //private static final int COL_TEXT_2 = 2;
    //private static final int COL_ICON_1 = 3;
    //private static final int COL_ICON_2 = 4;
    private static final int COL_QUERY = 5;
    private static final int COL_HISTORY = 6;

    private static final String[] COLUMNS = new String[]{
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_ICON_2,
            SearchManager.SUGGEST_COLUMN_QUERY,
            SUGGEST_HISTORY,
    };

    private static final String DB_NAME = "searchsuggest.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "main_table1";

    private DatabaseHelper mOpenHelper;

    private int mSuggestType;
    private ISuggest mSuggestEngine;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        mSuggestType = AppData.search_suggest_engine.get();
        mSuggestEngine = getSuggestEngine(mSuggestType);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int type = sUriMatcher.match(uri);
        if (type == UriMatcher.NO_MATCH) {
            Logger.e(TAG, "UriMatcher.NO_MATCH");
            return null;
        }

        String query = uri.getQueryParameter("q");
        if (query != null) query = query.trim();
        switch (type) {
            case TYPE_NET_ALL:
            case TYPE_NET:
                return queryNet(query);
            case TYPE_LOCAL_ALL:
            case TYPE_LOCAL:
                return queryLocal(query);
            case TYPE_NORMAL_ALL:
            case TYPE_NORMAL:
                return queryBoth(query);
        }
        return null;
    }

    private Cursor queryBoth(String query) {
        if (TextUtils.isEmpty(query)) {
            return queryLocal(query);
        }

        try {
            List<Suggestion> net = getSuggests(query);
            if (net != null) {
                String dbQuery = query.replace("%", "$%").replace("_", "$_");
                List<Suggestion> suggestions = new ArrayList<>();

                SQLiteDatabase db = mOpenHelper.getReadableDatabase();
                Cursor c = db.query(TABLE_NAME, null, SearchManager.SUGGEST_COLUMN_QUERY + " LIKE '%' || ? || '%' ESCAPE '$'", new String[]{dbQuery}, null, null, BaseColumns._ID + " DESC", "3");
                int COL_QUERY = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY);
                while (c.moveToNext()) {
                    Suggestion suggestion = new Suggestion(c.getString(COL_QUERY), true);

                    suggestions.add(suggestion);
                    net.remove(suggestion);
                }
                c.close();

                suggestions.addAll(net);

                for (String prefix : yuzuPrefix) {
                    if (prefix.startsWith(query)) {
                        suggestions.add(new Suggestion(prefix));
                    }
                }

                return new SuggestionsCursor(suggestions);
            }
        } catch (UnknownHostException e) {
            return queryLocal(query);
        }

        return null;
    }

    private Cursor queryNet(String query) {
        if (TextUtils.isEmpty(query)) {
            return null;
        }

        try {
            List<Suggestion> list = getSuggests(query);
            if (list != null) {
                for (String prefix : yuzuPrefix) {
                    if (prefix.startsWith(query)) {
                        list.add(new Suggestion(prefix));
                    }
                }
                return new SuggestionsCursor(list);
            }
        } catch (UnknownHostException e) {
            return queryLocal(query);
        }

        return null;
    }

    private List<Suggestion> getSuggests(String query) throws UnknownHostException {
        if (AppData.search_suggest_engine.get() != mSuggestType) {
            mSuggestType = AppData.search_suggest_engine.get();
            mSuggestEngine = getSuggestEngine(mSuggestType);
        }

        JsonParser parser = null;
        try {
            URL url = mSuggestEngine.getUrl(query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(2000);
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == 200) {
                parser = JsonUtils.getFactory().createParser(connection.getInputStream());

                return mSuggestEngine.getSuggestions(parser);
            }
        } catch (UnknownHostException e) {
            throw e;
        } catch (IOException | IllegalStateException | ArrayIndexOutOfBoundsException e) {
            // ArrayIndexOutOfBoundsException - workaround for OkHttp
            ErrorReportServer.printAndWriteLog(e);
        } finally {
            if (parser != null)
                try {
                    parser.close();
                } catch (IOException e) {
                    ErrorReportServer.printAndWriteLog(e);
                }
        }
        return null;
    }

    private Cursor queryLocal(String query) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        if (TextUtils.isEmpty(query))
            return wrapCursor(db.query(TABLE_NAME, null, null, null, null, null, BaseColumns._ID + " DESC"));
        else {
            String dbQuery = query.replace("%", "$%").replace("_", "$_");
            return addYuzuPrefix(query, db.query(TABLE_NAME, null, SearchManager.SUGGEST_COLUMN_QUERY + " LIKE '%' || ? || '%' ESCAPE '$'", new String[]{dbQuery}, null, null, BaseColumns._ID + " DESC"));
        }
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
                suggestions.add(new Suggestion(c.getString(COL_QUERY), true));
            }
            c.close();
        }
        return new SuggestionsCursor(suggestions);
    }

    private Cursor wrapCursor(Cursor c) {
        ArrayList<Suggestion> suggestions = new ArrayList<>();
        if (c != null) {
            final int COL_QUERY = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY);
            while (c.moveToNext()) {
                suggestions.add(new Suggestion(c.getString(COL_QUERY), true));
            }
            c.close();
        }
        return new SuggestionsCursor(suggestions);
    }

    private ISuggest getSuggestEngine(int type) {
        switch (type) {
            case 1:
                return new SuggestBing();
            case 2:
                return new SuggestDuckDuckGo();
            default:
                return new SuggestGoogle();
        }
    }

    public static class SuggestionsCursor extends AbstractCursor {
        private List<Suggestion> mList;

        public SuggestionsCursor(List<Suggestion> list) {
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
                    return mList.get(getPosition()).getTitle();
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
            if (getPosition() == -1) return 0;
            if (column == COL_HISTORY) {
                return mList.get(getPosition()).getSuggestHistory() ? 1 : 0;
            } else {
                return 0;
            }
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
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int type = sUriMatcher.match(uri);
        if (type == TYPE_NET || type == TYPE_NET_ALL)
            return 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.delete(TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return SearchManager.SUGGEST_MIME_TYPE;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int type = sUriMatcher.match(uri);
        if (type == TYPE_NET || type == TYPE_NET_ALL)
            return null;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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
