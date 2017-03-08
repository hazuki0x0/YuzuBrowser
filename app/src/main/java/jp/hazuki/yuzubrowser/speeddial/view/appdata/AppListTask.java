package jp.hazuki.yuzubrowser.speeddial.view.appdata;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by hazuki on 16/10/20.
 */

public class AppListTask extends AsyncTaskLoader<ArrayList<AppData>> {

    private PackageManager pm;
    private Intent mIntent;
    private boolean allApp = false;

    public AppListTask(Context context, Intent search, boolean all) {
        super(context);

        pm = context.getPackageManager();
        mIntent = search;
        allApp = all;
    }

    @Override
    public ArrayList<AppData> loadInBackground() {
        ArrayList<AppData> appDataList = new ArrayList<>();

        if (allApp) {
            List<PackageInfo> list = pm.getInstalledPackages(PackageManager.GET_META_DATA);
            AppData appData;
            for (PackageInfo packageInfo : list) {
                appData = new AppData();
                appData.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
                appData.setPackageName(packageInfo.packageName);
                appData.setIcon(packageInfo.applicationInfo.loadIcon(pm));
                appData.setClassName(packageInfo.applicationInfo.className);
                appDataList.add(appData);
            }
        } else {
            int flag = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                flag = PackageManager.MATCH_ALL;
            List<ResolveInfo> list = pm.queryIntentActivities(mIntent, flag);

            AppData appData;
            for (ResolveInfo info : list) {
                appData = new AppData();
                //アプリ名取得
                appData.setAppName(info.loadLabel(pm).toString());
                appData.setPackageName(info.activityInfo.packageName);
                appData.setIcon(info.loadIcon(pm));
                appData.setClassName(info.activityInfo.name);
                appDataList.add(appData);
            }
        }

        Collections.sort(appDataList, new AppDataCollator());
        return appDataList;
    }

    @Override
    protected void onStartLoading() {
        if (mIntent == null) allApp = true;
        forceLoad();
    }
}
