package jp.hazuki.yuzubrowser.webkit.handler;

import java.lang.ref.WeakReference;

import jp.hazuki.yuzubrowser.webkit.TabType;
import jp.hazuki.yuzubrowser.webkit.WebBrowser;

public class WebSrcImageOpenRightNewTabHandler extends WebSrcImageHandler {
    private final WeakReference<WebBrowser> mReference;

    public WebSrcImageOpenRightNewTabHandler(WebBrowser web) {
        mReference = new WeakReference<>(web);
    }

    @Override
    public void handleUrl(String url) {
        WebBrowser web = mReference.get();
        if (web != null)
            web.openInRightNewTab(url, TabType.WINDOW);
    }
}
