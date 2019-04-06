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

import android.os.Build
import android.webkit.WebSettings
import androidx.annotation.RequiresApi

class YuzuWebSettings(private val origin: WebSettings) {

    var appCacheEnabled = false
        set(flag) {
            origin.setAppCacheEnabled(flag)
            field = flag
        }
    var geolocationEnabled = false
        set(flag) {
            origin.setGeolocationEnabled(flag)
            field = flag
        }

    var mediaPlaybackRequiresUserGesture: Boolean
        get() = origin.mediaPlaybackRequiresUserGesture
        set(require) {
            origin.mediaPlaybackRequiresUserGesture = require
        }

    var allowFileAccess: Boolean
        get() = origin.allowFileAccess
        set(allow) {
            origin.allowFileAccess = allow
        }

    var allowContentAccess: Boolean
        get() = origin.allowContentAccess
        set(allow) {
            origin.allowContentAccess = allow
        }

    var loadWithOverviewMode: Boolean
        get() = origin.loadWithOverviewMode
        set(overview) {
            origin.loadWithOverviewMode = overview
        }

    var saveFormData: Boolean
        @Deprecated("")
        get() = origin.saveFormData
        @Deprecated("")
        set(save) {
            origin.saveFormData = save
        }

    var savePassword: Boolean
        @Suppress("DEPRECATION")
        @Deprecated("")
        get() = origin.savePassword
        @Suppress("DEPRECATION")
        @Deprecated("")
        set(save) {
            origin.savePassword = save
        }

    var textZoom: Int
        get() = origin.textZoom
        set(textZoom) {
            origin.textZoom = textZoom
        }

    var lightTouchEnabled: Boolean
        @Suppress("DEPRECATION")
        @Deprecated("")
        get() = origin.lightTouchEnabled
        @Suppress("DEPRECATION")
        @Deprecated("")
        set(enabled) {
            origin.lightTouchEnabled = enabled
        }

    var useWideViewPort: Boolean
        get() = origin.useWideViewPort
        set(use) {
            origin.useWideViewPort = use
        }

    var layoutAlgorithm: WebSettings.LayoutAlgorithm
        get() = origin.layoutAlgorithm
        set(l) {
            origin.layoutAlgorithm = l
        }

    var standardFontFamily: String
        get() = origin.standardFontFamily
        set(font) {
            origin.standardFontFamily = font
        }

    var fixedFontFamily: String
        get() = origin.fixedFontFamily
        set(font) {
            origin.fixedFontFamily = font
        }

    var sansSerifFontFamily: String
        get() = origin.sansSerifFontFamily
        set(font) {
            origin.sansSerifFontFamily = font
        }

    var serifFontFamily: String
        get() = origin.serifFontFamily
        set(font) {
            origin.serifFontFamily = font
        }

    var cursiveFontFamily: String
        get() = origin.cursiveFontFamily
        set(font) {
            origin.cursiveFontFamily = font
        }

    var fantasyFontFamily: String
        get() = origin.fantasyFontFamily
        set(font) {
            origin.fantasyFontFamily = font
        }

    var minimumFontSize: Int
        get() = origin.minimumFontSize
        set(size) {
            origin.minimumFontSize = size
        }

    var minimumLogicalFontSize: Int
        get() = origin.minimumLogicalFontSize
        set(size) {
            origin.minimumLogicalFontSize = size
        }

    var defaultFontSize: Int
        get() = origin.defaultFontSize
        set(size) {
            origin.defaultFontSize = size
        }

    var defaultFixedFontSize: Int
        get() = origin.defaultFixedFontSize
        set(size) {
            origin.defaultFixedFontSize = size
        }

    var loadsImagesAutomatically: Boolean
        get() = origin.loadsImagesAutomatically
        set(flag) {
            origin.loadsImagesAutomatically = flag
        }

    var blockNetworkImage: Boolean
        get() = origin.blockNetworkImage
        set(flag) {
            origin.blockNetworkImage = flag
        }

    var blockNetworkLoads: Boolean
        get() = origin.blockNetworkLoads
        set(flag) {
            origin.blockNetworkLoads = flag
        }

