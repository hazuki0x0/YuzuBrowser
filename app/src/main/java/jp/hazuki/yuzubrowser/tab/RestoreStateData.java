package jp.hazuki.yuzubrowser.tab;

import android.os.Bundle;

import jp.hazuki.yuzubrowser.webkit.CacheWebView;
import jp.hazuki.yuzubrowser.webkit.WebBrowser;

public final class RestoreStateData implements RestoreTabData {
    private final int currentNo;
    private final Bundle[] list;
    private final int[] tabType;

    public RestoreStateData(int currentNo, Bundle[] list, int[] tabType) {
        this.currentNo = currentNo;
        this.list = list;
        this.tabType = tabType;
    }

    public boolean restoreWebViewState(WebBrowser browser) {
        int length = list.length;
        if (length > 0) {
            MainTabData tablist[] = new MainTabData[length];

            for (int i = 0; i < length; ++i)
                tablist[i] = browser.addNewTab(CacheWebView.isBundleCacheWebView(list[i]), tabType[i]);

            int tabCount = browser.getTabCount();
            if (currentNo >= 0 && currentNo < tabCount)
                browser.setCurrentTab(currentNo);
            else if (tabCount != 0)
                browser.setCurrentTab(0);

            for (int i = 0; i < length; ++i)
                tablist[i].mWebView.restoreState(list[i]);

            return true;
        }
        return false;
    }
}