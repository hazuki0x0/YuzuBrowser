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

package jp.hazuki.yuzubrowser.webview.utility

import android.webkit.ValueCallback
import android.webkit.WebSettings
import jp.hazuki.yuzubrowser.webview.CustomWebView

inline fun CustomWebView.evaluateJavascript(js: String?, crossinline callback: ((String) -> Unit)) {
    evaluateJavascript(js, ValueCallback { callback(it) })
}

fun CustomWebView.getUserAgent(): String {
    val ua = webSettings.userAgentString
    return if (ua.isNullOrEmpty()) {
        WebSettings.getDefaultUserAgent(view.context)
    } else {
        ua
    }
}