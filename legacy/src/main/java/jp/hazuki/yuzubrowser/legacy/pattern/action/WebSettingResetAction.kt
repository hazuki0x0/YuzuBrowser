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

package jp.hazuki.yuzubrowser.legacy.pattern.action

import android.webkit.CookieManager
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData

class WebSettingResetAction(tabData: MainTabData) {
    private val userAgent: String?
    private val javaScriptEnable: Boolean
    private val navLockEnable: Boolean
    private val loadImage: Boolean
    private val thirdCookie: Boolean
    private val renderingMode: Int
    var patternAction: WebSettingPatternAction? = null

    init {
        val settings = tabData.mWebView.webSettings
        userAgent = settings.userAgentString
        javaScriptEnable = settings.javaScriptEnabled
        navLockEnable = tabData.isNavLock
        loadImage = settings.loadsImagesAutomatically
        thirdCookie = CookieManager.getInstance().acceptThirdPartyCookies(tabData.mWebView.webView)
        renderingMode = tabData.mWebView.renderingMode
    }

    fun reset(tab: MainTabData) {
        val settings = tab.mWebView.webSettings

        settings.userAgentString = userAgent
        settings.javaScriptEnabled = javaScriptEnable
        tab.isNavLock = navLockEnable
        settings.loadsImagesAutomatically = loadImage
        tab.cookieMode = MainTabData.COOKIE_UNDEFINED
        CookieManager.getInstance().run {
            setAcceptCookie(tab.isEnableCookie)
            setAcceptThirdPartyCookies(tab.mWebView.webView, thirdCookie)
        }
        tab.renderingMode = renderingMode
    }
}
