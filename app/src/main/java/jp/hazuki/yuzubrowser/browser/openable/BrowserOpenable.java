package jp.hazuki.yuzubrowser.browser.openable;

import android.os.Parcelable;

import jp.hazuki.yuzubrowser.webkit.WebBrowser;

public interface BrowserOpenable extends Parcelable {
    void open(WebBrowser browser);
}
