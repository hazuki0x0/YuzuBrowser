package jp.hazuki.yuzubrowser.search;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.data.ThemeData;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.WebUtils;

public class SearchActivity extends AppCompatActivity implements TextWatcher, LoaderCallbacks<Cursor>, SearchButton.Callback {
    private static final String TAG = "SearchActivity";
    public static final String EXTRA_URI = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.uri";
    public static final String EXTRA_QUERY = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.query";
    public static final String EXTRA_SELECT_INITIAL_QUERY = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.selectinitquery";
    public static final String EXTRA_APP_DATA = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.appdata";
    public static final String EXTRA_SEARCH_MODE = "jp.hazuki.yuzubrowser.search.SearchActivity.extra.searchmode";
    public static final int SEARCH_MODE_AUTO = 0;
    public static final int SEARCH_MODE_URL = 1;
    public static final int SEARCH_MODE_WORD = 2;

    private static final int RESULT_REQUEST_SPEECH = 0;

    private Uri mContentUri;
    private Bundle mAppData;

    private EditText editText;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        editText = (EditText) findViewById(R.id.editText);
        SearchButton searchButton = (SearchButton) findViewById(R.id.searchButton);
        listView = (ListView) findViewById(R.id.listView);

        if (ThemeData.isEnabled()) {
            findViewById(R.id.search_bar_container).setBackgroundColor(ThemeData.getInstance().toolbarBackgroundColor);
            int textColor = ThemeData.getInstance().toolbarTextColor;
            editText.setTextColor(textColor);
            editText.setHintTextColor(textColor & 0xffffff | 0x55000000);
        }

        editText.addTextChangedListener(this);
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    finishWithResult(editText.getText().toString(), SEARCH_MODE_AUTO);
                    return true;
                }
                return false;
            }
        });

        searchButton.setActionCallback(this);
        searchButton.setSense(AppData.swipebtn_sensitivity.get());

        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getParcelableExtra(EXTRA_URI);
            if (uri != null) {
                mContentUri = uri;
            } else {
                switch (AppData.search_suggest.get()) {
                    case 0:
                    default:
                        mContentUri = SuggestProvider.URI_NORMAL;
                        break;
                    case 1:
                        mContentUri = SuggestProvider.URI_NET;
                        break;
                    case 2:
                        mContentUri = SuggestProvider.URI_LOCAL;
                        break;
                }
            }

            String initquery = intent.getStringExtra(EXTRA_QUERY);
            if (initquery != null) {
                editText.setText(initquery);
            }

            if (intent.getBooleanExtra(EXTRA_SELECT_INITIAL_QUERY, true)) {
                editText.selectAll();
            }

            mAppData = intent.getBundleExtra(EXTRA_APP_DATA);
        } else {
            throw new IllegalStateException("Intent is null");
        }
    }

    @Override
    public void afterTextChanged(final Editable s) {
        final String query = s.toString();

        Bundle bundle = new Bundle();
        bundle.putString("QUERY", query);
        getSupportLoaderManager().restartLoader(0, bundle, this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
        String query;
        try {
            query = URLEncoder.encode(bundle.getString("QUERY"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            ErrorReport.printAndWriteLog(e);
            query = bundle.getString("QUERY");
        }
        return new CursorLoader(getApplicationContext(), Uri.withAppendedPath(mContentUri, query), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        if (c == null) {
            Logger.d(TAG, "Cursor is null");
            listView.setAdapter(null);
            return;
        }

        final int COL_QUERY = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY);
        CursorAdapter adapter = new CursorAdapter(getApplicationContext(), c, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                final String query = cursor.getString(COL_QUERY);
                ((TextView) view.findViewById(R.id.textView)).setText(query);
                view.findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishWithResult(query, SEARCH_MODE_AUTO);
                    }
                });
                view.findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        editText.setText(query);
                        editText.setSelection(query.length());
                    }
                });
            }

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.search_activity_list_item, null);
            }
        };
        listView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        listView.setAdapter(null);
    }

    private void finishWithResult(String query, int mode) {
        if (!TextUtils.isEmpty(query) && mode != SEARCH_MODE_URL && !WebUtils.isUrl(query)) {
            ContentValues values = new ContentValues();
            values.put(SearchManager.SUGGEST_COLUMN_QUERY, query);
            getContentResolver().insert(mContentUri, values);
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_QUERY, query);
        data.putExtra(EXTRA_SEARCH_MODE, mode);
        if (mAppData != null)
            data.putExtra(EXTRA_APP_DATA, mAppData);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void forceOpenUrl() {
        finishWithResult(editText.getText().toString(), SEARCH_MODE_URL);
    }

    @Override
    public void forceSearchWord() {
        finishWithResult(editText.getText().toString(), SEARCH_MODE_WORD);
    }

    @Override
    public void autoSearch() {
        finishWithResult(editText.getText().toString(), SEARCH_MODE_AUTO);
    }

    @Override
    public void recognizeSpeech() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, RESULT_REQUEST_SPEECH);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_REQUEST_SPEECH:
                if (resultCode != RESULT_OK || data == null) break;
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (!results.isEmpty()) {
                    String query = results.get(0);
                    editText.setText(query);
                    editText.setSelection(query.length());
                }
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            finish();
        }
        return super.onTouchEvent(event);
    }
}
