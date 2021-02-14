/*
 * Copyright (C) 2017-2021 Hazuki
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

import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import java.util.concurrent.Executor

class WebViewProxy private constructor() : Executor, Runnable {

    var requestState = 0
    var isConnected = false

    override fun execute(p0: Runnable?) {
        when (requestState) {
            1 -> isConnected = true
            2 -> isConnected = false
        }
        requestState = 0
    }

    override fun run() {}

    companion object {
        private var instance: WebViewProxy? = null

        private fun getInstance(): WebViewProxy {
            if (instance == null) instance = WebViewProxy()

            return instance!!
        }

        fun setProxy(host: String, httpsHost: String?) {
            try {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                    val config = ProxyConfig.Builder().apply {
                        if (httpsHost != null && httpsHost.isNotEmpty()) {
                            addProxyRule(host, ProxyConfig.MATCH_HTTP)
                            addProxyRule(httpsHost, ProxyConfig.MATCH_HTTPS)
                        } else {
                            addProxyRule(host)
                        }
                    }.build()

                    val instance = getInstance()
                    instance.requestState = 1
                    ProxyController.getInstance().setProxyOverride(config, instance, instance)
                }
            } catch (e: IllegalArgumentException) {
                ErrorReport.printAndWriteLog(e)
            }
        }

        fun clearProxy() {
            val instance = instance
            if (instance == null || (!instance.isConnected && instance.requestState == 0)) return

            if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                instance.requestState = 2
                ProxyController.getInstance().clearProxyOverride(instance, instance)
            }
        }
    }
}


