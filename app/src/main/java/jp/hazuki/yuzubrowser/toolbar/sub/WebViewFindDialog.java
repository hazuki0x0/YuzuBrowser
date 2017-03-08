package jp.hazuki.yuzubrowser.toolbar.sub;

import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public interface WebViewFindDialog {
    void show(CustomWebView web);

    void hide();

    boolean isVisible();
}
