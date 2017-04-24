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

import android.graphics.Bitmap;
import android.graphics.Paint;
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

    boolean getSwipeEnable();

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

    void setLayerType(int layerType, Paint paint);

    interface OnWebStateChangeListener {
        void onStateChanged(CustomWebView web, TabData tabdata);
    }

    interface OnScrollChangedListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }
}
