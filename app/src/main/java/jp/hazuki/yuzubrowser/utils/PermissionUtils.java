package jp.hazuki.yuzubrowser.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by hazuki on 17/01/19.
 */

public class PermissionUtils {

    public static final int FIRST_PERMISSION = 1;
    public static final int REQUEST_LOCATION = 2;
    public static final int REQUEST_STORAGE = 3;

    private static final String PREF = "permission";
    private static final String NO_NEED = "no_need";
    private static SharedPreferences preferences;

    @TargetApi(Build.VERSION_CODES.M)
    public static void checkFirst(Activity activity) {
        if (checkNeed(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, FIRST_PERMISSION);
            }
            getPref(activity).edit().putBoolean(NO_NEED, true).apply();
        } else {
            if (!checkWriteStorage(activity)) {
                setNoNeed(activity, false);
                activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
            }
        }
    }

    public static boolean checkLocation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static void requestLocation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    public static boolean checkWriteStorage(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static void requestStorage(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        }
    }

    public static void setNoNeed(Context context, boolean noNeed) {
        if (checkNeed(context) != noNeed)
            getPref(context).edit().putBoolean(NO_NEED, noNeed).apply();
    }

    private static boolean checkNeed(Context context) {
        return !getPref(context).getBoolean(NO_NEED, false);
    }

    private static SharedPreferences getPref(Context context) {
        if (preferences == null)
            preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return preferences;
    }
}
