package jp.hazuki.yuzubrowser.webkit.handler;

import java.lang.ref.WeakReference;

import jp.hazuki.yuzubrowser.webkit.TabType;
import jp.hazuki.yuzubrowser.webkit.WebBrowser;

public class WebSrcImageOpenRightBgTabHandler extends WebSrcImageHandler {
    private final WeakReference<WebBrowser> mReference;

    public WebSrcImageOpenRightBgTabHandler(WebBrowser web) {
        mReference = new WeakReference<>(web);
    }

    @Override
    public void handleUrl(String url) {
        WebBrowser web = mReference.get();
        if (web != null)
            web.openInRightBgTab(url, TabType.WINDOW);
    }
}
