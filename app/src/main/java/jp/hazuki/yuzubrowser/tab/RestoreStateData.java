package jp.hazuki.yuzubrowser.tab;

import android.os.Bundle;

import jp.hazuki.yuzubrowser.webkit.CacheWebView;
import jp.hazuki.yuzubrowser.webkit.WebBrowser;

public final class RestoreStateData implements RestoreTabData {
    private final int currentNo;
    private final Bundle[] list;
    private final int[] tabType;
    private final long[] ids;
    private final long[] parents;

    public RestoreStateData(int currentNo, Bundle[] list, int[] tabType, long[] ids, long[] parents) {
        this.currentNo = currentNo;
        this.list = list;
        this.tabType = tabType;
        this.ids = ids;
        this.parents = parents;
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

            MainTabData tabData;
            for (int i = 0; i < length; ++i) {
                tabData = tablist[i];
                if (ids[i] > 0)
                    tabData.mWebView.setIdentityId(ids[i]);
                tabData.setParent(parents[i]);
                tabData.mWebView.restoreState(list[i]);
            }

            return true;
        }
        return false;
    }
}