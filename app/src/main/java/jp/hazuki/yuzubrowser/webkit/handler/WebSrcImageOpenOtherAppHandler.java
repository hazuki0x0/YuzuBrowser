package jp.hazuki.yuzubrowser.webkit.handler;

import android.content.Context;

import java.lang.ref.WeakReference;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.PackageUtils;

public class WebSrcImageOpenOtherAppHandler extends WebSrcImageHandler {
    private final WeakReference<Context> mReference;

    public WebSrcImageOpenOtherAppHandler(Context activity) {
        mReference = new WeakReference<>(activity);
    }

    @Override
    public void handleUrl(String url) {
        Context context = mReference.get();
        if (context != null) {
            context.startActivity(PackageUtils.createChooser(context, url, context.getText(R.string.open_other_app)));
        }
    }
}
