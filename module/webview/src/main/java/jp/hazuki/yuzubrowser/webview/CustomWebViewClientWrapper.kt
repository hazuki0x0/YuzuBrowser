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
import android.net.http.SslError
import android.os.Message
import android.view.KeyEvent
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse

open class CustomWebViewClientWrapper(private val customWebView: CustomWebView) : CustomWebViewClient() {

    private var webViewClient: CustomWebViewClient? = null

    override fun onLoadResource(web: CustomWebView, url: String) {
        webViewClient?.onLoadResource(customWebView, url)
    }

    override fun onScaleChanged(view: CustomWebView, oldScale: Float, newScale: Float) {
        webViewClient?.onScaleChanged(customWebView, oldScale, newScale)
    }

    override fun onUnhandledKeyEvent(view: CustomWebView, event: KeyEvent) {
        webViewClient?.onUnhandledKeyEvent(customWebView, event)
    }

    override fun doUpdateVisitedHistory(web: CustomWebView, url: String, isReload: Boolean) {
        webViewClient?.doUpdateVisitedHistory(customWebView, url, isReload)
    }

    override fun onFormResubmission(web: CustomWebView, dontResend: Message, resend: Message) {
        webViewClient?.onFormResubmission(customWebView, dontResend, resend)
    }

    override fun onPageFinished(web: CustomWebView, url: String) {
        webViewClient?.onPageFinished(customWebView, url)
    }

    override fun onPageStarted(web: CustomWebView, url: String, favicon: Bitmap?) {
        webViewClient?.onPageStarted(customWebView, url, favicon)
    }

    override fun onPageChanged(web: CustomWebView, url: String, originalUrl: String, progress: Int, isLoading: Boolean) {
        webViewClient?.onPageChanged(web, url, originalUrl, progress, isLoading)
    }

    override fun onReceivedHttpAuthRequest(web: CustomWebView, handler: HttpAuthHandler, host: String, realm: String) {
        webViewClient?.onReceivedHttpAuthRequest(customWebView, handler, host, realm)
    }

    override fun onReceivedSslError(web: CustomWebView, handler: SslErrorHandler, error: SslError) {
        webViewClient?.onReceivedSslError(customWebView, handler, error)
    }

    override fun shouldInterceptRequest(web: CustomWebView, request: WebResourceRequest): WebResourceResponse? =
            webViewClient?.shouldInterceptRequest(customWebView, request)

    override fun shouldOverrideUrlLoading(web: CustomWebView, url: String, uri: Uri): Boolean =
            webViewClient?.shouldOverrideUrlLoading(customWebView, url, uri) ?: false

    override fun onPageCommitVisible(web: CustomWebView, url: String) {
        webViewClient?.onPageCommitVisible(customWebView, url)
    }


    fun setWebViewClient(webViewClient: CustomWebViewClient?) {
        this.webViewClient = webViewClient
    }
}
