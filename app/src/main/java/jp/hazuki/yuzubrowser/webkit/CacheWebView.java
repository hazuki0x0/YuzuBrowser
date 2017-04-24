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
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.print.PrintDocumentAdapter;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.tab.manager.TabData;
import jp.hazuki.yuzubrowser.utils.WebViewUtils;
import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector;

public class CacheWebView extends FrameLayout implements CustomWebView {
    private final ArrayList<TabData> mList = new ArrayList<>();
    private long id = System.currentTimeMillis();
    private int mCurrent = 0;
    private boolean isFirst = true;
    private View mTitleBar;
    private int layerType;
    private Paint layerPaint;
    private OnWebStateChangeListener mStateChangeListener;
    private OnScrollChangedListener mOnScrollChangedListener;
    private DownloadListener mDownloadListener;
    private final DownloadListener mDownloadListenerWrapper = new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            synchronized (CacheWebView.this) {
                if (mCurrent >= 1) {
                    TabData from = mList.get(mCurrent);
                    if (from.getUrl() == null || from.getUrl().equals(url)) {
                        mList.remove(mCurrent);

                        TabData to = mList.get(--mCurrent);
                        removeAllViews();
                        addView(to.mWebView.getView());
                        move(from, to);

                        from.mWebView.destroy();
                    }
                }
            }
            if (mDownloadListener != null)
                mDownloadListener.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength);
        }
    };
    private CustomWebChromeClient mWebChromeClient;
    private CustomWebViewClient mWebViewClient;
    private final CustomWebChromeClient mWebChromeClientWrapper = new CustomWebChromeClient() {
        @Override
        public View getVideoLoadingProgressView() {
            if (mWebChromeClient != null) return mWebChromeClient.getVideoLoadingProgressView();
            return null;
        }

        @Override
        public void onCloseWindow(CustomWebView window) {
            if (mWebChromeClient != null) mWebChromeClient.onCloseWindow(CacheWebView.this);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return mWebChromeClient != null && mWebChromeClient.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onCreateWindow(CustomWebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            return mWebChromeClient != null && mWebChromeClient.onCreateWindow(CacheWebView.this, isDialog, isUserGesture, resultMsg);
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            if (mWebChromeClient != null) mWebChromeClient.onGeolocationPermissionsHidePrompt();
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
            if (mWebChromeClient != null)
                mWebChromeClient.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        @Override
        public void onHideCustomView() {
            if (mWebChromeClient != null) mWebChromeClient.onHideCustomView();
        }

        @Override
        public boolean onJsAlert(CustomWebView view, String url, String message, JsResult result) {
            return mWebChromeClient != null && mWebChromeClient.onJsAlert(CacheWebView.this, url, message, result);
        }

        @Override
        public boolean onJsConfirm(CustomWebView view, String url, String message, JsResult result) {
            return mWebChromeClient != null && mWebChromeClient.onJsConfirm(CacheWebView.this, url, message, result);
        }

        @Override
        public boolean onJsPrompt(CustomWebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return mWebChromeClient != null && mWebChromeClient.onJsPrompt(CacheWebView.this, url, message, defaultValue, result);
        }

        @Override
        public void onProgressChanged(CustomWebView view, int newProgress) {
            TabData data = webview2data(view);
            if (data != null) {
                data.onProgressChanged(newProgress);
            }
            if (!view.equals(mList.get(mCurrent).mWebView)) return;
            if (mWebChromeClient != null)
                mWebChromeClient.onProgressChanged(CacheWebView.this, newProgress);
        }

        @Override
        public void onReceivedTitle(CustomWebView view, String title) {
            TabData data = webview2data(view);
            if (data != null) {
                data.onReceivedTitle(title);
            }
            if (!view.equals(mList.get(mCurrent).mWebView)) return;
            if (mWebChromeClient != null)
                mWebChromeClient.onReceivedTitle(CacheWebView.this, title);
        }

        @Override
        public void onReceivedIcon(CustomWebView view, Bitmap icon) {
            if (!view.equals(mList.get(mCurrent).mWebView)) return;
            if (mWebChromeClient != null)
                mWebChromeClient.onReceivedIcon(CacheWebView.this, icon);
        }

        @Override
        public void onRequestFocus(CustomWebView view) {
            if (mWebChromeClient != null) mWebChromeClient.onRequestFocus(CacheWebView.this);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (mWebChromeClient != null) mWebChromeClient.onShowCustomView(view, callback);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            return mWebChromeClient.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }
    };
    private final CustomWebViewClient mWebViewClientWrapper = new CustomWebViewClient() {
        @Override
        public void onLoadResource(CustomWebView view, String url) {
            if (mWebViewClient != null)
                mWebViewClient.onLoadResource(CacheWebView.this, url);
        }

        @Override
        public void onScaleChanged(CustomWebView view, float oldScale, float newScale) {
            if (!view.equals(mList.get(mCurrent).mWebView)) return;
            if (mWebViewClient != null)
                mWebViewClient.onScaleChanged(CacheWebView.this, oldScale, newScale);
        }

        @Override
        public void onUnhandledKeyEvent(CustomWebView view, KeyEvent event) {
            if (!view.equals(mList.get(mCurrent).mWebView)) return;
            if (mWebViewClient != null)
                mWebViewClient.onUnhandledKeyEvent(CacheWebView.this, event);
        }

        @Override
        public void doUpdateVisitedHistory(CustomWebView view, String url, boolean isReload) {
            if (mWebViewClient != null)
                mWebViewClient.doUpdateVisitedHistory(CacheWebView.this, url, isReload);
        }

        @Override
        public void onFormResubmission(CustomWebView view, Message dontResend, Message resend) {
            if (mWebViewClient != null)
                mWebViewClient.onFormResubmission(CacheWebView.this, dontResend, resend);
        }

        @Override
        public void onPageFinished(CustomWebView view, String url) {
            TabData data = webview2data(view);
            if (data != null) {
                data.onPageFinished(view, url);
            }
            if (!view.equals(mList.get(mCurrent).mWebView)) return;
            if (mWebViewClient != null) mWebViewClient.onPageFinished(CacheWebView.this, url);
        }

        @Override
        public void onPageStarted(CustomWebView view, String url, Bitmap favicon) {
            TabData data = webview2data(view);
            if (data != null) {
                data.onPageStarted(url, favicon);
            }
            if (!view.equals(mList.get(mCurrent).mWebView)) return;
            if (mWebViewClient != null)
                mWebViewClient.onPageStarted(CacheWebView.this, url, favicon);
        }

        @Override
        public void onReceivedHttpAuthRequest(CustomWebView view, HttpAuthHandler handler, String host, String realm) {
            if (mWebViewClient != null)
                mWebViewClient.onReceivedHttpAuthRequest(CacheWebView.this, handler, host, realm);
        }

        @Override
        public void onReceivedSslError(CustomWebView view, SslErrorHandler handler, SslError error) {
            if (mWebViewClient != null)
                mWebViewClient.onReceivedSslError(CacheWebView.this, handler, error);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(CustomWebView view, WebResourceRequest request) {
            if (mWebViewClient != null)
                return mWebViewClient.shouldInterceptRequest(CacheWebView.this, request);
            return null;
        }

        @Override
        public boolean shouldOverrideUrlLoading(CustomWebView view, String url, Uri uri) {
            if (url == null || uri == null) return true;
            if (WebViewUtils.shouldLoadSameTabAuto(url)) return false;
            if (mWebViewClient != null && mWebViewClient.shouldOverrideUrlLoading(CacheWebView.this, url, uri)) {
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
                mCreateContextMenuListener.onCreateContextMenu(menu, (CustomWebView) CacheWebView.this, menuInfo);
        }
    };
    private MultiTouchGestureDetector mGestureDetector;

    public CacheWebView(Context context) {
        super(context);
        SwipeWebView web = new SwipeWebView(context);
        mList.add(new TabData(web));
        addView(web);
    }

    private TabData webview2data(CustomWebView web) {
        for (TabData data : mList) {
            if (data.mWebView.equals(web))
                return data;
        }
        return null;
    }

    private static final TreeMap<String, String> sHeaderMap = new TreeMap<>();

    private void newtab(String url) {
        newtab(url, sHeaderMap);
    }

    private void newtab(String url, Map<String, String> additionalHttpHeaders) {
        TabData from = mList.get(mCurrent);
        TabData to = new TabData(new SwipeWebView(getContext()));
        for (int i = mList.size() - 1; i > mCurrent; --i) {
            mList.get(i).mWebView.destroy();
            mList.remove(i);
        }

        removeAllViews();
        mList.add(to);
        addView(to.mWebView.getView());
        settingWebView(from.mWebView, to.mWebView);
        if (additionalHttpHeaders == null)
            additionalHttpHeaders = sHeaderMap;
        additionalHttpHeaders.put("Referer", from.getUrl());
        to.mWebView.loadUrl(url, sHeaderMap);
        ++mCurrent;
        move(from, to);
    }

    private static final int CAN_NOT_MOVE = 0;
    private static final int CAN_EXTERNAL_MOVE = 1;
    private static final int CAN_INTERNAL_MOVE = 2;

    private int canGoBackType() {
        if (mList.get(mCurrent).mWebView.canGoBack()) return CAN_INTERNAL_MOVE;
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
            return mCurrent + steps < mList.size();
        }
    }

    private int canGoForwardType() {
        if (mList.get(mCurrent).mWebView.canGoForward()) return CAN_INTERNAL_MOVE;
        if (mCurrent + 1 < mList.size()) return CAN_EXTERNAL_MOVE;
        return CAN_NOT_MOVE;
    }

    @Override
    public boolean canGoForward() {
        return canGoForwardType() != CAN_NOT_MOVE;
    }

    @Override
    public void clearCache(boolean includeDiskFiles) {
        for (TabData web : mList) {
            web.mWebView.clearCache(true);
        }
    }

    @Override
    public void clearFormData() {
        for (TabData web : mList) {
            web.mWebView.clearFormData();
        }
    }

    @Override
    public synchronized void clearHistory() {
        TabData data = mList.get(mCurrent);
        data.mWebView.clearHistory();
        mList.clear();
        mList.add(data);
        mCurrent = 0;
    }

    @Override
    public void clearMatches() {
        for (TabData web : mList) {
            web.mWebView.clearMatches();
        }
    }

    @Override
    public CustomWebBackForwardList copyMyBackForwardList() {
        CustomWebBackForwardList list = new CustomWebBackForwardList(mCurrent, mList.size());
        for (TabData webdata : mList) {
            CustomWebView web = webdata.mWebView;
            CustomWebHistoryItem item = new CustomWebHistoryItem(web.getUrl(), web.getOriginalUrl(), web.getTitle(), web.getFavicon());
            list.add(item);
        }
        return list;
    }

    @Override
    public void destroy() {
        mTitleBar = null;
        for (TabData web : mList) {
            web.mWebView.destroy();
        }
    }

    @Override
    public void findAllAsync(String find) {
        mList.get(mCurrent).mWebView.findAllAsync(find);
    }

    @Override
    public void setFindListener(WebView.FindListener listener) {
        mList.get(mCurrent).mWebView.setFindListener(listener);
    }

    @Override
    public void findNext(boolean forward) {
        mList.get(mCurrent).mWebView.findNext(forward);
    }

    @Override
    public void flingScroll(int vx, int vy) {
        mList.get(mCurrent).mWebView.flingScroll(vx, vy);
    }

    @Override
    public Bitmap getFavicon() {
        return mList.get(mCurrent).mWebView.getFavicon();
    }

    @Override
    public HitTestResult getHitTestResult() {
        return mList.get(mCurrent).mWebView.getHitTestResult();
    }

    @Override
    public String[] getHttpAuthUsernamePassword(String host, String realm) {
        return mList.get(mCurrent).mWebView.getHttpAuthUsernamePassword(host, realm);
    }

    @Override
    public String getOriginalUrl() {
        return mList.get(mCurrent).mWebView.getOriginalUrl();
    }

    @Override
    public int getProgress() {
        return mList.get(mCurrent).mWebView.getProgress();
    }

    @Override
    public WebSettings getSettings() {
        return mList.get(mCurrent).mWebView.getSettings();
    }

    @Override
    public String getTitle() {
        return mList.get(mCurrent).mWebView.getTitle();
    }

    @Override
    public String getUrl() {
        return mList.get(mCurrent).mWebView.getUrl();
    }

    @Override
    public synchronized void goBack() {
        TabData from = mList.get(mCurrent);
        switch (canGoBackType()) {
            default:
                break;
            case CAN_EXTERNAL_MOVE:
                TabData to = mList.get(--mCurrent);
                removeAllViews();
                addView(to.mWebView.getView());
                move(from, to);
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
        TabData from = mList.get(mCurrent);
        mCurrent += steps;
        TabData to = mList.get(mCurrent);
        addView(to.mWebView.getView());
        move(from, to);
    }

    @Override
    public synchronized void goForward() {
        TabData from = mList.get(mCurrent);
        switch (canGoForwardType()) {
            default:
                break;
            case CAN_EXTERNAL_MOVE:
                TabData to = mList.get(++mCurrent);
                removeAllViews();
                addView(to.mWebView.getView());
                move(from, to);
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
            mList.get(0).mWebView.loadUrl(url);
        } else if (WebViewUtils.shouldLoadSameTabUser(url)) {
            mList.get(mCurrent).mWebView.loadUrl(url);
        } else if (url != null) {
            newtab(url);
        }
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        if (isFirst) {
            isFirst = false;
            mList.get(0).mWebView.loadUrl(url, additionalHttpHeaders);
        } else if (WebViewUtils.shouldLoadSameTabUser(url)) {
            mList.get(mCurrent).mWebView.loadUrl(url, additionalHttpHeaders);
        } else if (url != null) {
            newtab(url, additionalHttpHeaders);
        }
    }

    @Override
    public void evaluateJavascript(String js, ValueCallback<String> callback) {
        mList.get(mCurrent).mWebView.evaluateJavascript(js, callback);
    }

    @Override
    public void onPause() {
        for (TabData web : mList) {
            web.mWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        /*for(NormalWebView web:mList){
            web.onResume();
		}*/
        mList.get(mCurrent).mWebView.onResume();
    }

    @Override
    public boolean pageDown(boolean bottom) {
        return mList.get(mCurrent).mWebView.pageDown(bottom);
    }

    @Override
    public boolean pageUp(boolean top) {
        return mList.get(mCurrent).mWebView.pageUp(top);
    }

    @Override
    public void pauseTimers() {
        mList.get(mCurrent).mWebView.pauseTimers();
    }

    @Override
    public void reload() {
        mList.get(mCurrent).mWebView.reload();
    }

    @Override
    public void requestFocusNodeHref(Message hrefMsg) {
        mList.get(mCurrent).mWebView.requestFocusNodeHref(hrefMsg);
    }

    @Override
    public void requestImageRef(Message msg) {
        mList.get(mCurrent).mWebView.requestImageRef(msg);
    }

    @Override
    public synchronized WebBackForwardList restoreState(Bundle inState) {
        isFirst = false;

        TabData from = mList.get(mCurrent);
        mList.clear();
        removeAllViews();

        int all = inState.getInt("CacheWebView.WEB_ALL_COUNT");
        mCurrent = inState.getInt("CacheWebView.WEB_CURRENT_COUNT");

        for (int i = 0; i < all; ++i) {
            TabData web = new TabData(new SwipeWebView(getContext()));
            web.mWebView.onPause();
            mList.add(web);
            if (i == mCurrent)
                addView(web.mWebView.getView());
            web.mWebView.restoreState(inState.getBundle("CacheWebView.WEB_NO" + i));
            settingWebView(from.mWebView, web.mWebView);
        }
        move(from, mList.get(mCurrent));
        return null;
    }

    @Override
    public void resumeTimers() {
        mList.get(mCurrent).mWebView.resumeTimers();
    }

    public static boolean isBundleCacheWebView(Bundle state) {
        return state.getBoolean("CacheWebView.IsCacheWebView", false);
    }

    @Override
    public synchronized WebBackForwardList saveState(Bundle outState) {
        outState.putBoolean("CacheWebView.IsCacheWebView", true);
        outState.putInt("CacheWebView.WEB_ALL_COUNT", mList.size());
        outState.putInt("CacheWebView.WEB_CURRENT_COUNT", mCurrent);
        int i = 0;
        for (TabData web : mList) {
            final Bundle state = new Bundle();
            web.mWebView.saveState(state);
            outState.putBundle("CacheWebView.WEB_NO" + i, state);
            ++i;
        }
        return null;
    }

    @Override
    public void setDownloadListener(DownloadListener listener) {
        mDownloadListener = listener;
        for (TabData web : mList) {
            web.mWebView.setDownloadListener(mDownloadListenerWrapper);
        }
    }

    @Override
    public void setHttpAuthUsernamePassword(String host, String realm, String username, String password) {
        for (TabData web : mList) {
            web.mWebView.setHttpAuthUsernamePassword(host, realm, username, password);
        }
    }

    @Override
    public void setNetworkAvailable(boolean networkUp) {
        for (TabData web : mList) {
            web.mWebView.setNetworkAvailable(networkUp);
        }
    }

    @Override
    public void setMyWebChromeClient(CustomWebChromeClient client) {
        mWebChromeClient = client;
        /*mList.get(mCurrent).setMyWebChromeClient(mWebChromeClientWrapper);*/
        for (TabData web : mList) {
            web.mWebView.setMyWebChromeClient(mWebChromeClientWrapper);
        }
    }

    @Override
    public void setMyWebViewClient(CustomWebViewClient client) {
        mWebViewClient = client;
		/*mList.get(mCurrent).setMyWebViewClient(mWebViewClientWrapper);*/
        for (TabData web : mList) {
            web.mWebView.setMyWebViewClient(mWebViewClientWrapper);
        }
    }

    @Override
    public void stopLoading() {
        mList.get(mCurrent).mWebView.stopLoading();
    }

    @Override
    public boolean zoomIn() {
        return mList.get(mCurrent).mWebView.zoomIn();
    }

    @Override
    public boolean zoomOut() {
        return mList.get(mCurrent).mWebView.zoomOut();
    }

    @Override
    public void setOnMyCreateContextMenuListener(CustomOnCreateContextMenuListener webContextMenuListener) {
        mCreateContextMenuListener = webContextMenuListener;
		/*mList.get(mCurrent).setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper);*/
        for (TabData web : mList) {
            web.mWebView.setOnMyCreateContextMenuListener(mCreateContextMenuListenerWrapper);
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public WebView getWebView() {
        return mList.get(mCurrent).mWebView.getWebView();
    }

    @Override
    public void setSwipeEnable(boolean enable) {
        for (TabData web : mList) {
            web.mWebView.setSwipeEnable(enable);
        }
    }

    @Override
    public boolean getSwipeEnable() {
        return mList.get(mCurrent).mWebView.getSwipeEnable();
    }

    @Override
    public void setGestureDetector(MultiTouchGestureDetector d) {
        mGestureDetector = d;
        for (TabData web : mList) {
            web.mWebView.setGestureDetector(d);
        }
    }

    @Override
    public synchronized boolean setEmbeddedTitleBarMethod(View view) {
        for (TabData web : mList) {
            web.mWebView.setEmbeddedTitleBarMethod(null);
        }
        mTitleBar = view;
        return mList.get(mCurrent).mWebView.setEmbeddedTitleBarMethod(view);
    }

    @Override
    public boolean notifyFindDialogDismissedMethod() {
        for (TabData web : mList) {
            web.mWebView.notifyFindDialogDismissedMethod();
        }
        return true;
    }

    @Override
    public boolean setOverScrollModeMethod(int arg) {
        for (TabData web : mList) {
            web.mWebView.setOverScrollModeMethod(arg);
        }
        return true;
    }

    @Override
    public int getOverScrollModeMethod() {
        return mList.get(mCurrent).mWebView.getOverScrollModeMethod();
    }

    @Override
    public void setOnCustomWebViewStateChangeListener(OnWebStateChangeListener l) {
        mStateChangeListener = l;
    }

    @Override
    public int computeVerticalScrollRangeMethod() {
        return mList.get(mCurrent).mWebView.computeVerticalScrollRangeMethod();
    }

    @Override
    public int computeVerticalScrollOffsetMethod() {
        return mList.get(mCurrent).mWebView.computeVerticalScrollOffsetMethod();
    }

    @Override
    public int computeVerticalScrollExtentMethod() {
        return mList.get(mCurrent).mWebView.computeVerticalScrollExtentMethod();
    }

    @Override
    public int computeHorizontalScrollRangeMethod() {
        return mList.get(mCurrent).mWebView.computeHorizontalScrollRangeMethod();
    }

    @Override
    public int computeHorizontalScrollOffsetMethod() {
        return mList.get(mCurrent).mWebView.computeHorizontalScrollOffsetMethod();
    }

    @Override
    public int computeHorizontalScrollExtentMethod() {
        return mList.get(mCurrent).mWebView.computeHorizontalScrollExtentMethod();
    }

    @Override
    public PrintDocumentAdapter createPrintDocumentAdapter(String documentName) {
        return mList.get(mCurrent).mWebView.createPrintDocumentAdapter(documentName);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        mList.get(mCurrent).mWebView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
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
        for (TabData web : mList) {
            web.mWebView.resetTheme();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        mList.get(mCurrent).mWebView.scrollTo(x, y);
    }

    @Override
    public void scrollBy(int x, int y) {
        mList.get(mCurrent).mWebView.scrollBy(x, y);
    }

    @Override
    public boolean saveWebArchiveMethod(String filename) {
        return mList.get(mCurrent).mWebView.saveWebArchiveMethod(filename);
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

        to.setSwipeEnable(from.getSwipeEnable());

        to.setLayerType(layerType, layerPaint);

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
        to_setting.setSaveFormData(from_setting.getSaveFormData());
        to_setting.setLayoutAlgorithm(from_setting.getLayoutAlgorithm());

        //Unknown get
        to_setting.setAppCacheEnabled(AppData.web_app_cache.get());
        to_setting.setAppCachePath(BrowserManager.getAppCacheFilePath(getContext()));
        to_setting.setGeolocationEnabled(AppData.web_geolocation.get());
    }

    @Override
    public boolean isBackForwardListEmpty() {
        //return mList.size() == 1 && mList.get(0).mWebView.getUrl() == null;
        return mCurrent == 0 && mList.size() == 1;
    }

    @Override
    public void setMyOnScrollChangedListener(OnScrollChangedListener l) {
        mOnScrollChangedListener = l;
        mList.get(mCurrent).mWebView.setMyOnScrollChangedListener(l);
    }

    @Override
    public void setLayerType(int layerType, @Nullable Paint paint) {
        this.layerType = layerType;
        layerPaint = paint;
        for (TabData web : mList) {
            web.mWebView.setLayerType(layerType, paint);
        }
    }
}
