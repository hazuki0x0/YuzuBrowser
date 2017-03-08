package jp.hazuki.yuzubrowser.utils.app;

import android.content.Intent;

public class StartActivityInfo {
    private final Intent mIntent;
    private final OnActivityResultListener mListener;

    public StartActivityInfo(Intent intent, OnActivityResultListener l) {
        mIntent = intent;
        mListener = l;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public OnActivityResultListener getOnActivityResultListener() {
        return mListener;
    }
}
