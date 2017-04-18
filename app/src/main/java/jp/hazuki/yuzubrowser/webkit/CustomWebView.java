package jp.hazuki.yuzubrowser.webkit;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.print.PrintDocumentAdapter;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Map;

import jp.hazuki.yuzubrowser.tab.manager.TabData;
import jp.hazuki.yuzubrowser.utils.view.MultiTouchGestureDetector;

public interface CustomWebView {
    boolean canGoBack();

    boolean canGoBackOrForward(int steps);

    boolean canGoForward();

    void clearCache(boolean includeDiskFiles);

    void clearFormData();

    void clearHistory();

    void clearMatches();

    CustomWebBackForwardList copyMyBackForwardList();

    void destroy();

    void findAllAsync(String find);

    void setFindListener(WebView.FindListener listener);

    void findNext(boolean forward);

    void flingScroll(int vx, int vy);

    Bitmap getFavicon();

    WebView.HitTestResult getHitTestResult();

    String[] getHttpAuthUsernamePassword(String host, String realm);

    String getOriginalUrl();

    int getProgress();

    WebSettings getSettings();

    String getTitle();

    String getUrl();

    void goBack();

    void goBackOrForward(int steps);

    void goForward();

    void loadUrl(String url);

    void loadUrl(String url, Map<String, String> additionalHttpHeaders);

    void evaluateJavascript(String js, ValueCallback<String> callback);

    void onPause();

    void onResume();

    boolean pageDown(boolean bottom);

    boolean pageUp(boolean top);

    void pauseTimers();

    void reload();

    boolean requestFocus();

    void requestFocusNodeHref(Message hrefMsg);

    void requestImageRef(Message msg);

    WebBackForwardList restoreState(Bundle inState);

    void resumeTimers();

    WebBackForwardList saveState(Bundle outState);

    void setDownloadListener(DownloadListener listener);

    void setHttpAuthUsernamePassword(String host, String realm, String username, String password);

    void setNetworkAvailable(boolean networkUp);

    void setScrollBarStyle(int style);

    void setMyWebChromeClient(CustomWebChromeClient client);

    void setMyWebViewClient(CustomWebViewClient client);

    void stopLoading();

    boolean zoomIn();

    boolean zoomOut();

    boolean hasFocus();

    void setOnMyCreateContextMenuListener(CustomOnCreateContextMenuListener webContextMenuListener);

    int getScrollX(); //this is View's final Method

    int getScrollY(); //this is View's final Method

    void scrollTo(int x, int y);

    void scrollBy(int x, int y);

    boolean saveWebArchiveMethod(String filename);

    View getView();

    WebView getWebView();

    void setSwipeEnable(boolean enable);

    //public void setGestureDetector(GestureDetector d);
    void setGestureDetector(MultiTouchGestureDetector d);

    void setOnCustomWebViewStateChangeListener(OnWebStateChangeListener l);

    boolean isBackForwardListEmpty();

    void setMyOnScrollChangedListener(OnScrollChangedListener l);

    boolean setEmbeddedTitleBarMethod(View view);

    boolean notifyFindDialogDismissedMethod();

    boolean setOverScrollModeMethod(int arg);

    int getOverScrollModeMethod();

    int computeVerticalScrollRangeMethod();

    int computeVerticalScrollOffsetMethod();

    int computeVerticalScrollExtentMethod();

    int computeHorizontalScrollRangeMethod();

    int computeHorizontalScrollOffsetMethod();

    int computeHorizontalScrollExtentMethod();

    PrintDocumentAdapter createPrintDocumentAdapter(String documentName);

    void loadDataWithBaseURL(String baseUrl, String data,
                             String mimeType, String encoding, String historyUrl);

    long getIdentityId();

    void setIdentityId(long id);

    void resetTheme();

    interface OnWebStateChangeListener {
        void onStateChanged(CustomWebView web, TabData tabdata);
    }

    interface OnScrollChangedListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }
}
