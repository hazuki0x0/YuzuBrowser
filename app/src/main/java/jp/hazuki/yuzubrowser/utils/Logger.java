package jp.hazuki.yuzubrowser.utils;

import android.util.Log;

import java.net.URISyntaxException;

import jp.hazuki.yuzubrowser.BuildConfig;

/**
 * Created by hazuki on 17/01/26.
 */

public class Logger {

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, Object msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg.toString());
        }
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String s, URISyntaxException e) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, s, e);
        }
    }
}
