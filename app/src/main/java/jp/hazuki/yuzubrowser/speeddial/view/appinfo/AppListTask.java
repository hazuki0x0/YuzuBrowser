package jp.hazuki.yuzubrowser.speeddial.view.appinfo;

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

public class AppListTask extends AsyncTaskLoader<ArrayList<AppInfo>> {

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
    public ArrayList<AppInfo> loadInBackground() {
        ArrayList<AppInfo> appInfoList = new ArrayList<>();

        if (allApp) {
            List<PackageInfo> list = pm.getInstalledPackages(PackageManager.GET_META_DATA);
            AppInfo appInfo;
            for (PackageInfo packageInfo : list) {
                appInfo = new AppInfo();
                appInfo.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
                appInfo.setPackageName(packageInfo.packageName);
                appInfo.setIcon(packageInfo.applicationInfo.loadIcon(pm));
                appInfo.setClassName(packageInfo.applicationInfo.className);
                appInfoList.add(appInfo);
            }
        } else {
            int flag = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                flag = PackageManager.MATCH_ALL;
            List<ResolveInfo> list = pm.queryIntentActivities(mIntent, flag);

            AppInfo appInfo;
            for (ResolveInfo info : list) {
                appInfo = new AppInfo();
                //アプリ名取得
                appInfo.setAppName(info.loadLabel(pm).toString());
                appInfo.setPackageName(info.activityInfo.packageName);
                appInfo.setIcon(info.loadIcon(pm));
                appInfo.setClassName(info.activityInfo.name);
                appInfoList.add(appInfo);
            }
        }

        Collections.sort(appInfoList, new AppInfoCollator());
        return appInfoList;
    }

    @Override
    protected void onStartLoading() {
        if (mIntent == null) allApp = true;
        forceLoad();
    }
}
