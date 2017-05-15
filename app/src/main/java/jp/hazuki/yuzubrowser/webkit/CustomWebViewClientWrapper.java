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
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

class CustomWebViewClientWrapper extends CustomWebViewClient {

    private CustomWebViewClient mWebViewClient;
    private CustomWebView customWebView;

    CustomWebViewClientWrapper(CustomWebView webView) {
        this.customWebView = webView;
    }

    @Override
    public void onLoadResource(CustomWebView view, String url) {
        if (mWebViewClient != null)
            mWebViewClient.onLoadResource(customWebView, url);
    }

    @Override
    public void onScaleChanged(CustomWebView view, float oldScale, float newScale) {
        if (mWebViewClient != null)
            mWebViewClient.onScaleChanged(customWebView, oldScale, newScale);
    }

    @Override
    public void onUnhandledKeyEvent(CustomWebView view, KeyEvent event) {
        if (mWebViewClient != null)
            mWebViewClient.onUnhandledKeyEvent(customWebView, event);
    }

    @Override
    public void doUpdateVisitedHistory(CustomWebView view, String url, boolean isReload) {
        if (mWebViewClient != null)
            mWebViewClient.doUpdateVisitedHistory(customWebView, url, isReload);
    }

    @Override
    public void onFormResubmission(CustomWebView view, Message dontResend, Message resend) {
        if (mWebViewClient != null)
            mWebViewClient.onFormResubmission(customWebView, dontResend, resend);
    }

    @Override
    public void onPageFinished(CustomWebView view, String url) {
        if (mWebViewClient != null) mWebViewClient.onPageFinished(customWebView, url);
    }

    @Override
    public void onPageStarted(CustomWebView view, String url, Bitmap favicon) {
        if (mWebViewClient != null)
            mWebViewClient.onPageStarted(customWebView, url, favicon);
    }

    @Override
    public void onReceivedHttpAuthRequest(CustomWebView view, HttpAuthHandler handler, String host, String realm) {
        if (mWebViewClient != null)
            mWebViewClient.onReceivedHttpAuthRequest(customWebView, handler, host, realm);
    }

    @Override
    public void onReceivedSslError(CustomWebView view, SslErrorHandler handler, SslError error) {
        if (mWebViewClient != null)
            mWebViewClient.onReceivedSslError(customWebView, handler, error);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(CustomWebView view, WebResourceRequest request) {
        if (mWebViewClient != null)
            return mWebViewClient.shouldInterceptRequest(customWebView, request);
        return null;
    }

    @Override
    public boolean shouldOverrideUrlLoading(CustomWebView view, String url, Uri uri) {
        return mWebViewClient != null && mWebViewClient.shouldOverrideUrlLoading(customWebView, url, uri);
    }

    void setWebViewClient(CustomWebViewClient webViewClient) {
        this.mWebViewClient = webViewClient;
    }
}
