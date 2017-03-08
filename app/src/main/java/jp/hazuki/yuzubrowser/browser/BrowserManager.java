package jp.hazuki.yuzubrowser.browser;

import android.content.Context;
import android.webkit.GeolocationPermissions;
import android.webkit.WebStorage;

import java.io.File;

import jp.hazuki.yuzubrowser.utils.FileUtils;
import jp.hazuki.yuzubrowser.utils.content.BundleDatabase;

public class BrowserManager {
    public static final String EXTRA_OPENABLE = "jp.hazuki.yuzubrowser.BrowserActivity.extra.EXTRA_OPENABLE";
    public static final String EXTRA_LOAD_URL_TAB = "jp.hazuki.yuzubrowser.BrowserActivity.extra.EXTRA_LOAD_URL_TAB";
    //same as pref_newtab_values
    public static final int LOAD_URL_TAB_CURRENT = 0;
    public static final int LOAD_URL_TAB_NEW = 1;
    public static final int LOAD_URL_TAB_BG = 2;
    public static final int LOAD_URL_TAB_NEW_RIGHT = 3;
    public static final int LOAD_URL_TAB_BG_RIGHT = 4;

    public static File getAppCacheFile(Context context) {
        return context.getDir("appcache", Context.MODE_PRIVATE);
    }

    public static String getAppCacheFilePath(Context context) {
        return getAppCacheFile(context).getAbsolutePath();
    }

    public static void clearAppCacheFile(Context context) {
        FileUtils.deleteFile(context.getDir("appcache", Context.MODE_PRIVATE));
    }

    public static void clearWebDatabase() {
        WebStorage.getInstance().deleteAllData();
    }

    public static void clearGeolocation() {
        GeolocationPermissions.getInstance().clearAll();
    }

    public static BundleDatabase getLastTabListDatabase(Context context) {
        return new BundleDatabase(context.getFileStreamPath("last_url_2.dat"));
    }

}
