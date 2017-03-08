package jp.hazuki.yuzubrowser.utils.view.applist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.Collections;
import java.util.List;

import jp.hazuki.yuzubrowser.R;

public class ApplicationListDialog {
    private final Context mContext;
    private final AlertDialog.Builder mBuilder;
    private final Intent mQueryIntent;
    private ProgressBar progressBar;
    private ListView listView;
    private OnSelectIntentListener mListener;
    private AsyncTask<Void, Void, List<ResolveInfo>> mTask;

    public ApplicationListDialog(Context context, Intent intent) {
        mContext = context;
        mQueryIntent = intent;

        View view = LayoutInflater.from(context).inflate(R.layout.application_list_activity, null);
        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApplicationListAdapter adapter = (ApplicationListAdapter) listView.getAdapter();
                if (adapter == null)
                    return;
                if (mListener == null)
                    return;

                ResolveInfo item = adapter.getItem(position);
                Intent intent = new Intent(mQueryIntent);
                intent.setClassName(item.activityInfo.packageName, item.activityInfo.name);
                mListener.onSelectIntent(intent);
            }
        });

        mBuilder = new AlertDialog.Builder(context);
        mBuilder
                .setView(view)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel();
                    }
                });
    }

    public interface OnSelectIntentListener {
        void onSelectIntent(Intent intent);
    }

    public ApplicationListDialog setOnSelectIntentListener(OnSelectIntentListener l) {
        mListener = l;
        return this;
    }

    public void cancel() {
        if (mTask != null)
            mTask.cancel(true);
    }

    public Context getContext() {
        //return mBuilder.getContext();//API 11
        return mContext;
    }

    public void show() {
        mBuilder.show();

        mTask = new AsyncTask<Void, Void, List<ResolveInfo>>() {
            @Override
            protected List<ResolveInfo> doInBackground(Void... params) {
                PackageManager pm = getContext().getPackageManager();
                List<ResolveInfo> list = pm.queryIntentActivities(mQueryIntent, 0);
                Collections.sort(list, new ResolveInfo.DisplayNameComparator(pm));
                return list;
            }

            @Override
            protected void onPostExecute(List<ResolveInfo> result) {
                listView.setAdapter(new ApplicationListAdapter(getContext().getApplicationContext(), result));
                progressBar.setVisibility(View.GONE);
                mTask = null;
            }
        }.execute();
    }
}
