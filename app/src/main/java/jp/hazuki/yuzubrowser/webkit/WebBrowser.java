package jp.hazuki.yuzubrowser.webkit;

import android.os.Bundle;

import jp.hazuki.yuzubrowser.tab.MainTabData;

public interface WebBrowser {
    MainTabData addNewTab(@TabType int type);

    MainTabData addNewTab(boolean cacheType, @TabType int type);

    CustomWebView makeWebView(boolean cacheType);

    void openInNewTab(String url, @TabType int type);

    void openInBackground(String url, @TabType int type);

    void openInRightNewTab(String url, @TabType int type);

    void openInRightBgTab(String url, @TabType int type);

    void setCurrentTab(int no);

    int getCurrentTab();

    int getTabCount();

    boolean saveWebState(Bundle bundle);

    void restoreWebState(Bundle bundle);

    void loadUrl(String url, int target);
}
