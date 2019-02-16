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
import android.net.Uri
import android.os.Message
import android.view.View
import android.webkit.*

open class CustomWebChromeClientWrapper(private val customWebView: CustomWebView) : CustomWebChromeClient() {

    private var mWebChromeClient: CustomWebChromeClient? = null

    override fun getVideoLoadingProgressView() = mWebChromeClient?.videoLoadingProgressView

    override fun onCloseWindow(web: CustomWebView) {
        mWebChromeClient?.onCloseWindow(customWebView)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage) = mWebChromeClient?.onConsoleMessage(consoleMessage) ?: false

    override fun onCreateWindow(view: CustomWebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message) =
            mWebChromeClient?.onCreateWindow(customWebView, isDialog, isUserGesture, resultMsg) ?: false

    override fun onGeolocationPermissionsHidePrompt() {
        mWebChromeClient?.onGeolocationPermissionsHidePrompt()
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        mWebChromeClient?.onGeolocationPermissionsShowPrompt(origin, callback)
    }

    override fun onHideCustomView() {
        mWebChromeClient?.onHideCustomView()
    }

    override fun onJsAlert(view: CustomWebView, url: String, message: String, result: JsResult): Boolean =
            mWebChromeClient?.onJsAlert(customWebView, url, message, result) ?: false

    override fun onJsConfirm(view: CustomWebView, url: String, message: String, result: JsResult): Boolean =
            mWebChromeClient?.onJsConfirm(customWebView, url, message, result) ?: false

    override fun onJsPrompt(view: CustomWebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean =
            mWebChromeClient?.onJsPrompt(customWebView, url, message, defaultValue, result) ?: false

    override fun onProgressChanged(web: CustomWebView, newProgress: Int) {
        mWebChromeClient?.onProgressChanged(customWebView, newProgress)
    }

    override fun onReceivedTitle(web: CustomWebView, title: String) {
        mWebChromeClient?.onReceivedTitle(customWebView, title)
    }

    override fun onReceivedIcon(web: CustomWebView, icon: Bitmap) {
        mWebChromeClient?.onReceivedIcon(customWebView, icon)
    }

    override fun onRequestFocus(web: CustomWebView) {
        mWebChromeClient?.onRequestFocus(customWebView)
    }

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        mWebChromeClient?.onShowCustomView(view, callback)
    }

    override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean =
            mWebChromeClient?.onShowFileChooser(webView, filePathCallback, fileChooserParams) ?: false

    override fun getVisitedHistory(callback: ValueCallback<Array<String>>) {
        mWebChromeClient?.getVisitedHistory(callback)
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        mWebChromeClient?.onPermissionRequest(request) ?: super.onPermissionRequest(request)
    }

    fun setWebChromeClient(mWebChromeClient: CustomWebChromeClient?) {
        this.mWebChromeClient = mWebChromeClient
    }
}