    var domStorageEnabled: Boolean
        get() = origin.domStorageEnabled
        set(flag) {
            origin.domStorageEnabled = flag
        }

    var databasePath: String
        @Suppress("DEPRECATION")
        @Deprecated("")
        get() = origin.databasePath
        @Suppress("DEPRECATION")
        @Deprecated("")
        set(databasePath) {
            origin.databasePath = databasePath
        }

    var databaseEnabled: Boolean
        get() = origin.databaseEnabled
        set(flag) {
            origin.databaseEnabled = flag
        }

    var javaScriptEnabled: Boolean
        get() = origin.javaScriptEnabled
        set(flag) {
            origin.javaScriptEnabled = flag
        }

    var allowUniversalAccessFromFileURLs: Boolean
        get() = origin.allowUniversalAccessFromFileURLs
        set(flag) {
            origin.allowUniversalAccessFromFileURLs = flag
        }

    var allowFileAccessFromFileURLs: Boolean
        get() = origin.allowFileAccessFromFileURLs
        set(flag) {
            origin.allowFileAccessFromFileURLs = flag
        }

    var javaScriptCanOpenWindowsAutomatically: Boolean
        get() = origin.javaScriptCanOpenWindowsAutomatically
        set(flag) {
            origin.javaScriptCanOpenWindowsAutomatically = flag
        }

    var defaultTextEncodingName: String
        get() = origin.defaultTextEncodingName
        set(encoding) {
            origin.defaultTextEncodingName = encoding
        }

    var userAgentString: String?
        get() = origin.userAgentString
        set(ua) {
            origin.userAgentString = ua
        }

    var cacheMode: Int
        get() = origin.cacheMode
        set(mode) {
            origin.cacheMode = mode
        }

    var mixedContentMode: Int
        get() = origin.mixedContentMode
        set(mode) {
            origin.mixedContentMode = mode
        }

    var offscreenPreRaster: Boolean
        @RequiresApi(api = Build.VERSION_CODES.M)
        get() = origin.offscreenPreRaster
        @RequiresApi(api = Build.VERSION_CODES.M)
        set(enabled) {
            origin.offscreenPreRaster = enabled
        }

    var safeBrowsingEnabled: Boolean
        @RequiresApi(api = Build.VERSION_CODES.O)
        get() = origin.safeBrowsingEnabled
        @RequiresApi(api = Build.VERSION_CODES.O)
        set(enabled) {
            origin.safeBrowsingEnabled = enabled
        }

    var disabledActionModeMenuItems: Int
        @RequiresApi(api = Build.VERSION_CODES.N)
        get() = origin.disabledActionModeMenuItems
        @RequiresApi(api = Build.VERSION_CODES.N)
        set(menuItems) {
            origin.disabledActionModeMenuItems = menuItems
        }

    var displayZoomButtons: Boolean
        get() = origin.displayZoomControls
        set(show) {
            origin.setSupportZoom(true)
            origin.builtInZoomControls = true
            origin.displayZoomControls = show
        }

    fun setSupportZoom(support: Boolean) {
        origin.setSupportZoom(support)
    }

    fun supportZoom(): Boolean {
        return origin.supportZoom()
    }

    @Suppress("DEPRECATION")
    @Deprecated("")
    fun setEnableSmoothTransition(enable: Boolean) {
        origin.setEnableSmoothTransition(enable)
    }

    fun setSupportMultipleWindows(support: Boolean) {
        origin.setSupportMultipleWindows(support)
    }

    fun supportMultipleWindows(): Boolean {
        return origin.supportMultipleWindows()
    }

    @Suppress("DEPRECATION")
    @Deprecated("")
    fun setGeolocationDatabasePath(databasePath: String) {
        origin.setGeolocationDatabasePath(databasePath)
    }

    fun setAppCachePath(appCachePath: String) {
        origin.setAppCachePath(appCachePath)
    }

    @Suppress("DEPRECATION")
    @Deprecated("")
    fun setAppCacheMaxSize(appCacheMaxSize: Long) {
        origin.setAppCacheMaxSize(appCacheMaxSize)
    }

    fun setNeedInitialFocus(flag: Boolean) {
        origin.setNeedInitialFocus(flag)
    }
}
