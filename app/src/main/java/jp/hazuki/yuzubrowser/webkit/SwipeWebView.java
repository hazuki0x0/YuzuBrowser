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
import android.os.Bundle;
import android.os.Message;
import android.print.PrintDocumentAdapter;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Map;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.ThemeData;
import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector;

public class SwipeWebView extends SwipeRefreshLayout implements CustomWebView, SwipeRefreshLayout.OnRefreshListener, ScrollController.OnScrollEnable {
    private static final int TIMEOUT = 7500;

    private long id = System.currentTimeMillis();
    private NormalWebView webView;
    private ScrollController controller;
    private boolean enableSwipe = false;

    private final CustomWebChromeClientWrapper mWebChromeClientWrapper = new CustomWebChromeClientWrapper(this) {
        @Override
        public void onProgressChanged(CustomWebView view, int newProgress) {
            if (isRefreshing() && newProgress > 80)
                setRefreshing(false);

            super.onProgressChanged(view, newProgress);
        }
    };

    private final CustomWebViewClientWrapper mWebViewClientWrapper = new CustomWebViewClientWrapper(this) {
        @Override
        public void onPageFinished(CustomWebView view, String url) {
            controller.onPageChange();
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(CustomWebView view, String url, Bitmap favicon) {
            controller.onPageChange();
            super.onPageStarted(view, url, favicon);
        }
    };


    public SwipeWebView(Context context) {
        super(context);
        webView = new NormalWebView(context);
        controller = new ScrollController(this);
        webView.setScrollController(controller);
        addView(webView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setOnRefreshListener(this);
    }

    @Override
    public boolean canGoBack() {
        return webView.canGoBack();
    }

    @Override
    public boolean canGoBackOrForward(int steps) {
        return webView.canGoBackOrForward(steps);
    }

    @Override
    public boolean canGoForward() {
        return webView.canGoForward();
    }

    @Override
    public void clearCache(boolean includeDiskFiles) {
        webView.clearCache(includeDiskFiles);
    }

    @Override
    public void clearFormData() {
        webView.clearFormData();
    }

    @Override
    public void clearHistory() {
        webView.clearHistory();
    }

    @Override
    public void clearMatches() {
        webView.clearMatches();
    }

    @Override
    public CustomWebBackForwardList copyMyBackForwardList() {
        return webView.copyMyBackForwardList();
    }

    @Override
    public void destroy() {
        webView.destroy();
    }

    @Override
    public void findAllAsync(String find) {
        webView.findAllAsync(find);
    }

    @Override
    public void setFindListener(WebView.FindListener listener) {
        webView.setFindListener(listener);
    }

    @Override
    public void findNext(boolean forward) {
        webView.findNext(forward);
    }

    @Override
    public void flingScroll(int vx, int vy) {
        webView.flingScroll(vx, vy);
    }

    @Override
    public Bitmap getFavicon() {
        return webView.getFavicon();
    }

    @Override
    public WebView.HitTestResult getHitTestResult() {
        return webView.getHitTestResult();
    }

    @Override
    public String[] getHttpAuthUsernamePassword(String host, String realm) {
        return webView.getHttpAuthUsernamePassword(host, realm);
    }

    @Override
    public String getOriginalUrl() {
        return webView.getOriginalUrl();
    }

    @Override
    public int getProgress() {
        return webView.getProgress();
    }

    @Override
    public WebSettings getSettings() {
        return webView.getSettings();
    }

    @Override
    public String getTitle() {
        return webView.getTitle();
    }

    @Override
    public String getUrl() {
        return webView.getUrl();
    }

    @Override
    public void goBack() {
        webView.goBack();
    }

    @Override
    public void goBackOrForward(int steps) {
        webView.goBackOrForward(steps);
    }

    @Override
    public void goForward() {
        webView.goForward();
    }

    @Override
    public void loadUrl(String url) {
        webView.loadUrl(url);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        webView.loadUrl(url, additionalHttpHeaders);
    }

    @Override
    public void evaluateJavascript(String js, ValueCallback<String> callback) {
        webView.evaluateJavascript(js, callback);
    }

    @Override
    public void onPause() {
        webView.onPause();
    }

    @Override
    public void onResume() {
        webView.onResume();
    }

    @Override
    public boolean pageDown(boolean bottom) {
        return webView.pageDown(bottom);
    }

    @Override
    public boolean pageUp(boolean top) {
        return webView.pageUp(top);
    }

    @Override
    public void pauseTimers() {
        webView.pauseTimers();
    }

    @Override
    public void reload() {
        webView.reload();
    }

    @Override
    public void requestFocusNodeHref(Message hrefMsg) {
        webView.requestFocusNodeHref(hrefMsg);
    }

    @Override
    public void requestImageRef(Message msg) {
        webView.requestImageRef(msg);
    }

    @Override
    public WebBackForwardList restoreState(Bundle inState) {
        return webView.restoreState(inState);
    }

    @Override
    public void resumeTimers() {
        webView.resumeTimers();
    }

    @Override
    public WebBackForwardList saveState(Bundle outState) {
        return webView.saveState(outState);
    }

    @Override
    public void setDownloadListener(DownloadListener listener) {
        webView.setDownloadListener(listener);
    }

    @Override
    public void setHttpAuthUsernamePassword(String host, String realm, String username, String password) {
        webView.setHttpAuthUsernamePassword(host, realm, username, password);
    }

    @Override
    public void setNetworkAvailable(boolean networkUp) {
        webView.setNetworkAvailable(networkUp);
    }

    @Override
    public void setMyWebChromeClient(CustomWebChromeClient client) {
        mWebChromeClientWrapper.setWebChromeClient(client);
        webView.setMyWebChromeClient(mWebChromeClientWrapper);
    }

    @Override
    public void setMyWebViewClient(CustomWebViewClient client) {
        mWebViewClientWrapper.setWebViewClient(client);
        webView.setMyWebViewClient(mWebViewClientWrapper);
    }

    @Override
    public void stopLoading() {
        webView.stopLoading();
    }

    @Override
    public boolean zoomIn() {
        return webView.zoomIn();
    }

    @Override
    public boolean zoomOut() {
        return webView.zoomOut();
    }

    @Override
    public void setOnMyCreateContextMenuListener(CustomOnCreateContextMenuListener webContextMenuListener) {
        webView.setOnMyCreateContextMenuListener(webContextMenuListener);
    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {
        webView.scrollTo(x, y);
    }

    @Override
    public void scrollBy(@Px int x, @Px int y) {
        webView.scrollBy(x, y);
    }

    @Override
    public boolean saveWebArchiveMethod(String filename) {
        return webView.saveWebArchiveMethod(filename);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public WebView getWebView() {
        return webView;
    }

    @Override
    public void setSwipeEnable(boolean enable) {
        enableSwipe = enable;
        setEnabled(enable);
    }

    @Override
    public boolean getSwipeEnable() {
        return enableSwipe;
    }

    @Override
    public void setGestureDetector(MultiTouchGestureDetector d) {
        webView.setGestureDetector(d);
    }

    @Override
    public void setOnCustomWebViewStateChangeListener(OnWebStateChangeListener l) {
        webView.setOnCustomWebViewStateChangeListener(l);
    }

    @Override
    public boolean isBackForwardListEmpty() {
        return webView.isBackForwardListEmpty();
    }

    @Override
    public void setMyOnScrollChangedListener(OnScrollChangedListener l) {
        webView.setMyOnScrollChangedListener(l);
    }

    @Override
    public boolean setEmbeddedTitleBarMethod(View view) {
        return webView.setEmbeddedTitleBarMethod(view);
    }

    @Override
    public boolean notifyFindDialogDismissedMethod() {
        return webView.notifyFindDialogDismissedMethod();
    }

    @Override
    public boolean setOverScrollModeMethod(int arg) {
        return webView.setOverScrollModeMethod(arg);
    }

    @Override
    public int getOverScrollModeMethod() {
        return webView.getOverScrollMode();
    }

    @Override
    public int computeVerticalScrollRangeMethod() {
        return webView.computeVerticalScrollRangeMethod();
    }

    @Override
    public int computeVerticalScrollOffsetMethod() {
        return webView.computeVerticalScrollOffsetMethod();
    }

    @Override
    public int computeVerticalScrollExtentMethod() {
        return webView.computeVerticalScrollExtentMethod();
    }

    @Override
    public int computeHorizontalScrollRangeMethod() {
        return webView.computeHorizontalScrollRangeMethod();
    }

    @Override
    public int computeHorizontalScrollOffsetMethod() {
        return webView.computeHorizontalScrollOffsetMethod();
    }

    @Override
    public int computeHorizontalScrollExtentMethod() {
        return webView.computeHorizontalScrollExtentMethod();
    }

    @Override
    public PrintDocumentAdapter createPrintDocumentAdapter(String documentName) {
        return webView.createPrintDocumentAdapter(documentName);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        webView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
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
        if (ThemeData.isEnabled() && ThemeData.getInstance().progressColor != 0) {
            setColorSchemeColors(ThemeData.getInstance().progressColor);
            if (ThemeData.getInstance().refreshUseDark) {
                setProgressBackgroundColorSchemeColor(ResourcesCompat.getColor(getResources(), R.color.deep_gray, getContext().getTheme()));
            }
        } else {
            setColorSchemeResources(R.color.accent);
        }
    }

    @Override
    public void onRefresh() {
        webView.reload();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setRefreshing(false);
            }
        }, TIMEOUT);
    }

    @Override
    public void onScrollEnable(boolean enable) {
        setEnabled(enable);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enableSwipe && enabled);
    }

    @Override
    public void setLayerType(int layerType, @Nullable Paint paint) {
        webView.setLayerType(layerType, paint);
    }

    @Override
    public void onPreferenceReset() {
    }

    @Override
    public void setAcceptThirdPartyCookies(CookieManager manager, boolean accept) {
        webView.setAcceptThirdPartyCookies(manager, accept);
    }

    @Override
    public void setDoubleTapFling(boolean fling) {
        webView.setDoubleTapFling(fling);
    }
}