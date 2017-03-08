package jp.hazuki.yuzubrowser.utils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppInfoUtils {
    private AppInfoUtils() {
        throw new UnsupportedOperationException();
    }

    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 1).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 1).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "-1";
    }
}
