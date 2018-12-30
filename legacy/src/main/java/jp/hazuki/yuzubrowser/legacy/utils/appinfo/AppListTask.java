/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.utils.appinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.loader.content.AsyncTaskLoader;

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
