package jp.hazuki.yuzubrowser.webkit.handler;

import java.lang.ref.WeakReference;

import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class WebSrcImageLoadUrlHandler extends WebSrcImageHandler {
    private final WeakReference<CustomWebView> mReference;

    public WebSrcImageLoadUrlHandler(CustomWebView web) {
        mReference = new WeakReference<>(web);
    }

    @Override
    public void handleUrl(String url) {
        CustomWebView web = mReference.get();
        if (web != null)
            web.loadUrl(url);
    }
}
