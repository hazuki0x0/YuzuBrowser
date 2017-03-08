package jp.hazuki.yuzubrowser.webkit.handler;

import java.lang.ref.WeakReference;

import jp.hazuki.yuzubrowser.webkit.TabType;
import jp.hazuki.yuzubrowser.webkit.WebBrowser;

public class WebSrcImageOpenNewTabHandler extends WebSrcImageHandler {
    private final WeakReference<WebBrowser> mReference;

    public WebSrcImageOpenNewTabHandler(WebBrowser web) {
        mReference = new WeakReference<>(web);
    }

    @Override
    public void handleUrl(String url) {
        WebBrowser web = mReference.get();
        if (web != null)
            web.openInNewTab(url, TabType.WINDOW);
    }
}
