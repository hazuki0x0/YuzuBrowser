package jp.hazuki.yuzubrowser.webkit.handler;

import android.app.Activity;

import java.lang.ref.WeakReference;

import jp.hazuki.yuzubrowser.utils.WebUtils;

public class WebSrcImageShareWebHandler extends WebSrcImageHandler {
    private final WeakReference<Activity> mReference;

    public WebSrcImageShareWebHandler(Activity activity) {
        mReference = new WeakReference<>(activity);
    }

    @Override
    public void handleUrl(String url) {
        Activity activity = mReference.get();
        if (activity != null)
            WebUtils.shareWeb(activity, url, null, null, null);
    }
}
