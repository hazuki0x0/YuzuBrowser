package jp.hazuki.yuzubrowser.webkit.handler;

import android.content.Context;

import java.lang.ref.WeakReference;

import jp.hazuki.yuzubrowser.utils.ClipboardUtils;

public class WebSrcImageCopyUrlHandler extends WebSrcImageHandler {
    private final WeakReference<Context> mReference;

    public WebSrcImageCopyUrlHandler(Context context) {
        mReference = new WeakReference<>(context);
    }

    @Override
    public void handleUrl(String url) {
        Context context = mReference.get();
        if (context != null)
            ClipboardUtils.setClipboardText(context, url);
    }
}
