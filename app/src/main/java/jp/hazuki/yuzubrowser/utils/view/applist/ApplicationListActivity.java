package jp.hazuki.yuzubrowser.utils.view.applist;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import jp.hazuki.yuzubrowser.R;

public class ApplicationListActivity extends FragmentActivity implements LoaderCallbacks<List<ResolveInfo>> {
    private ListView listView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_list_activity);
        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();
        if (intent == null)
            throw new IllegalArgumentException("Intent is null");

        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        if (title != null)
            setTitle(title);

        final Intent query_intent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        if (query_intent == null)
            throw new IllegalArgumentException("Query intent is null");

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApplicationListAdapter adapter = (ApplicationListAdapter) parent.getAdapter();
                if (adapter != null) {
                    Intent resultintent = new Intent();

                    ResolveInfo item = adapter.getItem(position);
                    Intent intent = new Intent(query_intent);
                    intent.setClassName(item.activityInfo.packageName, item.activityInfo.name);

                    resultintent.putExtra(Intent.EXTRA_INTENT, intent);

                    setResult(RESULT_OK, resultintent);
                    finish();
                }
            }
        });

        Bundle bundle = new Bundle();
        bundle.putParcelable(Intent.EXTRA_INTENT, query_intent);
        getSupportLoaderManager().initLoader(0, bundle, this).forceLoad();
    }

    @Override
    public Loader<List<ResolveInfo>> onCreateLoader(int arg0, Bundle bundle) {
        return new ApplicationListLoader(getApplicationContext(), (Intent) bundle.getParcelable(Intent.EXTRA_INTENT));
    }

    @Override
    public void onLoadFinished(Loader<List<ResolveInfo>> arg0, List<ResolveInfo> list) {
        listView.setAdapter(new ApplicationListAdapter(getApplicationContext(), list));
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<List<ResolveInfo>> arg0) {
        listView.setAdapter(null);
        progressBar.setVisibility(View.VISIBLE);
    }
}
