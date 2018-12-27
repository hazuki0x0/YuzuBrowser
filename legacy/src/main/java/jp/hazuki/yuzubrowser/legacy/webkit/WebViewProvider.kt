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

package jp.hazuki.yuzubrowser.legacy.webkit

import android.content.Context
import java.lang.ref.WeakReference

object WebViewProvider {
    private var cacheProvider: WeakReference<CachedWebViewProvider>? = null

    @JvmStatic
    fun getInstance(context: Context): NormalWebView {
        val providerRef = cacheProvider
        if (providerRef != null) {
            val provider = providerRef.get()
            if (provider != null) {
                return provider.getWebView()
            }
        }
        return NormalWebView(context)
    }

    @JvmStatic
    fun setProvider(provider: CachedWebViewProvider) {
        cacheProvider = WeakReference(provider)
    }

    interface CachedWebViewProvider {
        fun getWebView(): NormalWebView
    }
}