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

package jp.hazuki.yuzubrowser.legacy.webkit

import android.os.Handler
import jp.hazuki.yuzubrowser.webview.CustomWebView

class WebViewAutoScrollManager {

    private var isRunning = false
    private var init = false
    private var scrollSpeed: Double = 0.0
    private var scrollY: Double = 0.0
    private var scrollMax: Int = 0
    private val handler: Handler = Handler()
    private var onStopListener: (() -> Unit)? = null

    fun start(webView: CustomWebView, speed: Int) {
        scrollSpeed = speed * 0.01
        isRunning = true
        init = true
        scrollY = webView.computeVerticalScrollOffsetMethod().toDouble()
        scrollMax = webView.computeVerticalScrollRangeMethod() - webView.computeVerticalScrollExtentMethod()

        handler.postDelayed({ init = false }, 200)

        val runScroll = Runnable {
            scrollY += scrollSpeed
            if (scrollY > scrollMax) {
                scrollY = scrollMax.toDouble()
                stop()
            }
            webView.scrollTo(webView.webScrollX, scrollY.toInt())
        }

        Thread(Runnable {
            while (isRunning) {
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                handler.post(runScroll)
            }
        }).start()
    }

    fun stop() {
        if (init) return
        isRunning = false
        onStopListener?.invoke()
    }

    fun setOnStopListener(listener: (() -> Unit)?) {
        onStopListener = listener
    }
}
