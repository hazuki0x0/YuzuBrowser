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
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomWebViewClient extends WebViewClient {
    public void doUpdateVisitedHistory(CustomWebView view, String url, boolean isReload) {
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        if (view instanceof CustomWebView)
            doUpdateVisitedHistory((CustomWebView) view, url, isReload);
    }

    public void onFormResubmission(CustomWebView view, Message dontResend, Message resend) {
        dontResend.sendToTarget();
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        if (view instanceof CustomWebView)
            onFormResubmission((CustomWebView) view, dontResend, resend);
        else
            dontResend.sendToTarget();
    }

    public void onLoadResource(CustomWebView view, String url) {
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (view instanceof CustomWebView)
            onLoadResource((CustomWebView) view, url);
    }

    public void onPageFinished(CustomWebView view, String url) {
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (view instanceof CustomWebView)
            onPageFinished((CustomWebView) view, url);
    }

    public void onPageStarted(CustomWebView view, String url, Bitmap favicon) {
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (view instanceof CustomWebView)
            onPageStarted((CustomWebView) view, url, favicon);
    }

    public void onReceivedHttpAuthRequest(CustomWebView view, HttpAuthHandler handler, String host, String realm) {
        handler.cancel();
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        if (view instanceof CustomWebView)
            onReceivedHttpAuthRequest((CustomWebView) view, handler, host, realm);
        else
            handler.cancel();
    }

    public void onReceivedLoginRequest(CustomWebView view, String realm, String account, String args) {
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        if (view instanceof CustomWebView)
            onReceivedLoginRequest((CustomWebView) view, realm, account, args);
    }

    public void onReceivedSslError(CustomWebView view, SslErrorHandler handler, SslError error) {
        handler.cancel();
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if (view instanceof CustomWebView)
            onReceivedSslError((CustomWebView) view, handler, error);
        else
            handler.cancel();
    }

    public void onScaleChanged(CustomWebView view, float oldScale, float newScale) {
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        if (view instanceof CustomWebView)
            onScaleChanged((CustomWebView) view, oldScale, newScale);
    }

    public void onUnhandledKeyEvent(CustomWebView view, KeyEvent event) {
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        if (view instanceof CustomWebView)
            onUnhandledKeyEvent((CustomWebView) view, event);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (view instanceof CustomWebView)
            return shouldInterceptRequest((CustomWebView) view, request);
        else
            return null;
    }

    public WebResourceResponse shouldInterceptRequest(CustomWebView view, WebResourceRequest request) {
        return null;
    }

    public boolean shouldOverrideKeyEvent(CustomWebView view, KeyEvent event) {
        return false;
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        if (view instanceof CustomWebView)
            return shouldOverrideKeyEvent((CustomWebView) view, event);
        else
            return false;
    }

    public boolean shouldOverrideUrlLoading(CustomWebView view, String url, Uri uri) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (view instanceof CustomWebView)
            return shouldOverrideUrlLoading((CustomWebView) view, url, Uri.parse(url));
        else
            return false;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (view instanceof CustomWebView)
            return shouldOverrideUrlLoading((CustomWebView) view, request.getUrl().toString(), request.getUrl());
        else
            return false;
    }
}
