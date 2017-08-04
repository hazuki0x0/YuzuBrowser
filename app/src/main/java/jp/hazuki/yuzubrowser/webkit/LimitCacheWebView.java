/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.webkit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.print.PrintDocumentAdapter;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.FrameLayout;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.tab.manager.TabCache;
import jp.hazuki.yuzubrowser.tab.manager.TabData;
import jp.hazuki.yuzubrowser.tab.manager.TabIndexData;
import jp.hazuki.yuzubrowser.utils.WebViewUtils;
import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector;

public class LimitCacheWebView extends FrameLayout implements CustomWebView, TabCache.OnCacheOverFlowListener<TabData> {
    private final ArrayList<TabIndexData> tabIndexList = new ArrayList<>();
    private final LongSparseArray<Bundle> tabSaveData = new LongSparseArray<>();
    private final TabCache<TabData> tabCache;
    private TabData currentTab;
    private long id = System.currentTimeMillis();
    private int mCurrent = 0;
    private boolean isFirst = true;
    private View mTitleBar;
    private int layerType;
    private Paint layerPaint;
    private boolean acceptThirdPartyCookies;
    private OnWebStateChangeListener mStateChangeListener;
    private OnScrollChangedListener mOnScrollChangedListener;
    private DownloadListener mDownloadListener;
    private final DownloadListener mDownloadListenerWrapper = new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            synchronized (LimitCacheWebView.this) {
                if (mCurrent >= 1) {
                    TabData from = currentTab;
                    if (from.getUrl() == null || from.getUrl().equals(url)) {
                        tabIndexList.remove(mCurrent);
                        moveTo(false);

                        from.mWebView.destroy();
                    }
                }
            }
            if (mDownloadListener != null)
                mDownloadListener.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength);
        }
    };

    private final CustomWebChromeClientWrapper mWebChromeClientWrapper = new CustomWebChromeClientWrapper(this) {
        @Override
        public void onProgressChanged(CustomWebView view, int newProgress) {
            TabData data = webview2data(view);
            if (data != null) {
                data.onProgressChanged(newProgress);
            }
            if (!view.equals(currentTab.mWebView)) return;
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(CustomWebView view, String title) {
            TabData data = webview2data(view);
            if (data != null) {
                data.onReceivedTitle(title);
            }
            if (!view.equals(currentTab.mWebView)) return;
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onReceivedIcon(CustomWebView view, Bitmap icon) {
            if (!view.equals(currentTab.mWebView)) return;
            super.onReceivedIcon(view, icon);
        }
    };

    private final CustomWebViewClientWrapper mWebViewClientWrapper = new CustomWebViewClientWrapper(this) {
        @Override
        public void onScaleChanged(CustomWebView view, float oldScale, float newScale) {
            if (!view.equals(currentTab.mWebView)) return;
            super.onScaleChanged(view, oldScale, newScale);
        }

        @Override
        public void onUnhandledKeyEvent(CustomWebView view, KeyEvent event) {
            if (!view.equals(currentTab.mWebView)) return;
            super.onUnhandledKeyEvent(view, event);
        }

        @Override
        public void onPageFinished(CustomWebView view, String url) {
            TabData data = webview2data(view);
            if (data != null) {
                data.onPageFinished(view, url);
            }
            if (!view.equals(currentTab.mWebView)) return;
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(CustomWebView view, String url, Bitmap favicon) {
            TabData data = webview2data(view);
            if (data != null) {
                data.onPageStarted(url, favicon);
            }
            if (!view.equals(currentTab.mWebView)) return;
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(CustomWebView view, String url, Uri uri) {
            if (url == null || uri == null) return true;
            if (WebViewUtils.shouldLoadSameTabAuto(url)) return false;
            if (super.shouldOverrideUrlLoading(view, url, uri)) {
                return true;
            } else {
                if (WebViewUtils.isRedirect(view)) return false;
                if (view.getUrl() == null) return false;
                newtab(url);
                return true;
            }
        }
    };

    private CustomOnCreateContextMenuListener mCreateContextMenuListener;
    private final CustomOnCreateContextMenuListener mCreateContextMenuListenerWrapper = new CustomOnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, CustomWebView v, ContextMenuInfo menuInfo) {
            if (mCreateContextMenuListener != null)
                mCreateContextMenuListener.onCreateContextMenu(menu, (CustomWebView) LimitCacheWebView.this, menuInfo);
        }
    };
    private MultiTouchGestureDetector mGestureDetector;

    public LimitCacheWebView(Context context) {
        super(context);
        tabCache = new TabCache<>(AppData.fast_back_cache_size.get(), this);
        SwipeWebView web = new SwipeWebView(context);
        TabData data = new TabData(web);
        tabIndexList.add(data.getTabIndexData());
        tabCache.put(data.getId(), data);
        currentTab = data;

        addView(web);
    }

    private TabData webview2data(CustomWebView web) {
        return tabCache.get(web.getIdentityId());
    }

    private static final TreeMap<String, String> sHeaderMap = new TreeMap<>();

    private void newtab(String url) {
        newtab(url, sHeaderMap);
    }

    private void newtab(String url, Map<String, String> additionalHttpHeaders) {
        TabData to = makeWebView();
        if (additionalHttpHeaders == null)
            additionalHttpHeaders = sHeaderMap;
        additionalHttpHeaders.put("Referer", currentTab.getUrl());
        to.mWebView.loadUrl(url, sHeaderMap);

        for (int i = tabIndexList.size() - 1; i > mCurrent; --i) {
            removeWebView(i);
        }

        tabIndexList.add(to.getTabIndexData());
        tabCache.put(to.getId(), to);
        moveTo(true);
    }

    private TabData makeWebView() {
        TabData to = new TabData(new SwipeWebView(getContext()));
        settingWebView(currentTab.mWebView, to.mWebView);
        return to;
    }

    private void removeWebView(int index) {
        TabIndexData now = tabIndexList.remove(index);
        tabCache.remove(now.getId());
        tabSaveData.remove(now.getId());
    }

    private TabData getWebView(int index) {
        TabIndexData now = tabIndexList.get(index);
        TabData data = tabCache.get(now.getId());
        if (data == null) {
            data = now.getTabData(new SwipeWebView(getContext()));
            settingWebView(currentTab.mWebView, data.mWebView);
            Bundle state = tabSaveData.get(now.getId());
            if (state != null) {
                data.mWebView.restoreState(state);
            } else {
                if (now.getUrl() != null)
                    data.mWebView.loadUrl(now.getUrl());
            }
            tabCache.put(now.getId(), data);
        }
        return data;
    }

    private void setCurrentTab(int index, TabData data) {
        mCurrent = index;
        currentTab = data;
    }

    private TabData moveTo(boolean next) {
        mCurrent += next ? 1 : -1;

        TabData from = currentTab;
        currentTab = getWebView(mCurrent);
        removeAllViews();
        addView(currentTab.mWebView.getView());
        move(from, currentTab);
        return currentTab;
    }

    private static final int CAN_NOT_MOVE = 0;
    private static final int CAN_EXTERNAL_MOVE = 1;
    private static final int CAN_INTERNAL_MOVE = 2;

    private int canGoBackType() {
        if (currentTab.mWebView.canGoBack()) return CAN_INTERNAL_MOVE;
        if (mCurrent >= 1) return CAN_EXTERNAL_MOVE;
        return CAN_NOT_MOVE;
    }

    @Override
    public boolean canGoBack() {
        return canGoBackType() != CAN_NOT_MOVE;
    }

    @Override
    public synchronized boolean canGoBackOrForward(int steps) {
        if (steps == 0) return true;
        if (steps < 0) {
            return mCurrent >= -steps;
        } else {
            return mCurrent + steps < tabIndexList.size();
        }
    }

    private int canGoForwardType() {
        if (currentTab.mWebView.canGoForward()) return CAN_INTERNAL_MOVE;
        if (mCurrent + 1 < tabIndexList.size()) return CAN_EXTERNAL_MOVE;
        return CAN_NOT_MOVE;
    }

    @Override
    public boolean canGoForward() {
        return canGoForwardType() != CAN_NOT_MOVE;
    }

    @Override
    public void clearCache(boolean includeDiskFiles) {
        for (TabData web : tabCache.values()) {
            web.mWebView.clearCache(true);
        }
    }

    @Override
    public void clearFormData() {
        for (TabData web : tabCache.values()) {
            web.mWebView.clearFormData();
        }
    }

    @Override
    public synchronized void clearHistory() {
        TabData data = currentTab;
        data.mWebView.clearHistory();
        tabIndexList.clear();
        tabCache.clear();
        tabIndexList.add(data.getTabIndexData());
        tabCache.put(data.getId(), data);
        mCurrent = 0;
    }

    @Override
    public void clearMatches() {
        for (TabData web : tabCache.values()) {
            web.mWebView.clearMatches();
        }
    }

    @Override
    public CustomWebBackForwardList copyMyBackForwardList() {
        CustomWebBackForwardList list = new CustomWebBackForwardList(mCurrent, tabIndexList.size());
        for (TabIndexData webdata : tabIndexList) {
            TabData data = tabCache.get(webdata.getId());
            CustomWebHistoryItem item;
            if (data == null) {
                item = new CustomWebHistoryItem(webdata.getUrl(), webdata.getUrl(), webdata.getTitle(), null);
            } else {
                item = new CustomWebHistoryItem(data.getUrl(), data.getOriginalUrl(), data.getTitle(), data.mWebView.getFavicon());
            }
            list.add(item);
        }
        return list;
    }

    @Override
    public void destroy() {
        mTitleBar = null;
        for (TabData web : tabCache.values()) {
            web.mWebView.destroy();
        }
    }

    @Override
    public void findAllAsync(String find) {
        currentTab.mWebView.findAllAsync(find);
    }

    @Override
    public void setFindListener(WebView.FindListener listener) {
        currentTab.mWebView.setFindListener(listener);
    }

    @Override
    public void findNext(boolean forward) {
        currentTab.mWebView.findNext(forward);
    }

    @Override
    public void flingScroll(int vx, int vy) {
        currentTab.mWebView.flingScroll(vx, vy);
    }

    @Override
    public Bitmap getFavicon() {
        return currentTab.mWebView.getFavicon();
    }

    @Override
    public HitTestResult getHitTestResult() {
        return currentTab.mWebView.getHitTestResult();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String[] getHttpAuthUsernamePassword(String host, String realm) {
        return currentTab.mWebView.getHttpAuthUsernamePassword(host, realm);
    }

    @Override
    public String getOriginalUrl() {
        return currentTab.mWebView.getOriginalUrl();
    }

    @Override
    public int getProgress() {
        return currentTab.mWebView.getProgress();
    }

    @Override
    public WebSettings getSettings() {
        return currentTab.mWebView.getSettings();
    }

    @Override
    public String getTitle() {
        return currentTab.mWebView.getTitle();
    }

    @Override
    public String getUrl() {
        return currentTab.mWebView.getUrl();
    }

    @Override
    public synchronized void goBack() {
        TabData from = currentTab;
        switch (canGoBackType()) {
            default:
                break;
            case CAN_EXTERNAL_MOVE:
                moveTo(false);
                break;
            case CAN_INTERNAL_MOVE:
                from.mWebView.goBack();
                break;
        }
    }

    @Override
    public synchronized void goBackOrForward(int steps) {
        if (!canGoBackOrForward(steps)) return;

        removeAllViews();
        TabData from = currentTab;
        mCurrent += steps;
        currentTab = getWebView(mCurrent);
        addView(currentTab.mWebView.getView());
        move(from, currentTab);
    }

    @Override
    public synchronized void goForward() {
        TabData from = currentTab;
        switch (canGoForwardType()) {
            default:
                break;
            case CAN_EXTERNAL_MOVE:
                moveTo(true);
                break;
            case CAN_INTERNAL_MOVE:
                from.mWebView.goForward();
                break;
        }
    }

    @Override
    public void loadUrl(String url) {
        if (isFirst) {
            isFirst = false;
            currentTab.mWebView.loadUrl(url);
        } else if (WebViewUtils.shouldLoadSameTabUser(url)) {
            currentTab.mWebView.loadUrl(url);
        } else if (url != null) {
            newtab(url);
        }
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        if (isFirst) {
            isFirst = false;
            currentTab.mWebView.loadUrl(url, additionalHttpHeaders);
        } else if (WebViewUtils.shouldLoadSameTabUser(url)) {
            currentTab.mWebView.loadUrl(url, additionalHttpHeaders);
        } else if (url != null) {
            newtab(url, additionalHttpHeaders);
        }
    }

    @Override
    public void evaluateJavascript(String js, ValueCallback<String> callback) {
        currentTab.mWebView.evaluateJavascript(js, callback);
    }

    @Override
    public void onPause() {
        for (TabData web : tabCache.values()) {
            web.mWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        /*for(NormalWebView web:mList){
            web.onResume();
		}*/
        currentTab.mWebView.onResume();
    }

    @Override
    public boolean pageDown(boolean bottom) {
        return currentTab.mWebView.pageDown(bottom);
    }

    @Override
    public boolean pageUp(boolean top) {
        return currentTab.mWebView.pageUp(top);
    }

    @Override
    public void pauseTimers() {
        currentTab.mWebView.pauseTimers();
    }

    @Override
    public void reload() {
        currentTab.mWebView.reload();
    }

    @Override
    public void requestFocusNodeHref(Message hrefMsg) {
        currentTab.mWebView.requestFocusNodeHref(hrefMsg);
    }

    @Override
    public void requestImageRef(Message msg) {
        currentTab.mWebView.requestImageRef(msg);
    }

    private static final String BUNDLE_TAB_DATA = "FastBack.TAB_DATA";
    private static final String BUNDLE_WEB_NO = "FastBack.WEB_NO";
    private static final String BUNDLE_LOADED = "FastBack.LOADED_";
    private static final String BUNDLE_IS_FAST_BACK = "FastBack.IsFastBack";
    private static final String BUNDLE_CURRENT = "FastBack.WEB_CURRENT_COUNT";

    @Override
    public synchronized WebBackForwardList restoreState(Bundle inState) {
        isFirst = false;

        TabData from = currentTab;
        tabCache.clear();
        tabSaveData.clear();
        removeAllViews();

        mCurrent = inState.getInt(BUNDLE_CURRENT);
        String data = inState.getString(BUNDLE_TAB_DATA);
        loadIndexData(data);

        for (int i = 0; i < tabIndexList.size(); ++i) {
            TabIndexData indexData = tabIndexList.get(i);
            Bundle state = inState.getBundle(BUNDLE_WEB_NO + i);
            tabSaveData.put(indexData.getId(), state);

            if (inState.getBoolean(BUNDLE_LOADED + indexData.getId(), false)) {
                TabData web = indexData.getTabData(new SwipeWebView(getContext()));
                web.mWebView.onPause();
                tabCache.put(id, web);
                if (i == mCurrent) {
                    addView(web.mWebView.getView());
                    currentTab = web;
                }

                web.mWebView.restoreState(state);
                settingWebView(from.mWebView, web.mWebView);
            }
        }


        move(from, currentTab);
        return null;
    }

    @Override
    public void resumeTimers() {
        currentTab.mWebView.resumeTimers();
    }

    public static boolean isBundleFastBackWebView(Bundle state) {
        return state.getBoolean(BUNDLE_IS_FAST_BACK, false);
    }

    @Override
    public synchronized WebBackForwardList saveState(Bundle outState) {
        outState.putBoolean(BUNDLE_IS_FAST_BACK, true);
        outState.putInt(BUNDLE_CURRENT, mCurrent);
        outState.putString(BUNDLE_TAB_DATA, saveIndexData());

        for (TabData tabData : tabCache.values()) {
            final Bundle state = new Bundle();
            tabData.mWebView.saveState(state);
            tabSaveData.put(tabData.getId(), state);
            outState.putBoolean(BUNDLE_LOADED + tabData.getId(), true);
        }

        for (int i = 0; tabIndexList.size() > i; i++) {
            Bundle state = tabSaveData.get(tabIndexList.get(i).getId());
            outState.putBundle(BUNDLE_WEB_NO + i, state);
        }

        return null;
    }

    private static final String JSON_NAME_ID = "id";
    private static final String JSON_NAME_URL = "url";
    private static final String JSON_NAME_TITLE = "t";

    private String saveIndexData() {
        StringWriter writer = new StringWriter();
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonGenerator generator = jsonFactory.createGenerator(writer)) {
            generator.writeStartArray();
            for (TabIndexData data : tabIndexList) {
                generator.writeStartObject();
                generator.writeNumberField(JSON_NAME_ID, data.getId());
                generator.writeStringField(JSON_NAME_URL, data.getUrl());
                generator.writeStringField(JSON_NAME_TITLE, data.getTitle());
                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.flush();
            return writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void loadIndexData(String data) {
        tabIndexList.clear();
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(data)) {
            // 配列の処理
            if (parser.nextToken() == JsonToken.START_ARRAY) {
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    // 各オブジェクトの処理
                    if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                        long id = -1;
                        String url = null;
                        String title = null;
                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                            String name = parser.getCurrentName();
                            parser.nextToken();
                            if (name != null) {
                                switch (name) {
                                    case JSON_NAME_ID:
                                        id = parser.getLongValue();
                                        break;
                                    case JSON_NAME_URL:
                                        url = parser.getText();
                                        break;
                                    case JSON_NAME_TITLE:
                                        title = parser.getText();
                                    default:
                                        parser.skipChildren();
                                        break;
                                }
                            }
                        }
                        tabIndexList.add(new TabIndexData(url, title, 0, id, 0));
                    } else {
                        parser.skipChildren();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDownloadListener(DownloadListener listener) {
        mDownloadListener = listener;
        for (TabData web : tabCache.values()) {
            web.mWebView.setDownloadListener(mDownloadListenerWrapper);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setHttpAuthUsernamePassword(String host, String realm, String username, String password) {
        for (TabData web : tabCache.values()) {
            web.mWebView.setHttpAuthUsernamePassword(host, realm, username, password);
        }
    }

    @Override
    public void setNetworkAvailable(boolean networkUp) {
        for (TabData web : tabCache.values()) {
            web.mWebView.setNetworkAvailable(networkUp);
        }
    }

    @Override
    public void setMyWebChromeClient(CustomWebChromeClient client) {
        mWebChromeClientWrapper.setWebChromeClient(client);
        for (TabData web : tabCache.values()) {
            web.mWebView.setMyWebChromeClient(mWebChromeClientWrapper);
        }
    }

    @Override
    public void setMyWebViewClient(CustomWebViewClient client) {
        mWebViewClientWrapper.setWebViewClient(client);
        for (TabData web : tabCache.values()) {
            web.mWebView.setMyWebViewClient(mWebViewClientWrapper);
        }
    }

    @Override
    public void stopLoading() {
        currentTab.mWebView.stopLoading();
    }

    @Override
    public boolean zoomIn() {
        return currentTab.mWebView.zoomIn();
    }

    @Override
    public boolean zoomOut() {
        return currentTab.mWebView.zoomOut();
    }

    @Override
    public void setOnMyCreateContextMenuListener(CustomOnCreateContextMenuListener webContextMenuListener) {
        mCreateContextMenuListener = webContextMenuListener;
        for (TabData web : tabCache.values()) {
            web.mWebView.setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper);
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public WebView getWebView() {
        return currentTab.mWebView.getWebView();
    }

    @Override
    public void setSwipeEnable(boolean enable) {
        for (TabData web : tabCache.values()) {
            web.mWebView.setSwipeEnable(enable);
        }
    }

    @Override
    public boolean getSwipeEnable() {
        return currentTab.mWebView.getSwipeEnable();
    }

    @Override
    public void setGestureDetector(MultiTouchGestureDetector d) {
        mGestureDetector = d;
        for (TabData web : tabCache.values()) {
            web.mWebView.setGestureDetector(d);
        }
    }

    @Override
    public synchronized boolean setEmbeddedTitleBarMethod(View view) {
        for (TabData web : tabCache.values()) {
            web.mWebView.setEmbeddedTitleBarMethod(null);
        }
        mTitleBar = view;
        return currentTab.mWebView.setEmbeddedTitleBarMethod(view);
    }

    @Override
    public boolean notifyFindDialogDismissedMethod() {
        for (TabData web : tabCache.values()) {
            web.mWebView.notifyFindDialogDismissedMethod();
        }
        return true;
    }

    @Override
    public boolean setOverScrollModeMethod(int arg) {
        for (TabData web : tabCache.values()) {
            web.mWebView.setOverScrollModeMethod(arg);
        }
        return true;
    }

    @Override
    public int getOverScrollModeMethod() {
        return currentTab.mWebView.getOverScrollModeMethod();
    }

    @Override
    public void setOnCustomWebViewStateChangeListener(OnWebStateChangeListener l) {
        mStateChangeListener = l;
    }

    @Override
    public int computeVerticalScrollRangeMethod() {
        return currentTab.mWebView.computeVerticalScrollRangeMethod();
    }

    @Override
    public int computeVerticalScrollOffsetMethod() {
        return currentTab.mWebView.computeVerticalScrollOffsetMethod();
    }

    @Override
    public int computeVerticalScrollExtentMethod() {
        return currentTab.mWebView.computeVerticalScrollExtentMethod();
    }

    @Override
    public int computeHorizontalScrollRangeMethod() {
        return currentTab.mWebView.computeHorizontalScrollRangeMethod();
    }

    @Override
    public int computeHorizontalScrollOffsetMethod() {
        return currentTab.mWebView.computeHorizontalScrollOffsetMethod();
    }

    @Override
    public int computeHorizontalScrollExtentMethod() {
        return currentTab.mWebView.computeHorizontalScrollExtentMethod();
    }

    @Override
    public PrintDocumentAdapter createPrintDocumentAdapter(String documentName) {
        return currentTab.mWebView.createPrintDocumentAdapter(documentName);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        currentTab.mWebView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public long getIdentityId() {
        return id;
    }

    @Override
    public void setIdentityId(long identityId) {
        if (id > identityId)
            id = identityId;
    }

    @Override
    public void resetTheme() {
        for (TabData web : tabCache.values()) {
            web.mWebView.resetTheme();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        currentTab.mWebView.scrollTo(x, y);
    }

    @Override
    public void scrollBy(int x, int y) {
        currentTab.mWebView.scrollBy(x, y);
    }

    @Override
    public boolean saveWebArchiveMethod(String filename) {
        return currentTab.mWebView.saveWebArchiveMethod(filename);
    }

    private void move(TabData fromdata, TabData todata) {
        CustomWebView from = fromdata.mWebView;
        CustomWebView to = todata.mWebView;

        from.onPause();
        to.onResume();
        from.setEmbeddedTitleBarMethod(null);
        to.setEmbeddedTitleBarMethod(mTitleBar);
        from.setOnMyCreateContextMenuListener(null);
        to.setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper);
        from.setGestureDetector(null);
        to.setGestureDetector(mGestureDetector);
        from.setDownloadListener(null);
        to.setDownloadListener(mDownloadListenerWrapper);
        from.setMyOnScrollChangedListener(null);
        to.setMyOnScrollChangedListener(mOnScrollChangedListener);

        if (mStateChangeListener != null)
            mStateChangeListener.onStateChanged(this, todata);
        to.requestFocus();
    }

    private void settingWebView(CustomWebView from, CustomWebView to) {
        to.setMyWebChromeClient(mWebChromeClientWrapper);
        to.setMyWebViewClient(mWebViewClientWrapper);

        to.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        to.setOverScrollModeMethod(from.getOverScrollModeMethod());

        to.resetTheme();
        to.setSwipeEnable(from.getSwipeEnable());

        to.setLayerType(layerType, layerPaint);
        to.setAcceptThirdPartyCookies(CookieManager.getInstance(), acceptThirdPartyCookies);

        WebSettings from_setting = from.getSettings();
        WebSettings to_setting = to.getSettings();

        to_setting.setMinimumFontSize(from_setting.getMinimumFontSize());
        to_setting.setMinimumLogicalFontSize(from_setting.getMinimumLogicalFontSize());

        to_setting.setNeedInitialFocus(false);
        to_setting.setSupportMultipleWindows(from_setting.supportMultipleWindows());
        to_setting.setDefaultFontSize(from_setting.getDefaultFontSize());
        to_setting.setDefaultFixedFontSize(from_setting.getDefaultFixedFontSize());
        WebViewUtils.setTextSize(from_setting, to_setting);
        to_setting.setJavaScriptEnabled(from_setting.getJavaScriptEnabled());
        to_setting.setLoadsImagesAutomatically(from_setting.getLoadsImagesAutomatically());
        to_setting.setDatabaseEnabled(from_setting.getDatabaseEnabled());
        to_setting.setDomStorageEnabled(from_setting.getDomStorageEnabled());

        to_setting.setAllowContentAccess(from_setting.getAllowContentAccess());
        to_setting.setAllowFileAccess(from_setting.getAllowFileAccess());
        to_setting.setMixedContentMode(from_setting.getMixedContentMode());
        to_setting.setDefaultTextEncodingName(from_setting.getDefaultTextEncodingName());
        to_setting.setUserAgentString(from_setting.getUserAgentString());
        to_setting.setLoadWithOverviewMode(from_setting.getLoadWithOverviewMode());
        to_setting.setUseWideViewPort(from_setting.getUseWideViewPort());
        WebViewUtils.setDisplayZoomButtons(from_setting, to_setting);
        to_setting.setCacheMode(from_setting.getCacheMode());
        to_setting.setJavaScriptCanOpenWindowsAutomatically(from_setting.getJavaScriptCanOpenWindowsAutomatically());
        to_setting.setLayoutAlgorithm(from_setting.getLayoutAlgorithm());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            to_setting.setSafeBrowsingEnabled(from_setting.getSafeBrowsingEnabled());
        } else {
            //noinspection deprecation
            to_setting.setSaveFormData(from_setting.getSaveFormData());
        }

        //Unknown get
        to_setting.setAppCacheEnabled(AppData.web_app_cache.get());
        to_setting.setAppCachePath(BrowserManager.getAppCacheFilePath(getContext()));
        to_setting.setGeolocationEnabled(AppData.web_geolocation.get());
    }

    @Override
    public boolean isBackForwardListEmpty() {
        return isFirst || mCurrent == 0 && tabIndexList.size() == 1 && tabIndexList.get(0).getUrl() == null;
    }

    @Override
    public void setMyOnScrollChangedListener(OnScrollChangedListener l) {
        mOnScrollChangedListener = l;
        currentTab.mWebView.setMyOnScrollChangedListener(l);
    }

    @Override
    public void setLayerType(int layerType, @Nullable Paint paint) {
        this.layerType = layerType;
        layerPaint = paint;
        for (TabData web : tabCache.values()) {
            web.mWebView.setLayerType(layerType, paint);
        }
    }

    @Override
    public void onPreferenceReset() {
        tabCache.setSize(AppData.fast_back_cache_size.get());
    }

    @Override
    public void setAcceptThirdPartyCookies(CookieManager manager, boolean accept) {
        acceptThirdPartyCookies = accept;
        for (TabData web : tabCache.values()) {
            web.mWebView.setAcceptThirdPartyCookies(manager, accept);
        }
    }

    @Override
    public void setDoubleTapFling(boolean fling) {
        currentTab.mWebView.setDoubleTapFling(fling);
    }

    @Override
    public void onCacheOverflow(TabData tabData) {
        Bundle bundle = new Bundle();
        tabData.mWebView.saveState(bundle);
        tabSaveData.put(tabData.getId(), bundle);
        tabData.mWebView.setEmbeddedTitleBarMethod(null);
        tabData.mWebView.destroy();
    }
}
