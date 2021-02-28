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

package jp.hazuki.yuzubrowser.browser

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.view.View
import android.webkit.*
import android.widget.TextView
import android.widget.Toast
import jp.hazuki.yuzubrowser.adblock.*
import jp.hazuki.yuzubrowser.adblock.filter.mining.MiningProtector
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpDatabase
import jp.hazuki.yuzubrowser.adblock.ui.abp.AbpFilterSubscribeDialog
import jp.hazuki.yuzubrowser.adblock.ui.original.AdBlockActivity
import jp.hazuki.yuzubrowser.bookmark.view.BookmarkActivity
import jp.hazuki.yuzubrowser.core.cache.SoftCache
import jp.hazuki.yuzubrowser.core.utility.extensions.*
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.download.*
import jp.hazuki.yuzubrowser.download.core.data.DownloadFile
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import jp.hazuki.yuzubrowser.download.core.data.EncodedImage
import jp.hazuki.yuzubrowser.download.ui.DownloadListActivity
import jp.hazuki.yuzubrowser.download.ui.FastDownloadActivity
import jp.hazuki.yuzubrowser.download.ui.fragment.DownloadDialog
import jp.hazuki.yuzubrowser.favicon.FaviconAsyncManager
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.history.presenter.BrowserHistoryActivity
import jp.hazuki.yuzubrowser.history.repository.BrowserHistoryAsyncManager
import jp.hazuki.yuzubrowser.legacy.Constants
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.browser.*
import jp.hazuki.yuzubrowser.legacy.debug.DebugActivity
import jp.hazuki.yuzubrowser.legacy.help.getHelpResponse
import jp.hazuki.yuzubrowser.legacy.help.isHelpUrl
import jp.hazuki.yuzubrowser.legacy.pattern.action.OpenOthersPatternAction
import jp.hazuki.yuzubrowser.legacy.pattern.action.WebSettingPatternAction
import jp.hazuki.yuzubrowser.legacy.pattern.action.WebSettingResetAction
import jp.hazuki.yuzubrowser.legacy.pattern.url.PatternUrlManager
import jp.hazuki.yuzubrowser.legacy.readitlater.ReadItLaterActivity
import jp.hazuki.yuzubrowser.legacy.resblock.ResourceBlockListActivity
import jp.hazuki.yuzubrowser.legacy.resblock.ResourceBlockManager
import jp.hazuki.yuzubrowser.legacy.resblock.ResourceChecker
import jp.hazuki.yuzubrowser.legacy.settings.activity.MainSettingsActivity
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDialAsyncManager
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDialHtml
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.toolbar.sub.GeolocationPermissionToolbar
import jp.hazuki.yuzubrowser.legacy.userjs.UserScript
import jp.hazuki.yuzubrowser.legacy.userjs.UserScriptDatabase
import jp.hazuki.yuzubrowser.legacy.utils.DisplayUtils
import jp.hazuki.yuzubrowser.legacy.utils.WebDownloadUtils
import jp.hazuki.yuzubrowser.legacy.utils.WebUtils
import jp.hazuki.yuzubrowser.legacy.utils.extensions.setClipboardWithToast
import jp.hazuki.yuzubrowser.legacy.webkit.TabType
import jp.hazuki.yuzubrowser.legacy.webkit.WebUploadHandler
import jp.hazuki.yuzubrowser.legacy.webrtc.WebPermissionsDao
import jp.hazuki.yuzubrowser.legacy.webrtc.WebRtcPermission
import jp.hazuki.yuzubrowser.ui.dialog.JsAlertDialog
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.ui.settings.PreferenceConstants
import jp.hazuki.yuzubrowser.ui.theme.ThemeData
import jp.hazuki.yuzubrowser.ui.utils.checkStoragePermission
import jp.hazuki.yuzubrowser.ui.widget.longToast
import jp.hazuki.yuzubrowser.webview.CustomWebChromeClient
import jp.hazuki.yuzubrowser.webview.CustomWebView
import jp.hazuki.yuzubrowser.webview.CustomWebViewClient
import jp.hazuki.yuzubrowser.webview.WebViewRenderingManager
import jp.hazuki.yuzubrowser.webview.utility.WebViewUtility
import jp.hazuki.yuzubrowser.webview.utility.getUserAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URISyntaxException
import java.text.DateFormat
import java.util.*
import kotlin.concurrent.thread

