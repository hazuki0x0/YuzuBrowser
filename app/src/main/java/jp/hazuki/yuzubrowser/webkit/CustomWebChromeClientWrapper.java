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
import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebView;

class CustomWebChromeClientWrapper extends CustomWebChromeClient {

    private CustomWebChromeClient mWebChromeClient;
    private CustomWebView customWebView;

    CustomWebChromeClientWrapper(CustomWebView webView) {
        this.customWebView = webView;
    }

    @Override
    public View getVideoLoadingProgressView() {
        if (mWebChromeClient != null) return mWebChromeClient.getVideoLoadingProgressView();
        return null;
    }

    @Override
    public void onCloseWindow(CustomWebView window) {
        if (mWebChromeClient != null) mWebChromeClient.onCloseWindow(customWebView);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        return mWebChromeClient != null && mWebChromeClient.onConsoleMessage(consoleMessage);
    }

    @Override
    public boolean onCreateWindow(CustomWebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        return mWebChromeClient != null && mWebChromeClient.onCreateWindow(customWebView, isDialog, isUserGesture, resultMsg);
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
        return mWebChromeClient != null && mWebChromeClient.onJsAlert(customWebView, url, message, result);
    }

    @Override
    public boolean onJsConfirm(CustomWebView view, String url, String message, JsResult result) {
        return mWebChromeClient != null && mWebChromeClient.onJsConfirm(customWebView, url, message, result);
    }

    @Override
    public boolean onJsPrompt(CustomWebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return mWebChromeClient != null && mWebChromeClient.onJsPrompt(customWebView, url, message, defaultValue, result);
    }

    @Override
    public void onProgressChanged(CustomWebView view, int newProgress) {
        if (mWebChromeClient != null)
            mWebChromeClient.onProgressChanged(customWebView, newProgress);
    }

    @Override
    public void onReceivedTitle(CustomWebView view, String title) {
        if (mWebChromeClient != null)
            mWebChromeClient.onReceivedTitle(customWebView, title);
    }

    @Override
    public void onReceivedIcon(CustomWebView view, Bitmap icon) {
        if (mWebChromeClient != null)
            mWebChromeClient.onReceivedIcon(customWebView, icon);
    }

    @Override
    public void onRequestFocus(CustomWebView view) {
        if (mWebChromeClient != null) mWebChromeClient.onRequestFocus(customWebView);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (mWebChromeClient != null) mWebChromeClient.onShowCustomView(view, callback);
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        return mWebChromeClient != null && mWebChromeClient.onShowFileChooser(webView, filePathCallback, fileChooserParams);
    }

    @Override
    public void getVisitedHistory(ValueCallback<String[]> callback) {
        if (mWebChromeClient != null)
            mWebChromeClient.getVisitedHistory(callback);
    }

    void setWebChromeClient(CustomWebChromeClient mWebChromeClient) {
        this.mWebChromeClient = mWebChromeClient;
    }
}
