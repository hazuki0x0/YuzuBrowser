/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.webview

import android.graphics.Bitmap
import android.os.Message
import android.view.View
import android.webkit.*

open class CustomWebChromeClient : WebChromeClient() {
    override fun getDefaultVideoPoster(): Bitmap? = null

    override fun getVideoLoadingProgressView(): View? = null

    override fun getVisitedHistory(callback: ValueCallback<Array<String>>) {}

    open fun onCloseWindow(web: CustomWebView) {}

    override fun onCloseWindow(window: WebView) {
        if (window is CustomWebView)
            onCloseWindow(window as CustomWebView)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean = false

    open fun onCreateWindow(view: CustomWebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean = false

    override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean =
            view is CustomWebView && onCreateWindow(view as CustomWebView, isDialog, isUserGesture, resultMsg)

    override fun onGeolocationPermissionsHidePrompt() {}

    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {}

    override fun onHideCustomView() {}

    open fun onJsAlert(view: CustomWebView, url: String, message: String, result: JsResult): Boolean = false

    override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean =
            view is CustomWebView && onJsAlert(view as CustomWebView, url, message, result)

    open fun onJsBeforeUnload(view: CustomWebView, url: String, message: String, result: JsResult): Boolean = false

    override fun onJsBeforeUnload(view: WebView, url: String, message: String, result: JsResult): Boolean =
            view is CustomWebView && onJsBeforeUnload(view as CustomWebView, url, message, result)

    open fun onJsConfirm(view: CustomWebView, url: String, message: String, result: JsResult): Boolean = false

    override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean =
            view is CustomWebView && onJsConfirm(view as CustomWebView, url, message, result)

    open fun onJsPrompt(view: CustomWebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean = false

    override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean =
            view is CustomWebView && onJsPrompt(view as CustomWebView, url, message, defaultValue, result)

    open fun onProgressChanged(web: CustomWebView, newProgress: Int) {}

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        if (view is CustomWebView)
            onProgressChanged(view as CustomWebView, newProgress)
    }

    open fun onReceivedIcon(web: CustomWebView, icon: Bitmap) {}

    override fun onReceivedIcon(view: WebView, icon: Bitmap) {
        if (view is CustomWebView)
            onReceivedIcon(view as CustomWebView, icon)
    }

    open fun onReceivedTitle(web: CustomWebView, title: String) {}

    override fun onReceivedTitle(view: WebView, title: String) {
        if (view is CustomWebView)
            onReceivedTitle(view as CustomWebView, title)
    }

    open fun onReceivedTouchIconUrl(view: CustomWebView, url: String, precomposed: Boolean) {}

    override fun onReceivedTouchIconUrl(view: WebView, url: String, precomposed: Boolean) {
        if (view is CustomWebView)
            onReceivedTouchIconUrl(view as CustomWebView, url, precomposed)
    }

    open fun onRequestFocus(web: CustomWebView) {}

    override fun onRequestFocus(view: WebView) {
        if (view is CustomWebView)
            onRequestFocus(view as CustomWebView)
    }

    override fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {}
}
