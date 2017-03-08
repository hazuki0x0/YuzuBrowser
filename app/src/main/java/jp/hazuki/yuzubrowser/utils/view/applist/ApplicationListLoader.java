package jp.hazuki.yuzubrowser.utils.view.applist;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.content.AsyncTaskLoader;

import java.util.Collections;
import java.util.List;

public class ApplicationListLoader extends AsyncTaskLoader<List<ResolveInfo>> {
    private final Intent mQueryIntent;

    public ApplicationListLoader(Context context, Intent query_intent) {
        super(context);
        mQueryIntent = query_intent;
    }

    @Override
    public List<ResolveInfo> loadInBackground() {
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(mQueryIntent, 0);
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(pm));
        return list;
    }
}