class WebClient(
    private val activity: BrowserBaseActivity,
    private val controller: BrowserController,
    private val abpDatabase: AbpDatabase,
    private val webPermissionsDao: WebPermissionsDao,
    faviconManager: FaviconManager
) : WebViewUtility {
    private val patternManager = PatternUrlManager(activity.applicationContext)
    private val speedDialManager = SpeedDialAsyncManager(activity.applicationContext)
    private val speedDialHtml = SpeedDialHtml(activity.applicationContext)
    private val faviconManager = FaviconAsyncManager(faviconManager)
    private val webViewRenderingManager = WebViewRenderingManager()
    private val scrollableToolbarHeight = { controller.appBarLayout.totalScrollRange + controller.pagePaddingHeight }
    private var browserHistoryManager: BrowserHistoryAsyncManager? = null
    private var resourceCheckerList: ArrayList<ResourceChecker>? = null
    private var adBlockController: AdBlockController? = null
    private var miningProtector: MiningProtector? = null
    private var userScriptList: ArrayList<UserScript>? = null
    private var webUploadHandler: WebUploadHandler? = null
    private val invertEnableJs by SoftCache {
        activity.readAssetsText("scripts/invert-min.js").replace("%s", "true")
    }
    private val invertDisableJs by SoftCache {
        activity.readAssetsText("scripts/invert-min.js").replace("%s", "false")
    }

    var isEnableHistory
        get() = browserHistoryManager != null
        set(enable) {
            if (enable == (browserHistoryManager != null)) return

            browserHistoryManager = if (enable) {
                BrowserHistoryAsyncManager(activity)
            } else {
                browserHistoryManager?.destroy()
                null
            }
        }

    var isEnableUserScript
        get() = userScriptList != null
        set(value) {
            if (value == isEnableUserScript) return

            resetUserScript(value)
        }

    var isEnableAdBlock
        get() = adBlockController != null
        set(value) {
            if (value == isEnableAdBlock) return

            adBlockController = if (value) AdBlockController(activity, abpDatabase.abpDao()) else null
        }

    fun updateAdBlockList() {
        adBlockController?.update()
    }

    fun destroy() {
        browserHistoryManager?.destroy()
        browserHistoryManager = null
        webUploadHandler?.destroy()
        webUploadHandler = null
        speedDialManager.destroy()
        faviconManager.destroy()
    }

    fun webUploadResult(resultCode: Int, data: Intent?) {
        webUploadHandler?.onActivityResult(resultCode, data)
    }

    fun onPreferenceReset() {
        patternManager.load(activity.applicationContext)
        webViewRenderingManager.onPreferenceReset(
            AppPrefs.rendering.get(),
            AppPrefs.night_mode_color.get(),
            AppPrefs.night_mode_bright.get())

        isEnableHistory = !AppPrefs.private_mode.get() && AppPrefs.save_history.get()

        resourceCheckerList = if (AppPrefs.resblock_enable.get()) {
            ResourceBlockManager(activity.applicationContext).list
        } else {
            null
        }

        adBlockController = if (AppPrefs.ad_block.get()) {
            AdBlockController(activity.applicationContext, abpDatabase.abpDao())
        } else {
            null
        }
        if (AppPrefs.mining_protect.get()) {
            if (miningProtector == null) {
                miningProtector = MiningProtector()
            }
        } else {
            miningProtector = null
        }

        controller.tabManager.loadedData.forEach {
            val webView = it.mWebView
            val oldInverted = webView.isInvertMode
            initWebSetting(webView)
            webView.onPreferenceReset()

            if (oldInverted != webView.isInvertMode) {
                webView.evaluateJavascript(if (webView.isInvertMode) invertEnableJs else invertDisableJs, null)
            }
        }

        controller.tabManager.currentTabData?.let {
            controller.toolbarManager.notifyChangeWebState(it)
        }

        val cookie = if (AppPrefs.private_mode.get())
            AppPrefs.accept_cookie.get() && AppPrefs.accept_cookie_private.get()
        else
            AppPrefs.accept_cookie.get()

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(cookie)

        val thirdCookie = cookie && AppPrefs.accept_third_cookie.get()
        controller.tabManager.loadedData.forEach {
            it.mWebView.setAcceptThirdPartyCookies(cookieManager, thirdCookie)
        }

        resetUserScript(AppPrefs.userjs_enable.get())
    }

    fun initWebSetting(web: CustomWebView) {
        web.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY)
        web.setOverScrollModeMethod(View.OVER_SCROLL_IF_CONTENT_SCROLLS)

        applyRenderingMode(web, webViewRenderingManager.defaultMode)
        webViewRenderingManager.setWebViewRendering(web)
        web.setScrollableHeight(scrollableToolbarHeight)

        web.setMyWebChromeClient(MyWebChromeClient())
        web.setMyWebViewClient(mWebViewClient)

        web.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            onDownloadStart(web, url, userAgent, contentDisposition, mimetype, contentLength)
        }

        val setting = web.webSettings
        setting.setNeedInitialFocus(false)
        setting.defaultFontSize = 16
        setting.defaultFixedFontSize = 13
        setting.minimumLogicalFontSize = AppPrefs.minimum_font.get()
        setting.minimumFontSize = AppPrefs.minimum_font.get()

        setting.mixedContentMode = AppPrefs.mixed_content.get()
        setting.setSupportMultipleWindows(AppPrefs.newtab_blank.get() != BrowserManager.LOAD_URL_TAB_CURRENT)
        setting.textZoom = AppPrefs.text_size.get()
        setting.javaScriptEnabled = AppPrefs.javascript.get()


        setting.allowContentAccess = AppPrefs.allow_content_access.get()
        setting.defaultTextEncodingName = AppPrefs.default_encoding.get()
        setting.userAgentString =
            activity.getRealUserAgent(AppPrefs.user_agent.get(), AppPrefs.fake_chrome.get())
        setting.loadWithOverviewMode = AppPrefs.load_overview.get()
        setting.useWideViewPort = AppPrefs.web_wideview.get()
        setting.displayZoomButtons = AppPrefs.show_zoom_button.get()
        setting.cacheMode = AppPrefs.web_cache.get()
        setting.javaScriptCanOpenWindowsAutomatically = AppPrefs.web_popup.get()
        setting.layoutAlgorithm = WebSettings.LayoutAlgorithm.valueOf(AppPrefs.layout_algorithm.get())
        setting.loadsImagesAutomatically = !AppPrefs.block_web_images.get()

        val noPrivate = !AppPrefs.private_mode.get()
        setting.databaseEnabled = noPrivate && AppPrefs.web_db.get()
        setting.domStorageEnabled = noPrivate && AppPrefs.web_dom_db.get()
        setting.geolocationEnabled = noPrivate && AppPrefs.web_geolocation.get()
        setting.appCacheEnabled = noPrivate && AppPrefs.web_app_cache.get()
        setting.webTheme = AppPrefs.webTheme.get()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setting.safeBrowsingEnabled = AppPrefs.safe_browsing.get()
        } else {
            @Suppress("DEPRECATION")
            setting.saveFormData = noPrivate && AppPrefs.save_formdata.get()
        }

        setting.setAppCachePath(activity.appCacheFilePath)

        var webViewTheme: CustomWebView.WebViewTheme? = null
        val theme = ThemeData.getInstance()
        if (theme != null && theme.progressColor != 0) {
            val color = theme.progressColor
            val isDark = if (theme.refreshUseDark) activity.getResColor(R.color.deep_gray) else 0
            webViewTheme = CustomWebView.WebViewTheme(color, isDark)
        }
        web.setWebViewTheme(webViewTheme)
        web.swipeEnable = AppPrefs.pull_to_refresh.get()

        //if add to this, should also add to AbstractCacheWebView#settingWebView
    }

    fun loadUrl(tab: MainTabData, url: String, handleOpenInBrowser: Boolean) {
        if (tab.isNavLock && !url.shouldLoadSameTabUser()) {
            controller.performNewTabLink(BrowserManager.LOAD_URL_TAB_NEW_RIGHT, tab, url, TabType.WINDOW)
            return
        }
        if (!checkUrl(tab, url, Uri.parse(url))) {
            if (!checkLoadPagePatternMatch(tab, url, handleOpenInBrowser))
                tab.mWebView.loadUrl(url)
        }
    }

    fun loadUrl(tab: MainTabData, url: String, target: Int, type: Int) {
        if (!checkNewTabLinkUser(target, tab, url, type))
            loadUrl(tab, url, target == BrowserManager.LOAD_URL_TAB_CURRENT_FORCE)
    }

    private fun checkNewTabLinkUser(perform: Int, tab: MainTabData, url: String, @TabType type: Int): Boolean {
        if (perform < 0)
            return false

        return when (perform) {
            BrowserManager.LOAD_URL_TAB_CURRENT, BrowserManager.LOAD_URL_TAB_CURRENT_FORCE -> false
            else -> !url.shouldLoadSameTabUser() && controller.performNewTabLink(perform, tab, url, type)
        }
    }

    private val mWebViewClient = object : CustomWebViewClient() {
        override fun shouldOverrideUrlLoading(web: CustomWebView, url: String, uri: Uri): Boolean {
            val data = controller.getTabOrNull(web) ?: return true

            val patternResult = checkLoadPagePatternMatch(data, url, false)

            if (patternResult || checkNewTabLinkAuto(getNewTabPerformType(data), data, url)) {
                if (web.url == null || data.mWebView.isBackForwardListEmpty) {
                    controller.removeTab(controller.indexOf(data.id))
                }
                return true
            }

            return checkUrl(data, url, uri)
        }

        override fun onPageStarted(web: CustomWebView, url: String, favicon: Bitmap?) {
            val data = controller.getTabOrNull(web) ?: return

            if (AppPrefs.toolbar_auto_open.get()) {
                controller.expandToolbar()
                data.mWebView.isNestedScrollingEnabledMethod = false
            }
            if (controller.isEnableFindOnPage && controller.isFindOnPageAutoClose) {
                controller.isEnableFindOnPage = false
            }
            checkSettingsPatternMatch(data, url)

            applyUserScript(web, url, UserScript.RunAt.START)

            data.onPageStarted(url, favicon)

            if (data === controller.currentTabData) {
                controller.notifyChangeWebState(data)
            }

            if (controller.isActivityPaused) {
                resumeWebViewTimers(data)
            }

            controller.stopAutoScroll()

            data.onStartPage()

            if (AppPrefs.save_tabs_for_crash.get())
                controller.tabManager.saveData()

            controller.tabManager.removeThumbnailCache(url)
        }

        override fun onDomContentLoaded(web: CustomWebView) {
            val data = controller.getTabOrNull(web) ?: return
            applyJavascriptInjection(data, web, data.url ?: web.url ?: "")
        }

        override fun onPageFinished(web: CustomWebView, url: String) {
            controller.onPageFinished()
            val data = controller.getTabOrNull(web) ?: return
            applyUserScript(web, url, UserScript.RunAt.IDLE)

            if (controller.isActivityPaused) {
                pauseWebViewTimers(data)
            }

            data.onPageFinished(web, url)

            controller.requestAdjustWebView()

            if (data === controller.currentTabData) {
                controller.notifyChangeWebState(data)

                web.view.postDelayed({ controller.adjustBrowserPadding(data) }, 50)
            }

            controller.tabManager.takeThumbnailIfNeeded(data)

            if (AppPrefs.save_tabs_for_crash.get())
                controller.tabManager.saveData()

            if (speedDialManager.isNeedUpdate(data.originalUrl)) {
                web.evaluateJavascript(Scripts.GET_ICON_URL) {
                    val iconUrl = if (it.startsWith('"') && it.endsWith('"')) it.substring(1, it.length - 1) else it

                    if (iconUrl.isEmpty() || iconUrl == "null") {
                        speedDialManager.updateAsync(data.originalUrl, faviconManager[data.originalUrl])
                    } else {
                        if (iconUrl.startsWith("data", ignoreCase = true)) {
                            EncodedImage(iconUrl)?.let { image ->
                                speedDialManager.updateAsync(data.originalUrl, image.image)
                            }
                        } else {
                            val userAgent = data.mWebView.getUserAgent()
                            GlobalScope.launch(Dispatchers.IO) {
                                val cookie = CookieManager.getInstance().getCookie(url)
                                val icon = controller.okHttpClient
                                    .getImage(iconUrl, userAgent, url, cookie)
                                speedDialManager.updateAsync(data.originalUrl, icon)
                            }
                        }
                    }
                }
            }
        }

        private fun applyJavascriptInjection(tab: MainTabData, web: CustomWebView, url: String) {
            if (tab.renderingMode >= 0) {
                applyRenderingMode(web, tab.renderingMode)
                tab.resetRenderingMode()
            }
            if (web.isInvertMode) {
                web.evaluateJavascript(invertEnableJs, null)
            }
            val adBlockController = adBlockController
            if (adBlockController != null) {
                adBlockController.loadScript(Uri.parse(url))?.let {
                    web.evaluateJavascript(it, null)
                }
            }

            applyUserScript(web, url, UserScript.RunAt.END)
        }

        override fun onPageChanged(web: CustomWebView, url: String, originalUrl: String, progress: Int, isLoading: Boolean) {
            controller.getTabOrNull(web)?.let { tab ->
                tab.onStateChanged(web.title, url, originalUrl, progress, isLoading, faviconManager)
                if (tab == controller.currentTabData) {
                    controller.notifyChangeWebState(tab)
                }
                checkSettingsPatternMatch(tab, url)
            }
            if (controller.isEnableFindOnPage && controller.isFindOnPageAutoClose) {
                controller.isEnableFindOnPage = false
            }
        }

        override fun onFormResubmission(web: CustomWebView, dontResend: Message, resend: Message) {
            AlertDialog.Builder(activity)
                .setTitle(web.url)
                .setMessage(R.string.form_resubmit)
                .setPositiveButton(android.R.string.ok) { _, _ -> resend.sendToTarget() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> dontResend.sendToTarget() }
                .setOnCancelListener { dontResend.sendToTarget() }
                .show()
        }

        override fun doUpdateVisitedHistory(web: CustomWebView, url: String, isReload: Boolean) {
            val data = controller.getTabOrNull(web)?.originalUrl ?: return

            browserHistoryManager?.add(data)
        }

        override fun onReceivedHttpAuthRequest(web: CustomWebView, handler: HttpAuthHandler, host: String, realm: String) {
            HttpAuthRequestDialog(activity).requestHttpAuth(web, handler, host, realm)
        }

        override fun onReceivedError(view: CustomWebView, errorCode: Int, description: CharSequence, url: Uri) {
            if (errorCode == ERROR_UNSUPPORTED_SCHEME && url.toString().equals("yuzu:speeddial", true)) {
                view.view.postDelayed({ view.reload() }, 50)
            }
        }

        override fun onReceivedSslError(web: CustomWebView, handler: SslErrorHandler, error: SslError) {
            if (!AppPrefs.ssl_error_alert.get()) {
                handler.cancel()
                return
            }

            if (!activity.isFinishing) {
                val view = View.inflate(activity, R.layout.dialog_ssl_error, null)
                view.findViewById<TextView>(R.id.urlTextView).apply {
                    text = error.url
                    setOnLongClickListener {
                        activity.setClipboardWithToast(text.toString())
                        true
                    }
                }
                view.findViewById<TextView>(R.id.messageTextView).text = activity.getString(R.string.ssl_error_mes, error.getErrorMessages(activity))

                AlertDialog.Builder(activity)
                    .setTitle(R.string.ssl_error_title)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok) { _, _ -> handler.proceed() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> handler.cancel() }
                    .setOnCancelListener { handler.cancel() }
                    .show()
            }
        }

        override fun shouldInterceptRequest(web: CustomWebView, request: WebResourceRequest): WebResourceResponse? {
            if ("yuzu".equals(request.url.scheme, ignoreCase = true)) {
                if (request.url.isHelpUrl()) {
                    return request.url.getHelpResponse(activity)
                }
                val action = request.url.schemeSpecificPart

                if (action != null) {
                    when {
                        "speeddial".equals(action, ignoreCase = true) -> return speedDialHtml.createResponse()
                        "speeddial/base.css" == action -> return speedDialHtml.baseCss
                        "speeddial/custom.css" == action -> return speedDialHtml.customCss
                        action.startsWith("speeddial/img/") -> return speedDialHtml.getImage(action)
                    }
                }
            }

            if ("file".equals(request.url.scheme, ignoreCase = true)) {
                return WebResourceResponse("text/text", "UTF-8", EmptyInputStream())
            }

            val tabIndexData = controller.tabManager.getIndexData(web.identityId) ?: return null
            val uri = Uri.parse(tabIndexData.url ?: "")

            adBlockController?.run {
                val host = uri.host
                if (host != null) {
                    if (request.isForMainFrame) {
                        web.isBlock = !isWhitePage(request.url)
                    }

                    if (web.isBlock) {
                        val filter = isBlock(request.getContentRequest(uri ?: Uri.parse("")))
                        if (filter != null) {
                            return if (request.isForMainFrame) {
                                createMainFrameDummy(activity, request.url, filter.pattern)
                            } else {
                                createDummy(request.url)
                            }
                        }
                    }
                }
            }

            miningProtector?.run {
                if (isBlock(request.getContentRequest(uri ?: Uri.parse("")))) {
                    return dummy
                }
            }

            resourceCheckerList?.forEach {
                when (it.check(request.url)) {
                    ResourceChecker.SHOULD_RUN -> return it.getResource(activity.applicationContext)
                    ResourceChecker.SHOULD_BREAK -> return null
                    ResourceChecker.SHOULD_CONTINUE -> return@forEach
                    else -> throw RuntimeException("unknown : " + it.check(request.url))
                }
            }
            return null
        }
    }

    fun checkLoadPagePatternMatch(tab: MainTabData, url: String?, handleOpenInBrowser: Boolean): Boolean {
        if (url == null) return false

        if (url.startsWith("yuzu://help")) {
            if (!tab.mWebView.webSettings.javaScriptEnabled) {
                if (tab.resetAction == null) {
                    tab.resetAction = WebSettingResetAction(tab)
                }
                tab.mWebView.webSettings.javaScriptEnabled = true
            }
        }

        for (item in patternManager.list) {
            if (item.action is WebSettingPatternAction) continue
            if (!item.isMatchUrl(url)) continue

            if (handleOpenInBrowser && item.action is OpenOthersPatternAction) {
                continue
            } else if (item.action.run(activity, tab, url))
                return true
        }
        return false
    }

    fun checkSettingsPatternMatch(tab: MainTabData, url: String?) {
        if (url == null) return
        var normalSettings = true
        var changeSetting = false

        if (url.startsWith("yuzu://help")) {
            normalSettings = false
            changeSetting = true
        }

        for (item in patternManager.list) {
            if (item.action !is WebSettingPatternAction) continue
            if (!item.isMatchUrl(url)) continue

            if (tab.resetAction != null && tab.resetAction.patternAction == item.action) {
                normalSettings = false
                continue
            }

            /* save normal settings */
            if (tab.resetAction == null)
                tab.resetAction = WebSettingResetAction(tab)
            tab.resetAction.patternAction = item.action as WebSettingPatternAction

            /* change web settings */
            item.action.run(activity, tab, url)
            changeSetting = true
        }

        if (changeSetting) return

        /* reset to normal */
        if (normalSettings && tab.resetAction != null) {
            tab.resetAction.reset(tab)
            tab.resetAction = null
            controller.notifyChangeWebState()
        }
    }

    private fun onDownloadStart(web: CustomWebView, url: String, userAgent: String, contentDisposition: String, mimetype: String, contentLength: Long) {
        val referrer = web.url
        if (url.startsWith("blob")) {
            if (controller.applicationContextInfo.checkStoragePermission()) {
                web.evaluateJavascript(activity.getBlobDownloadJavaScript(url, controller.secretKey), null)
            } else {
                ui {
                    if (controller.requestStoragePermission()) {
                        web.evaluateJavascript(activity.getBlobDownloadJavaScript(url, controller.secretKey), null)
                    }
                }
            }
            return
        }

        when (AppPrefs.download_action.get()) {
            PreferenceConstants.DOWNLOAD_DO_NOTHING -> {
            }
            PreferenceConstants.DOWNLOAD_AUTO -> if (WebDownloadUtils.shouldOpen(contentDisposition)) {
                actionOpen(url, referrer, userAgent, contentDisposition, mimetype, contentLength)
            } else {
                actionDownload(url, referrer, userAgent, contentDisposition, mimetype, contentLength)
            }
            PreferenceConstants.DOWNLOAD_DOWNLOAD -> actionDownload(url, referrer, userAgent, contentDisposition, mimetype, contentLength)
            PreferenceConstants.DOWNLOAD_OPEN -> actionOpen(url, referrer, userAgent, contentDisposition, mimetype, contentLength)
            PreferenceConstants.DOWNLOAD_SHARE -> actionShare(url)
            PreferenceConstants.DOWNLOAD_SELECT -> {

                AlertDialog.Builder(activity)
                    .setTitle(R.string.download)
                    .setItems(
                        arrayOf(getString(R.string.download), getString(R.string.open), getString(R.string.share))
                    ) { _, which ->
                        when (which) {
                            0 -> actionDownload(url, referrer, userAgent, contentDisposition, mimetype, contentLength)
                            1 -> actionOpen(url, referrer, userAgent, contentDisposition, mimetype, contentLength)
                            2 -> actionShare(url)
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }

        if (web.isBackForwardListEmpty) {
            controller.removeTab(controller.indexOf(web.identityId))
        }
    }

    private fun actionDownload(url: String, referrer: String?, userAgent: String, contentDisposition: String, mimetype: String, contentLength: Long) {
        if (controller.applicationContextInfo.checkStoragePermission()) {
            activity.showDialog(DownloadDialog(url, userAgent, contentDisposition, mimetype, contentLength, referrer), "download")
        } else {
            ui {
                if (controller.requestStoragePermission()) {
                    activity.showDialog(DownloadDialog(url, userAgent, contentDisposition, mimetype, contentLength, referrer), "download")
                }
            }
        }
    }

    private fun actionOpen(url: String, referrer: String?, userAgent: String, contentDisposition: String, mimetype: String, contentLength: Long) {
        if (!WebDownloadUtils.openFile(activity, url, mimetype)) {
            //application not found
            Toast.makeText(activity.applicationContext, R.string.app_notfound, Toast.LENGTH_SHORT).show()
            actionDownload(url, referrer, userAgent, contentDisposition, mimetype, contentLength)
        }
    }

    private fun actionShare(url: String) {
        WebUtils.shareWeb(activity, url, null)
    }

    private inner class MyWebChromeClient : CustomWebChromeClient() {
        private var geoView: GeolocationPermissionToolbar? = null

        override fun onProgressChanged(web: CustomWebView, newProgress: Int) {
            val data = controller.getTabOrNull(web) ?: return

            data.onProgressChanged(newProgress)

            if (data === controller.currentTabData) {
                if (data.isInPageLoad)
                    controller.notifyChangeProgress(data)
                else
                    controller.notifyChangeWebState(data)
            }

            if (data.isStartDocument) {
                data.isStartDocument = false
                web.onPageDocumentStart()
            }
        }

        override fun onReceivedTitle(web: CustomWebView, title: String) {
            val data = controller.getTabOrNull(web) ?: return

            data.onReceivedTitle(title)

            browserHistoryManager?.update(data.originalUrl, title)
        }

        override fun onReceivedIcon(web: CustomWebView, icon: Bitmap) {
            val data = controller.getTabOrNull(web) ?: return

            faviconManager.updateAsync(data.originalUrl, icon)

            data.onReceivedIcon(icon)
        }

        override fun onRequestFocus(web: CustomWebView) {
            val i = controller.indexOf(web.identityId)
            if (i >= 0)
                controller.setCurrentTab(i)
        }


        override fun onCreateWindow(view: CustomWebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
            controller.checkNewTabLink(AppPrefs.newtab_blank.get(), resultMsg.obj as WebView.WebViewTransport)
            resultMsg.sendToTarget()
            return true
        }

        override fun onCloseWindow(web: CustomWebView) {
            val i = controller.indexOf(web.identityId)
            if (i >= 0)
                controller.removeTab(i)
        }

        override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>?>, fileChooserParams: FileChooserParams): Boolean {
            if (webUploadHandler == null)
                webUploadHandler = WebUploadHandler()

            try {
                controller.startActivity(Intent.createChooser(webUploadHandler!!.onShowFileChooser(filePathCallback, fileChooserParams), getString(R.string.select_file)), BrowserController.REQUEST_WEB_UPLOAD)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                Toast.makeText(activity.applicationContext, R.string.app_notfound, Toast.LENGTH_SHORT).show()
            }

            return true
        }

        override fun onJsAlert(view: CustomWebView, url: String, message: String, result: JsResult): Boolean {
            val tab = controller.getTabOrNull(view) ?: return true
            if (tab.isAlertAllowed && !activity.isFinishing) {
                JsAlertDialog(activity)
                    .setAlertMode(url, message, tab.alertMode == MainTabData.ALERT_MULTIPULE) { dialogResult, blockAlert ->
                        if (dialogResult) result.confirm() else result.cancel()
                        if (blockAlert) tab.alertMode = MainTabData.ALERT_BLOCKED
                    }
                    .show()
                tab.alertMode = MainTabData.ALERT_MULTIPULE
            } else {
                result.cancel()
            }
            return true
        }

        override fun onJsConfirm(view: CustomWebView, url: String, message: String, result: JsResult): Boolean {
            val tab = controller.getTabOrNull(view) ?: return true
            if (tab.isAlertAllowed && !activity.isFinishing) {
                JsAlertDialog(activity)
                    .setConfirmMode(url, message, tab.alertMode == MainTabData.ALERT_MULTIPULE) { dialogResult, blockAlert ->
                        if (dialogResult) result.confirm() else result.cancel()
                        if (blockAlert) tab.alertMode = MainTabData.ALERT_BLOCKED
                    }
                    .show()
                tab.alertMode = MainTabData.ALERT_MULTIPULE
            } else {
                result.cancel()
            }
            return true
        }

        override fun onJsPrompt(view: CustomWebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
            val tab = controller.getTabOrNull(view) ?: return true
            if (tab.isAlertAllowed && !activity.isFinishing) {
                JsAlertDialog(activity)
                    .setPromptMode(url, message, defaultValue, tab.alertMode == MainTabData.ALERT_MULTIPULE) { dialogResult, blockAlert ->
                        if (dialogResult != null) result.confirm(dialogResult) else result.cancel()
                        if (blockAlert) tab.alertMode = MainTabData.ALERT_BLOCKED
                    }
                    .show()
                tab.alertMode = MainTabData.ALERT_MULTIPULE
            } else {
                result.cancel()
            }
            return true
        }


        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            controller.showCustomView(view, callback)
        }

        override fun onHideCustomView() {
            controller.hideCustomView()
        }

        override fun getVideoLoadingProgressView(): View? = controller.getVideoLoadingProgressView()

        override fun onGeolocationPermissionsHidePrompt() {
            geoView?.let {
                controller.toolbarManager.hideGeolocationPermissionPrompt(it)
                geoView = null
            }
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
            if (geoView == null) {
                geoView = object : GeolocationPermissionToolbar(activity, controller) {
                    override fun onHideToolbar() {
                        controller.toolbarManager.hideGeolocationPermissionPrompt(geoView!!)
                        geoView = null
                    }
                }
                controller.toolbarManager.showGeolocationPermissionPrompt(geoView!!)
            }
            geoView!!.onGeolocationPermissionsShowPrompt(origin, callback)
        }

        override fun getVisitedHistory(callback: ValueCallback<Array<String>>) {
            browserHistoryManager?.run {
                thread {
                    callback.onReceiveValue(getHistoryArray(3000))
                }
            }
        }

        override fun onPermissionRequest(request: PermissionRequest) {
            if (AppPrefs.webRtc.get()) {
                WebRtcPermission.getInstance(webPermissionsDao).requestPermission(request, controller.webRtcRequest)
            } else {
                ui { request.deny() }
            }
        }

        override fun getDefaultVideoPoster(): Bitmap? {
            return BitmapFactory.decodeResource(controller.resourcesByInfo, R.drawable.ic_movie_play_white)
        }
    }

    private fun checkUrl(data: MainTabData, url: String, uri: Uri): Boolean {
        val scheme = uri.scheme ?: return false

        when (scheme.toLowerCase(Locale.ENGLISH)) {
            "intent" -> {
                try {
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)

                    if (intent != null) {
                        if (BookmarkActivity::class.java.name == intent.component?.className) {
                            controller.startActivity(intent, BrowserController.REQUEST_BOOKMARK)
                        } else {
                            val info = activity.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                            if (info != null) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                try {
                                    activity.startActivity(intent)
                                    return true
                                } catch (e: SecurityException) {
                                    e.printStackTrace()
                                }

                            }
                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                            if (!fallbackUrl.isNullOrEmpty()) {
                                controller.loadUrl(data, fallbackUrl)
                            }
                        }
                        return true
                    }
                } catch (e: URISyntaxException) {
                    Logger.e(TAG, "Can't resolve intent://", e)
                }
            }
            "yuzu" -> {
                var action = uri.schemeSpecificPart

                val intent: Intent
                if (action.isNullOrEmpty()) {
                    return false
                } else
                    if (action.indexOf('/') > -1) {
                        action = action.substring(0, action.indexOf('/'))
                    }
                when (action.toLowerCase(Locale.ENGLISH)) {
                    "settings", "setting" -> intent = Intent(activity, MainSettingsActivity::class.java)
                    "histories", "history" -> {
                        intent = Intent(activity, BrowserHistoryActivity::class.java)
                        intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, controller.isFullscreenMode && DisplayUtils.isNeedFullScreenFlag())
                        intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, controller.requestedOrientationByCtrl)
                        controller.startActivity(intent, BrowserController.REQUEST_HISTORY)
                        return true
                    }
                    "downloads", "download" -> {
                        intent = Intent(activity, DownloadListActivity::class.java).apply {
                            putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, controller.isFullscreenMode && DisplayUtils.isNeedFullScreenFlag())
                            putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, controller.requestedOrientationByCtrl)
                        }
                    }
                    "debug" -> intent = Intent(activity, DebugActivity::class.java)
                    "bookmarks", "bookmark" -> {
                        intent = Intent(activity, BookmarkActivity::class.java).apply {
                            putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, controller.isFullscreenMode && DisplayUtils.isNeedFullScreenFlag())
                            putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, controller.requestedOrientationByCtrl)
                        }
                        controller.startActivity(intent, BrowserController.REQUEST_BOOKMARK)
                        return true
                    }
                    "search" -> {
                        controller.showSearchBox("", controller.indexOf(data.id), 0, "reverse".equals(uri.fragment, ignoreCase = true))
                        return true
                    }
                    "speeddial" -> return false
                    "home" -> {
                        if ("yuzu:home".equals(AppPrefs.home_page.get(), ignoreCase = true) || "yuzu://home".equals(AppPrefs.home_page.get(), ignoreCase = true)) {
                            AppPrefs.home_page.set("about:blank")
                            AppPrefs.commit(activity, AppPrefs.home_page)
                        }
                        controller.loadUrl(data, AppPrefs.home_page.get())
                        return true
                    }
                    "resblock" -> intent = Intent(activity, ResourceBlockListActivity::class.java)
                    "adblock" -> intent = Intent(activity, AdBlockActivity::class.java)
                    "readitlater" -> intent = Intent(activity, ReadItLaterActivity::class.java)
                    "download-file" -> {
                        val keyData = uri.schemeSpecificPart.substring(action.length + 1)

                        val items = keyData.split('&', limit = 3)
                        if (items.size == 3 && items[0] == controller.secretKey) {
                            when (items[1]) {
                                "0" -> onDownloadStart(data.mWebView, items[2], "", "", "", -1)
                                "1" -> DownloadDialog(items[2], data.mWebView.webSettings.userAgentString)//TODO referer
                                    .show(controller.activity.supportFragmentManager, "download")
                                "2" -> {
                                    val file = DownloadFile(items[2], null, DownloadRequest(null, data.mWebView.webSettings.userAgentString, null))
                                    controller.activity.download(getDownloadFolderUri(controller.applicationContextInfo), file, null)
                                }
                                "3" -> {
                                    val downloader = FastDownloadActivity
                                        .intent(controller.activity,
                                            items[2],
                                            data.mWebView.url,
                                            data.mWebView.getUserAgent(),
                                            ".jpg")
                                    controller.startActivity(downloader, BrowserController.REQUEST_SHARE_IMAGE)
                                }
                            }
                        }
                        return true
                    }
                    else -> return false
                }
                activity.startActivity(intent)
                return true
            }
            "mailto" -> {
                try {
                    activity.startActivity(Intent(Intent.ACTION_SENDTO, uri))
                } catch (e: ActivityNotFoundException) {
                    controller.activity.longToast(R.string.app_notfound)
                }
                return true
            }
            "tel" -> {
                try {
                    activity.startActivity(Intent(Intent.ACTION_DIAL, uri))
                } catch (e: ActivityNotFoundException) {
                    controller.activity.longToast(R.string.app_notfound)
                }
                return true
            }
            "abp" -> {
                if (uri.host == "subscribe") {
                    if (subscribeAdBlockFilter(uri)) {
                        return true
                    }
                }
            }
            "http", "https" -> {
                if (uri.host == "subscribe.adblockplus.org" && uri.path == "/") {
                    if (subscribeAdBlockFilter(uri)) {
                        return true
                    }
                }
            }
        }

        if (AppPrefs.share_unknown_scheme.get()) {
            if (WebUtils.isOverrideScheme(uri)) {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                val info = activity.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                if (info != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        activity.startActivity(intent)
                        return true
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }

                }
                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                if (!fallbackUrl.isNullOrEmpty()) {
                    controller.loadUrl(data, fallbackUrl)
                }
                return true
            }
        }
        data.url = url

        return false
    }

    fun resetUserScript(enable: Boolean) {
        if (enable) {
            userScriptList = UserScriptDatabase(activity.applicationContext).enableJsDataList
        } else {
            if (userScriptList != null)
                userScriptList = null
        }
    }

    private fun applyUserScript(web: CustomWebView, url: String, runAt: UserScript.RunAt) {
        userScriptList?.let {
            SCRIPT_LOOP@ for (script in it) {
                if (runAt != script.runAt)
                    continue

                for (pattern in script.exclude) {
                    if (pattern.matcher(url).find())
                        continue@SCRIPT_LOOP
                }

                for (pattern in script.include) {
                    if (pattern.matcher(url).find()) {
                        web.evaluateJavascript(script.runnable, null)

                        continue@SCRIPT_LOOP
                    }
                }
            }
        }
    }

    private fun subscribeAdBlockFilter(uri: Uri): Boolean {
        val url = uri.getQueryParameter("location") ?: return false
        val title = uri.getQueryParameter("title") ?: url

        AbpFilterSubscribeDialog.create(title, url)
            .show(controller.activity.supportFragmentManager, "subscribe")

        return true
    }

    private fun checkNewTabLinkAuto(perform: Int, tab: MainTabData, url: String): Boolean {
        if (tab.isNavLock && !url.shouldLoadSameTabAuto()) {
            controller.performNewTabLink(BrowserManager.LOAD_URL_TAB_NEW_RIGHT, tab, url, TabType.WINDOW)
            return true
        }

        if (perform == BrowserManager.LOAD_URL_TAB_CURRENT)
            return false

        if (url.shouldLoadSameTabAuto())
            return false

        return if (url.shouldLoadSameTabScheme()) false else !((url == tab.url) || tab.mWebView.isBackForwardListEmpty) && controller.performNewTabLink(perform, tab, url, TabType.WINDOW)
    }

    private fun getNewTabPerformType(tab: MainTabData): Int {
        return if ((tab.originalUrl ?: tab.url ?: "").isSpeedDial()) {
            AppPrefs.newtab_speeddial.get()
        } else {
            AppPrefs.newtab_link.get()
        }
    }

    fun pauseWebViewTimers(tab: MainTabData?): Boolean {
        Logger.d(TAG, "pauseWebViewTimers")
        if (tab == null) return true
        if (!tab.isInPageLoad) {
            Logger.d(TAG, "pauseTimers")
            tab.mWebView.pauseTimers()
            return true
        }
        return false
    }

    fun resumeWebViewTimers(tab: MainTabData?): Boolean {
        Logger.d(TAG, "resumeWebViewTimers")
        if (tab == null) return true
        val inLoad = tab.isInPageLoad
        val paused = controller.isActivityPaused
        if (!paused && !inLoad || paused && inLoad) {
            Logger.d(TAG, "resumeTimers")
            tab.mWebView.resumeTimers()
            return true
        }
        return false
    }

    var defaultRenderingMode: Int
        get() = webViewRenderingManager.defaultMode
        set(value) {
            webViewRenderingManager.defaultMode = value
        }

    fun applyRenderingMode(webView: CustomWebView, mode: Int) {
        if (webView.renderingMode == mode) return

        val oldIsInvert = webView.isInvertMode
        webViewRenderingManager.setWebViewRendering(webView, mode)
        if (oldIsInvert != webView.isInvertMode) {
            webView.evaluateJavascript(if (webView.isInvertMode) invertEnableJs else invertDisableJs, null)
        }
    }

    private fun getString(id: Int): String = activity.getString(id)

    private fun SslError.getErrorMessages(context: Context): String {
        val builder = StringBuilder()
        if (hasError(SslError.SSL_DATE_INVALID)) {
            builder.appendError(context.getText(R.string.ssl_error_certificate_date_invalid))
        }
        if (hasError(SslError.SSL_EXPIRED)) {
            builder.appendError(context.getText(R.string.ssl_error_certificate_expired))
                .appendErrorInfo(context.getText(R.string.ssl_error_certificate_expired_info), DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT).format(certificate.validNotAfterDate))
        }
        if (hasError(SslError.SSL_IDMISMATCH)) {
            builder.appendError(context.getText(R.string.ssl_error_certificate_domain_mismatch))
                .appendErrorInfo(context.getText(R.string.ssl_error_certificate_domain_mismatch_info), certificate.issuedTo.cName)
        }
        if (hasError(SslError.SSL_NOTYETVALID)) {
            builder.appendError(context.getText(R.string.ssl_error_certificate_not_yet_valid))
                .appendErrorInfo(context.getText(R.string.ssl_error_certificate_not_yet_valid_info), DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT).format(certificate.validNotBeforeDate))
        }
        if (hasError(SslError.SSL_UNTRUSTED)) {
            builder.appendError(context.getText(R.string.ssl_error_certificate_untrusted))
                .appendErrorInfo(context.getText(R.string.ssl_error_certificate_untrusted_info), certificate.issuedBy.dName)
        }
        if (hasError(SslError.SSL_INVALID)) {
            builder.appendError(context.getText(R.string.ssl_error_certificate_invalid))
        }
        return builder.toString()
    }

    private fun StringBuilder.appendError(sequence: CharSequence): StringBuilder {
        append(" - ").append(sequence).append("\n")
        return this
    }

    private fun StringBuilder.appendErrorInfo(sequence: CharSequence, info: CharSequence): StringBuilder {
        append("   ").append(sequence).append(info).append('\n')
        return this
    }

    fun String.isSpeedDialUrl(): Boolean {
        return "yuzu:speeddial".equals(this, ignoreCase = true)
    }

    companion object {
        private const val TAG = "WebClient"
    }
}
