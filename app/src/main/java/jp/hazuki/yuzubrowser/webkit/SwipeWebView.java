package jp.hazuki.yuzubrowser.webkit;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.print.PrintDocumentAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
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

import java.util.Map;

import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector;

/**
 * Created by hazuki on 17/02/24.
 */

public class SwipeWebView extends SwipeRefreshLayout implements CustomWebView, SwipeRefreshLayout.OnRefreshListener {
    private static final int TIMEOUT = 7500;

    private long id = System.currentTimeMillis();
    private NormalWebView webView;

    private CustomWebChromeClient mWebChromeClient;
    private CustomWebViewClient mWebViewClient;
    private final CustomWebChromeClient mWebChromeClientWrapper = new CustomWebChromeClient() {
        @Override
        public void onReceivedTouchIconUrl(CustomWebView view, String url, boolean precomposed) {
            if (mWebChromeClient != null)
                mWebChromeClient.onReceivedTouchIconUrl(SwipeWebView.this, url, precomposed);
        }

        @Override
        public boolean onJsBeforeUnload(CustomWebView view, String url, String message, JsResult result) {
            return mWebChromeClient != null && mWebChromeClient.onJsBeforeUnload(SwipeWebView.this, url, message, result);
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (mWebChromeClient != null) return mWebChromeClient.getVideoLoadingProgressView();
            return null;
        }

        @Override
        public void onCloseWindow(CustomWebView window) {
            if (mWebChromeClient != null) mWebChromeClient.onCloseWindow(SwipeWebView.this);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return mWebChromeClient != null && mWebChromeClient.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onCreateWindow(CustomWebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            return mWebChromeClient != null && mWebChromeClient.onCreateWindow(SwipeWebView.this, isDialog, isUserGesture, resultMsg);
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            if (mWebChromeClient != null) mWebChromeClient.onGeolocationPermissionsHidePrompt();
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            if (mWebChromeClient != null)
                mWebChromeClient.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        @Override
        public void onHideCustomView() {
            if (mWebChromeClient != null) mWebChromeClient.onHideCustomView();
        }

        @Override
        public boolean onJsAlert(CustomWebView view, String url, String message, JsResult result) {
            return mWebChromeClient != null && mWebChromeClient.onJsAlert(SwipeWebView.this, url, message, result);
        }

        @Override
        public boolean onJsConfirm(CustomWebView view, String url, String message, JsResult result) {
            return mWebChromeClient != null && mWebChromeClient.onJsConfirm(SwipeWebView.this, url, message, result);
        }

        @Override
        public boolean onJsPrompt(CustomWebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return mWebChromeClient != null && mWebChromeClient.onJsPrompt(SwipeWebView.this, url, message, defaultValue, result);
        }

        @Override
        public void onProgressChanged(CustomWebView view, int newProgress) {
            if (isRefreshing() && newProgress > 80)
                setRefreshing(false);

            if (mWebChromeClient != null)
                mWebChromeClient.onProgressChanged(SwipeWebView.this, newProgress);
        }

        @Override
        public void onReceivedTitle(CustomWebView view, String title) {
            if (mWebChromeClient != null)
                mWebChromeClient.onReceivedTitle(SwipeWebView.this, title);
        }

        @Override
        public void onReceivedIcon(CustomWebView view, Bitmap icon) {
            if (mWebChromeClient != null)
                mWebChromeClient.onReceivedIcon(SwipeWebView.this, icon);
        }

        @Override
        public void onRequestFocus(CustomWebView view) {
            if (mWebChromeClient != null) mWebChromeClient.onRequestFocus(SwipeWebView.this);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (mWebChromeClient != null) mWebChromeClient.onShowCustomView(view, callback);
        }

        @Override
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            if (mWebChromeClient != null)
                mWebChromeClient.openFileChooser(uploadMsg, acceptType, capture);
        }

        @Override
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            if (mWebChromeClient != null) mWebChromeClient.openFileChooser(uploadMsg, acceptType);
        }

        @Override
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            if (mWebChromeClient != null) mWebChromeClient.openFileChooser(uploadMsg);
        }
    };
    private final CustomWebViewClient mWebViewClientWrapper = new CustomWebViewClient() {
        @Override
        public void onLoadResource(CustomWebView view, String url) {
            if (mWebViewClient != null)
                mWebViewClient.onLoadResource(SwipeWebView.this, url);
        }

        @Override
        public void onScaleChanged(CustomWebView view, float oldScale, float newScale) {
            if (mWebViewClient != null)
                mWebViewClient.onScaleChanged(SwipeWebView.this, oldScale, newScale);
        }

        @Override
        public void onUnhandledKeyEvent(CustomWebView view, KeyEvent event) {
            if (mWebViewClient != null)
                mWebViewClient.onUnhandledKeyEvent(SwipeWebView.this, event);
        }

        @Override
        public void doUpdateVisitedHistory(CustomWebView view, String url, boolean isReload) {
            if (mWebViewClient != null)
                mWebViewClient.doUpdateVisitedHistory(SwipeWebView.this, url, isReload);
        }

        @Override
        public void onFormResubmission(CustomWebView view, Message dontResend, Message resend) {
            if (mWebViewClient != null)
                mWebViewClient.onFormResubmission(SwipeWebView.this, dontResend, resend);
        }

        @Override
        public void onPageFinished(CustomWebView view, String url) {
            if (mWebViewClient != null) mWebViewClient.onPageFinished(SwipeWebView.this, url);
        }

        @Override
        public void onPageStarted(CustomWebView view, String url, Bitmap favicon) {
            if (mWebViewClient != null)
                mWebViewClient.onPageStarted(SwipeWebView.this, url, favicon);
        }

        @Override
        public void onReceivedHttpAuthRequest(CustomWebView view, HttpAuthHandler handler, String host, String realm) {
            if (mWebViewClient != null)
                mWebViewClient.onReceivedHttpAuthRequest(SwipeWebView.this, handler, host, realm);
        }

        @Override
        public void onReceivedSslError(CustomWebView view, SslErrorHandler handler, SslError error) {
            if (mWebViewClient != null)
                mWebViewClient.onReceivedSslError(SwipeWebView.this, handler, error);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(CustomWebView view, WebResourceRequest request) {
            if (mWebViewClient != null)
                return mWebViewClient.shouldInterceptRequest(SwipeWebView.this, request);
            return null;
        }

        @Override
        public boolean shouldOverrideUrlLoading(CustomWebView view, String url, Uri uri) {
            return mWebViewClient != null && mWebViewClient.shouldOverrideUrlLoading(SwipeWebView.this, url, uri);
        }
    };

    public SwipeWebView(Context context) {
        super(context);
        webView = new NormalWebView(context);
        addView(webView);

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
        mWebChromeClient = client;
        webView.setMyWebChromeClient(mWebChromeClientWrapper);
    }

    @Override
    public void setMyWebViewClient(CustomWebViewClient client) {
        mWebViewClient = client;
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
    public SwipeRefreshLayout getSwipeRefresh() {
        return this;
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
    public void onRefresh() {
        webView.reload();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setRefreshing(false);
            }
        }, TIMEOUT);
    }
}
