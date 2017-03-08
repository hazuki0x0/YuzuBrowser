package jp.hazuki.yuzubrowser.utils.app;

import android.content.Context;
import android.content.Intent;

public interface OnActivityResultListener {
    void onActivityResult(Context context, int resultCode, Intent intent);
}
