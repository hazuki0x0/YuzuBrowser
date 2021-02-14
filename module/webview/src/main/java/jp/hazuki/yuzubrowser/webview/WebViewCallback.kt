/*
 * Copyright 2021 Hazuki
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

import android.webkit.JavascriptInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

internal class WebViewCallback(
    private val listener: OnWebViewListener
) {
    private val secretKey = Random.nextLong().toString(32)

    fun createInjectScript(): String {
        return "(function() {document.addEventListener('DOMContentLoaded'," +
            "function() {$INTERFACE_KEY.onDomContentLoaded(\"$secretKey\")})}())"
    }

    @JavascriptInterface
    fun onDomContentLoaded(secret: String) {
        if (secretKey != secret) return

        GlobalScope.launch(Dispatchers.Main) {
            listener.onDomContentLoaded()
        }
    }

    companion object {
        const val INTERFACE_KEY = "_momo_browser_interface"
    }

    interface OnWebViewListener {
        fun onDomContentLoaded()
    }
}
