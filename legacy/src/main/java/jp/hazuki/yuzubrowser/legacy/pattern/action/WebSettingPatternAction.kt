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

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.pattern.PatternAction
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import java.io.IOException

class WebSettingPatternAction : PatternAction {

    var userAgentString: String? = null
        private set
    var javaScriptSetting: Int = UNDEFINED
        private set
    var navLock: Int = UNDEFINED
        private set
    var loadImage: Int = UNDEFINED
        private set
    var cookie = UNDEFINED
        private set
    var thirdCookie = UNDEFINED
        private set

    constructor(ua: String?, js: Int, navLock: Int, image: Int, cookie: Int, thirdCookie: Int) {
        userAgentString = ua
        javaScriptSetting = js
        this.navLock = navLock
        loadImage = image
        this.cookie = cookie
        this.thirdCookie = thirdCookie
    }

    @Throws(IOException::class)
    constructor(parser: JsonParser) {
        if (parser.nextToken() != JsonToken.START_OBJECT) return
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentToken != JsonToken.FIELD_NAME) return
            when (parser.currentName) {
                FIELD_NAME_UA -> {
                    if (parser.nextToken() != JsonToken.VALUE_STRING) return
                    userAgentString = parser.text
                }
                FIELD_NAME_JS -> {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                    javaScriptSetting = parser.intValue
                }
                FIELD_NAME_NAV_LOCK -> {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                    navLock = parser.intValue
                }
                FIELD_NAME_IMAGE -> {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                    loadImage = parser.intValue
                }
                FIELD_NAME_THIRD_COOKIE -> {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                    thirdCookie = parser.intValue
                }
                FIELD_NAME_COOKIE -> {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                    cookie = parser.intValue
                }
                else -> {
                    parser.skipChildren()
                }
            }
        }
    }

    override fun getTypeId(): Int {
        return PatternAction.WEB_SETTING
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.pattern_change_websettings)
    }

    @Throws(IOException::class)
    override fun write(generator: JsonGenerator): Boolean {
        generator.writeNumber(PatternAction.WEB_SETTING)
        generator.writeStartObject()
        if (userAgentString != null)
            generator.writeStringField(FIELD_NAME_UA, userAgentString)
        generator.writeNumberField(FIELD_NAME_JS, javaScriptSetting)
        generator.writeNumberField(FIELD_NAME_NAV_LOCK, navLock)
        generator.writeNumberField(FIELD_NAME_IMAGE, loadImage)
        generator.writeNumberField(FIELD_NAME_THIRD_COOKIE, thirdCookie)
        generator.writeNumberField(FIELD_NAME_COOKIE, cookie)
        generator.writeEndObject()
        return true
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun run(context: Context, tab: MainTabData, url: String): Boolean {
        val settings = tab.mWebView.webSettings

        if (userAgentString != null)
            settings.userAgentString = userAgentString

        when (javaScriptSetting) {
            ENABLE -> settings.javaScriptEnabled = true
            DISABLE -> settings.javaScriptEnabled = false
        }

        when (navLock) {
            ENABLE -> tab.isNavLock = true
            DISABLE -> tab.isNavLock = false
        }

        when (loadImage) {
            ENABLE -> settings.loadsImagesAutomatically = true
            DISABLE -> settings.loadsImagesAutomatically = false
        }

        when (cookie) {
            ENABLE -> tab.cookieMode = MainTabData.COOKIE_ENABLE
            DISABLE -> tab.cookieMode = MainTabData.COOKIE_DISABLE
        }
        CookieManager.getInstance().setAcceptCookie(tab.isEnableCookie)

        when (thirdCookie) {
            ENABLE -> CookieManager.getInstance().setAcceptThirdPartyCookies(tab.mWebView.webView, true)
            DISABLE -> CookieManager.getInstance().setAcceptThirdPartyCookies(tab.mWebView.webView, false)
        }
        return false
    }

    companion object {
        const val UNDEFINED = 0
        const val ENABLE = 1
        const val DISABLE = 2

        private const val FIELD_NAME_UA = "0"
        private const val FIELD_NAME_JS = "1"
        private const val FIELD_NAME_NAV_LOCK = "2"
        private const val FIELD_NAME_IMAGE = "3"
        private const val FIELD_NAME_THIRD_COOKIE = "4"
        private const val FIELD_NAME_COOKIE = "5"
    }
}
