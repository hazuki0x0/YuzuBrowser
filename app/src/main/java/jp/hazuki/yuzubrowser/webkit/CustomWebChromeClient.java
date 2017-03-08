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
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class CustomWebChromeClient extends WebChromeClient {
    public Bitmap getDefaultVideoPoster() {
        return null;
    }

    public View getVideoLoadingProgressView() {
        return null;
    }

    public void getVisitedHistory(ValueCallback<String[]> callback) {
    }

    public void onCloseWindow(CustomWebView window) {
    }

    @Override
    public void onCloseWindow(WebView window) {
        if (window instanceof CustomWebView)
            onCloseWindow((CustomWebView) window);
    }

    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        return false;
    }

    public boolean onCreateWindow(CustomWebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        return false;
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        return view instanceof CustomWebView && onCreateWindow((CustomWebView) view, isDialog, isUserGesture, resultMsg);
    }

    public void onGeolocationPermissionsHidePrompt() {
    }

    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
    }

    public void onHideCustomView() {
    }

    public boolean onJsAlert(CustomWebView view, String url, String message, JsResult result) {
        return false;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return view instanceof CustomWebView && onJsAlert((CustomWebView) view, url, message, result);
    }

    public boolean onJsBeforeUnload(CustomWebView view, String url, String message, JsResult result) {
        return false;
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        return view instanceof CustomWebView && onJsBeforeUnload((CustomWebView) view, url, message, result);
    }

    public boolean onJsConfirm(CustomWebView view, String url, String message, JsResult result) {
        return false;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return view instanceof CustomWebView && onJsConfirm((CustomWebView) view, url, message, result);
    }

    public boolean onJsPrompt(CustomWebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return false;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return view instanceof CustomWebView && onJsPrompt((CustomWebView) view, url, message, defaultValue, result);
    }

    public void onProgressChanged(CustomWebView view, int newProgress) {
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (view instanceof CustomWebView)
            onProgressChanged((CustomWebView) view, newProgress);
    }

    public void onReceivedIcon(CustomWebView view, Bitmap icon) {
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        if (view instanceof CustomWebView)
            onReceivedIcon((CustomWebView) view, icon);
    }

    public void onReceivedTitle(CustomWebView view, String title) {
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        if (view instanceof CustomWebView)
            onReceivedTitle((CustomWebView) view, title);
    }

    public void onReceivedTouchIconUrl(CustomWebView view, String url, boolean precomposed) {
    }

    @Override
    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
        if (view instanceof CustomWebView)
            onReceivedTouchIconUrl((CustomWebView) view, url, precomposed);
    }

    public void onRequestFocus(CustomWebView view) {
    }

    @Override
    public void onRequestFocus(WebView view) {
        if (view instanceof CustomWebView)
            onRequestFocus((CustomWebView) view);
    }

    public void onShowCustomView(View view, CustomViewCallback callback) {
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        uploadMsg.onReceiveValue(null);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, final String acceptType) {
        uploadMsg.onReceiveValue(null);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        uploadMsg.onReceiveValue(null);
    }
}
