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

package jp.hazuki.yuzubrowser.browser

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.gesture.GestureOverlayView
import android.media.AudioManager
import android.os.*
import android.text.TextUtils
import android.view.*
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import com.squareup.moshi.Moshi
import jp.hazuki.asyncpermissions.AsyncPermissions
import jp.hazuki.yuzubrowser.browser.behavior.BottomBarBehavior
import jp.hazuki.yuzubrowser.browser.behavior.WebViewBehavior
import jp.hazuki.yuzubrowser.browser.manager.UserActionManager
import jp.hazuki.yuzubrowser.browser.tab.TabManagerFactory
import jp.hazuki.yuzubrowser.browser.view.Toolbar
import jp.hazuki.yuzubrowser.core.utility.extensions.appCacheFilePath
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.legacy.BrowserApplication
import jp.hazuki.yuzubrowser.legacy.BrowserState
import jp.hazuki.yuzubrowser.legacy.Constants
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.action.item.AutoPageScrollAction
import jp.hazuki.yuzubrowser.legacy.action.item.OpenOptionsMenuAction
import jp.hazuki.yuzubrowser.legacy.action.item.TabListSingleAction
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionExecutor
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.legacy.action.manager.MenuActionManager
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.adblock.AddAdBlockDialog
import jp.hazuki.yuzubrowser.legacy.bookmark.view.showAddBookmarkDialog
import jp.hazuki.yuzubrowser.legacy.browser.*
import jp.hazuki.yuzubrowser.legacy.browser.openable.BrowserOpenable
import jp.hazuki.yuzubrowser.legacy.download.service.DownloadFile
import jp.hazuki.yuzubrowser.legacy.download.ui.FastDownloadActivity
import jp.hazuki.yuzubrowser.legacy.download.ui.fragment.SaveWebArchiveDialog
import jp.hazuki.yuzubrowser.legacy.favicon.FaviconManager
import jp.hazuki.yuzubrowser.legacy.gesture.GestureManager
import jp.hazuki.yuzubrowser.legacy.history.BrowserHistoryManager
import jp.hazuki.yuzubrowser.legacy.menuwindow.MenuWindow
import jp.hazuki.yuzubrowser.legacy.readitlater.readItLater
import jp.hazuki.yuzubrowser.legacy.search.SearchActivity
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData
import jp.hazuki.yuzubrowser.legacy.tab.BrowserTabManager
import jp.hazuki.yuzubrowser.legacy.tab.TabListLayout
import jp.hazuki.yuzubrowser.legacy.tab.UiTabManager
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.tab.manager.OnWebViewCreatedListener
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabManager
import jp.hazuki.yuzubrowser.legacy.tab.manager.WebViewProvider
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager
import jp.hazuki.yuzubrowser.legacy.toolbar.sub.WebViewFindDialog
import jp.hazuki.yuzubrowser.legacy.toolbar.sub.WebViewFindDialogFactory
import jp.hazuki.yuzubrowser.legacy.toolbar.sub.WebViewPageFastScroller
import jp.hazuki.yuzubrowser.legacy.utils.CrashlyticsUtils
import jp.hazuki.yuzubrowser.legacy.utils.DisplayUtils
import jp.hazuki.yuzubrowser.legacy.utils.WebUtils
import jp.hazuki.yuzubrowser.legacy.utils.extensions.saveArchive
import jp.hazuki.yuzubrowser.legacy.utils.network.ConnectionStateMonitor
import jp.hazuki.yuzubrowser.legacy.utils.ui
import jp.hazuki.yuzubrowser.legacy.webkit.TabType
import jp.hazuki.yuzubrowser.legacy.webkit.WebCustomViewHandler
import jp.hazuki.yuzubrowser.legacy.webkit.WebViewAutoScrollManager
import jp.hazuki.yuzubrowser.legacy.webkit.WebViewProxy
import jp.hazuki.yuzubrowser.legacy.webrtc.WebRtcPermissionHandler
import jp.hazuki.yuzubrowser.legacy.webrtc.core.WebRtcRequest
import jp.hazuki.yuzubrowser.ui.theme.ThemeData
import jp.hazuki.yuzubrowser.ui.widget.PointerView
import jp.hazuki.yuzubrowser.webview.CustomWebHistoryItem
import jp.hazuki.yuzubrowser.webview.CustomWebView
import jp.hazuki.yuzubrowser.webview.WebViewFactory
import jp.hazuki.yuzubrowser.webview.WebViewType
import jp.hazuki.yuzubrowser.webview.listener.OnScrollChangedListener
import jp.hazuki.yuzubrowser.webview.listener.OnScrollableChangeListener
import kotlinx.android.synthetic.main.browser_activity.*
import java.util.*
import javax.inject.Inject

class BrowserActivity : BrowserBaseActivity(), BrowserController, FinishAlertDialog.OnFinishDialogCallBack, OnWebViewCreatedListener, AddAdBlockDialog.OnAdBlockListUpdateListener, WebRtcRequest, SaveWebArchiveDialog.OnSaveWebViewListener, WebViewProvider {

    private val asyncPermissions by lazy { AsyncPermissions(this) }
    private val handler = Handler(Looper.getMainLooper())
    private val actionController = ActionExecutor(this)
    private val iconManager = ActionIconManager(this)
    private val connectionStateMonitor = ConnectionStateMonitor { isAvailable ->
        handler.post {
            for (tab in tabManager.loadedData) {
                tab.mWebView.setNetworkAvailable(isAvailable)
            }
        }
    }

    private val saveTabsRunnable = object : Runnable {
        override fun run() {
            tabManagerIn.saveData()

            val delay = AppData.auto_tab_save_delay.get()
            if (delay > 0)
                handler.postDelayed(this, (delay * 1000).toLong())
        }
    }
    private val takeCurrentTabScreen = Runnable {
        tabManagerIn.currentTabData?.run {
            if (isShotThumbnail) {
                tabManagerIn.forceTakeThumbnail(this)
            }
        }
    }
    private val paddingReset = Runnable {
        tabManagerIn.currentTabData?.let {
            adjustBrowserPadding(it)
        }
    }
    private var scrollableChangeListener = object : OnScrollableChangeListener {
        override fun onScrollableChanged(scrollable: Boolean) {
            val tab = currentTabData ?: return
            adjustBrowserPadding(tab)
        }
    }

    private val scrollChangedListener: OnScrollChangedListener = { webView: CustomWebView, l: Int, t: Int, oldl: Int, oldt: Int ->
        webViewFastScroller.onPageScroll()
        webViewPageFastScroller?.onScrollWebView(webView)
    }

    private lateinit var browserState: BrowserState
    private lateinit var toolbar: Toolbar
    private lateinit var tabManagerIn: UiTabManager
    private lateinit var userActionManager: UserActionManager
    private lateinit var webViewBehavior: WebViewBehavior
    private lateinit var bottomBarBehavior: BottomBarBehavior
    private lateinit var webClient: WebClient
    private lateinit var menuWindow: MenuWindow

    private var isActivityDestroyed = false
    private var isResumed = false
    override var isActivityPaused: Boolean = true
        private set
    private var forceDestroy: Boolean = false

    private var closedTabs: ArrayDeque<Bundle>? = null
    private var webViewFindDialog: WebViewFindDialog? = null
    private var webViewPageFastScroller: WebViewPageFastScroller? = null
    private var webViewAutoScrollManager: WebViewAutoScrollManager? = null
    private var subGestureView: GestureOverlayView? = null
    private var tabListView: TabListLayout? = null
    private var webCustomViewHandler: WebCustomViewHandler? = null
    private var videoLoadingProgressView: View? = null
    private var mouseCursorView: PointerView? = null
    private var findOnPage: WebViewFindDialog? = null
    private var delayAction: Action? = null

    @Inject
    internal lateinit var webViewFactory: WebViewFactory
    @Inject
    internal lateinit var moshi: Moshi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.browser_activity)

        browserState = (applicationContext as BrowserApplication).browserState

        if (browserState.isNeedLoad) {
            AppData.load(this, moshi)
            browserState.isNeedLoad = false
        }

        userActionManager = UserActionManager(this, this, actionController, iconManager)
        tabManagerIn = BrowserTabManager(TabManagerFactory.newInstance(this, webViewFactory), this)
        webClient = WebClient(this, this)

        toolbar = Toolbar(this, superFrameLayoutInfo, this, actionController, iconManager)
        toolbar.addToolbarView(resources)

        webViewBehavior = (webGestureOverlayView.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams).behavior as WebViewBehavior
        bottomBarBehavior = (bottomOverlayLayout.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams).behavior as BottomBarBehavior
        superFrameLayout.setOnImeShownListener { visible ->
            if (isImeShown != visible) {
                isImeShown = visible
                webViewBehavior.isImeShown = visible
                val tab = tabManagerIn.currentTabData
                if (tab != null) {
                    toolbar.notifyChangeWebState(tab)
                    adjustBrowserPadding(tab)
                }
                toolbar.onImeChanged(visible)

                if (!visible && isFullscreenMode) {
                    window.decorView.systemUiVisibility = DisplayUtils.getFullScreenVisibility()
                }
            }
        }
        topToolbar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                topToolbar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                coordinator.setToolbarHeight(topToolbar.height)
                tabManager.onLayoutCreated()
                bottomAlwaysOverlayToolbarPadding.height = bottomAlwaysOverlayToolbar.height
            }
        })

        webViewFastScroller.attachAppBarLayout(coordinator, appbar)
        webGestureOverlayView.setWebFrame(appbar)

        onPreferenceReset()
        tabManagerIn.setOnWebViewCreatedListener(this)

        if (savedInstanceState != null) {
            restoreWebState()
        } else {
            val cookie = CookieManager.getInstance()
            cookie.removeSessionCookies(null)

            tabManagerIn.loadData()
            if (tabManagerIn.size() > 0 && tabManagerIn.currentTabNo >= 0 && tabManagerIn.currentTabNo < tabManagerIn.size()) {
                setCurrentTab(tabManagerIn.currentTabNo)
                toolbar.scrollTabTo(tabManagerIn.currentTabNo)
            }

            handleIntent(intent)
        }

        if (tabManagerIn.isEmpty) {
            val tab = addNewTab(TabType.DEFAULT)
            setCurrentTab(0)
            loadUrl(tab, AppData.home_page.get())
        }

        webViewBehavior.setController(this)

        val menuAction = MenuActionManager.getInstance(applicationContext)
        menuWindow = MenuWindow(this, menuAction.browser_activity.list, actionController, iconManager).apply { setListener { setFullscreenIfEnable() } }

        window.decorView.setOnSystemUiVisibilityChangeListener { setFullscreenIfEnable() }

        webGestureOverlayView.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                requestAdjustWebView()
            }
            userActionManager.onTouchEvent(event)
        }
    }

    override fun onStart() {
        super.onStart()

        if (AppData.auto_tab_save_delay.get() > 0)
            handler.post(saveTabsRunnable)

        if (isActivityPaused) {
            isActivityPaused = false
            tabManagerIn.currentTabData?.let {
                it.mWebView.onResume()
                webClient.resumeWebViewTimers(it)
            }
        } else {
            Logger.w(TAG, "Activity is already started")
        }


        WebViewProxy.setProxy(applicationContext, AppData.proxy_set.get(), AppData.proxy_address.get())

        connectionStateMonitor.enable(this)

        val tab = tabManagerIn.currentTabData
        if (tab != null && tab.mWebView.view.parent == null) {
            setCurrentTab(tabManagerIn.currentTabNo)
        }

        delayAction?.let {
            actionController.run(it)
            delayAction = null
        }
    }

    override fun onResume() {
        super.onResume()
        isResumed = true
    }

    override fun onPostResume() {
        super.onPostResume()
        setFullscreenIfEnable()
        toolbar.resetToolBar()
        if (!checkBrowserPermission()) {
            ui { requestBrowserPermissions(asyncPermissions) }
        }
    }

    override fun onPause() {
        super.onPause()
        isResumed = false
    }

    override fun onStop() {
        super.onStop()
        webClient.onStop()
        tabListView?.closeSnackBar()

        tabManagerIn.saveData()
        handler.removeCallbacks(saveTabsRunnable)
        FaviconManager.getInstance(applicationContext).save()

        if (isActivityPaused) {
            Logger.w(TAG, "Activity is already stopped")
            return
        }

        if (AppData.pause_web_background.get()) {
            isActivityPaused = true

            val tab = tabManagerIn.currentTabData
            if (tab != null) {
                tab.mWebView.onPause()
                webClient.pauseWebViewTimers(tab)
            }
        }

        WebViewProxy.resetProxy(applicationContext)

        connectionStateMonitor.disable(this)
    }

    override fun onDestroy() {
        Logger.d(TAG, "onDestroy()")
        super.onDestroy()
        destroy()
        if (AppData.kill_process.get() || forceDestroy)
            Process.killProcess(Process.myPid())
    }

    private fun destroy() {
        if (isActivityDestroyed)
            return

        webCustomViewHandler?.run {
            hideCustomView(this@BrowserActivity)
            webCustomViewHandler = null
        }
        webFrameLayout.removeAllViews()
        webGestureOverlayView.removeAllViews()
        tabManagerIn.destroy()
        webClient.destroy()
        isActivityDestroyed = true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus)
            setFullscreenIfEnable()
    }

    override val isLoadThemeData = true

    private fun setFullscreenIfEnable() {
        if (isFullscreenMode) {
            window.decorView.systemUiVisibility = DisplayUtils.getFullScreenVisibility()
        }
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }

    override fun onSaveInstanceState(bundle: Bundle?) {
        super.onSaveInstanceState(bundle)
        saveWebState()
    }

    private fun saveWebState(): Boolean {
        if (isActivityDestroyed)
            return false
        tabListView?.run {
            closeSnackBar()
        }
        tabManagerIn.saveData()
        return true
    }

    private fun restoreWebState() {
        tabManagerIn.loadData()
        if (!tabManagerIn.isEmpty)
            toolbar.scrollTabTo(tabManagerIn.currentTabNo)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toolbar.onActivityConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        videoLoadingProgressView = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            BrowserController.REQUEST_WEB_UPLOAD -> webClient.webUploadResult(resultCode, data)
            BrowserController.REQUEST_SEARCHBOX -> {
                if (resultCode != RESULT_OK || data == null) return
                val query = data.getStringExtra(SearchActivity.EXTRA_QUERY)
                val searchUrl = data.getStringExtra(SearchActivity.EXTRA_SEARCH_URL)

                if (TextUtils.isEmpty(query)) return

                val url: String
                url = when (data.getIntExtra(SearchActivity.EXTRA_SEARCH_MODE, SearchActivity.SEARCH_MODE_AUTO)) {
                    SearchActivity.SEARCH_MODE_URL -> WebUtils.makeUrl(query)
                    SearchActivity.SEARCH_MODE_WORD -> WebUtils.makeSearchUrlFromQuery(query, searchUrl, "%s")
                    else -> WebUtils.makeUrlFromQuery(query, searchUrl, "%s")
                }
                val appdata = data.getBundleExtra(SearchActivity.EXTRA_APP_DATA)
                val target = appdata.getInt(EXTRA_DATA_TARGET, -1)
                val tab = tabManagerIn.get(target)
                if (data.getBooleanExtra(SearchActivity.EXTRA_OPEN_NEW_TAB, false) || tab == null)
                    openInNewTab(url, TabType.DEFAULT)
                else
                    loadUrl(tab, url)
            }
            BrowserController.REQUEST_BOOKMARK, BrowserController.REQUEST_HISTORY -> {
                if (resultCode != RESULT_OK || data == null) return
                val openable = data.getParcelableExtra<BrowserOpenable>(BrowserManager.EXTRA_OPENABLE) ?: return
                openable.open(this)
            }
            BrowserController.REQUEST_SETTING -> {
                AppData.load(applicationContext, moshi)
                onPreferenceReset()
            }
            BrowserController.REQUEST_USERAGENT -> {
                if (resultCode != RESULT_OK || data == null) return
                val ua = data.getStringExtra(Intent.EXTRA_TEXT) ?: return
                val tab = tabManagerIn.currentTabData
                tab.mWebView.webSettings.userAgentString = ua
                tab.mWebView.reload()
            }
            BrowserController.REQUEST_DEFAULT_USERAGENT -> {
                if (resultCode != RESULT_OK || data == null) return
                val ua = data.getStringExtra(Intent.EXTRA_TEXT) ?: return
                AppData.user_agent.set(ua)
                AppData.commit(this, AppData.user_agent)
                for (tabData in tabManagerIn.loadedData) {
                    tabData.mWebView.webSettings.userAgentString = ua
                    tabData.mWebView.reload()
                }
            }
            BrowserController.REQUEST_USERJS_SETTING -> webClient.resetUserScript(AppData.userjs_enable.get())
            BrowserController.REQUEST_WEB_ENCODE_SETTING -> {
                if (resultCode != RESULT_OK || data == null) return
                val encoding = data.getStringExtra(Intent.EXTRA_TEXT) ?: return
                val tab = tabManagerIn.currentTabData
                tab.mWebView.webSettings.defaultTextEncodingName = encoding
                tab.mWebView.reload()
            }
            BrowserController.REQUEST_SHARE_IMAGE -> {
                if (resultCode != RESULT_OK || data == null) return
                var uri = data.data ?: return
                val mineType = data.getStringExtra(FastDownloadActivity.EXTRA_MINE_TYPE)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && uri.scheme == "file") {
                    val provider = (applicationContext as BrowserApplication).providerManager.downloadFileProvider
                    uri = provider.getUriFromPath(uri.path ?: "")
                }

                val open = Intent(Intent.ACTION_SEND)
                if (mineType != null)
                    open.type = mineType
                else
                    open.type = "image/*"

                open.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                open.putExtra(Intent.EXTRA_STREAM, uri)
                open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(open, getText(R.string.share)))
            }
            BrowserController.REQUEST_ACTION_LIST -> {
                if (resultCode != RESULT_OK || data == null) return
                val action = data.getParcelableExtra<Action>(ActionActivity.EXTRA_ACTION)
                if (action == null) {
                    Logger.w(TAG, "Action is null")
                    return
                }
                if (isResumed)
                    actionController.run(action)
                else
                    delayAction = action
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> if (AppData.volume_default_playing.get() && (getSystemService(Context.AUDIO_SERVICE) as AudioManager).isMusicActive) {
                return super.onKeyDown(keyCode, event)

            } else if (webCustomViewHandler?.isCustomViewShowing != true) {
                if (userActionManager.onVolumeKey(true))
                    return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> if (AppData.volume_default_playing.get() && (getSystemService(Context.AUDIO_SERVICE) as AudioManager).isMusicActive) {
                return super.onKeyDown(keyCode, event)

            } else if (webCustomViewHandler?.isCustomViewShowing != true) {
                if (userActionManager.onVolumeKey(false))
                    return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> if (webCustomViewHandler?.isCustomViewShowing != true) {
                if (userActionManager.isVolumeActionNotEmpty(true))
                    return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> if (webCustomViewHandler?.isCustomViewShowing != true) {
                if (userActionManager.isVolumeActionNotEmpty(false))
                    return true
            }
            KeyEvent.KEYCODE_CAMERA ->
                if (!event.isCanceled && userActionManager.onCameraKey()) {
                    return true
                }
            KeyEvent.KEYCODE_MENU -> if (!event.isCanceled) {
                if (menuWindow.isShowing) {
                    menuWindow.dismiss()
                } else {
                    menuWindow.show(findViewById(R.id.superFrameLayout), OpenOptionsMenuAction.getGravity(AppData.menu_btn_list_mode.get()))
                }
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onBackKeyPressed() {
        if (menuWindow.isShowing) {
            menuWindow.dismiss()
        } else if (webCustomViewHandler?.isCustomViewShowing == true) {
            webCustomViewHandler!!.hideCustomView(this)
        } else if (subGestureView != null) {
            superFrameLayout.removeView(subGestureView)
            subGestureView = null
        } else if (mouseCursorView?.backFinish == true) {
            mouseCursorView!!.setView(null)
            webFrameLayout.removeView(mouseCursorView)
            mouseCursorView = null
        } else if (tabListView != null) {
            tabListView!!.close()
        } else if (findOnPage != null && findOnPage!!.isVisible) {
            findOnPage!!.hide()
        } else if (webViewPageFastScroller != null) {
            webViewPageFastScroller!!.close()
        } else if (webViewAutoScrollManager != null) {
            webViewAutoScrollManager!!.stop()
        } else if (tabManagerIn.currentTabData?.mWebView?.canGoBack() == true) {
            tabManagerIn.currentTabData.mWebView.goBack()
            handler.postDelayed(takeCurrentTabScreen, 500)
            handler.postDelayed(paddingReset, 50)
        } else {
            userActionManager.onBackKey()
        }
    }

    override fun onBackKeyLongPressed() {
        userActionManager.onBackKeyLong()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_SEARCH && !event.isCanceled) {
            userActionManager.onSearchKey()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun adjustBrowserPadding(tab: MainTabData) {
        webViewBehavior.adjustWebView(tab, topToolbar.height + bottomOverlayLayout.height)
    }

    private fun handleIntent(intent: Intent?): Boolean {
        if (intent == null) return false

        val action = intent.action
        setIntent(Intent())
        if (action == null) return false
        if (Constants.intent.ACTION_FINISH == action) {
            forceDestroy = intent.getBooleanExtra(Constants.intent.EXTRA_FORCE_DESTROY, false)
            browserState.isNeedLoad = true
            if (parent == null) {
                recreate()
            } else {
                val restart = Intent(this, BrowserActivity::class.java)
                finish()
                startActivity(restart)
            }
            return false
        }
        if (Constants.intent.ACTION_NEW_TAB == action) {
            tabListView?.close()
            openInNewTab(AppData.home_page.get(), TabType.DEFAULT)
            return false
        }
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) {
            return false
        }
        if (Intent.ACTION_VIEW == action) {
            var url = intent.dataString
            val window = intent.getBooleanExtra(EXTRA_WINDOW_MODE, false)
            if (url.isNullOrEmpty())
                url = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!url.isNullOrEmpty())
                openInNewTab(url, if (window) TabType.WINDOW else TabType.INTENT, intent.getBooleanExtra(EXTRA_SHOULD_OPEN_IN_NEW_TAB, false))
            else {
                Logger.w(TAG, "ACTION_VIEW : url is null or empty.")
                return false
            }
        } else if (Constants.intent.ACTION_OPEN_DEFAULT == action) {
            val url = intent.dataString ?: return false
            openInNewTab(url, TabType.DEFAULT)
        } else {
            return false
        }
        tabListView?.close()
        return true
    }

    private fun onPreferenceReset() {
        toolbar.onPreferenceReset()
        tabManagerIn.onPreferenceReset()
        userActionManager.onPreferenceReset()
        CrashlyticsUtils.setWebViewMode()

        if (ThemeData.createInstance(applicationContext, AppData.theme_setting.get()) != null) {
            val themeData = ThemeData.getInstance()

            toolbar.onThemeChanged(themeData)
            userActionManager.onThemeChanged(themeData)
            toolbar.notifyChangeWebState()

            if (themeData.statusBarColor != 0) {
                window.run {
                    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    statusBarColor = themeData.statusBarColor
                    decorView.systemUiVisibility = ThemeData.getSystemUiVisibilityFlag()
                }
            }

            if (themeData.scrollbarAccentColor != 0) {
                webViewFastScroller.handlePressedColor = themeData.scrollbarAccentColor
            } else if (themeData.tabAccentColor != 0) {
                webViewFastScroller.handlePressedColor = themeData.tabAccentColor
            }
        }

        menuWindow = MenuWindow(this, MenuActionManager.getInstance(applicationContext).browser_activity.list, actionController, iconManager)

        webClient.onPreferenceReset()
        isEnableQuickControl = AppData.qc_enable.get()
        requestedOrientation = AppData.oritentation.get()
        isFullscreenMode = AppData.fullscreen.get()
        isEnableMultiFingerGesture = AppData.multi_finger_gesture.get()
        userActionManager.setEnableGesture(webGestureOverlayView)

        if (AppData.keep_screen_on.get())
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val touchScrollbar = AppData.touch_scrollbar.get()
        if (touchScrollbar >= 0) {
            webViewFastScroller.setScrollEnabled(true)
            webViewFastScroller.isShowLeft = touchScrollbar == 1
        } else {
            webViewFastScroller.setScrollEnabled(false)
        }

        ErrorReport.setDetailedLog(AppData.detailed_log.get())
    }

    override fun getTabOrNull(target: Int): MainTabData? = tabManagerIn[target]

    override fun getTabOrNull(target: CustomWebView): MainTabData? = tabManagerIn[target]

    override fun indexOf(id: Long): Int = tabManagerIn.indexOf(id)

    override fun setCurrentTab(target: Int) {
        val oldTab = tabManagerIn.currentTabData
        val newTab = tabManagerIn[target]

        tabManagerIn.setCurrentTab(target, oldTab, newTab)
        tabManagerIn.saveData()

        webViewFindDialog?.run {
            if (isVisible)
                hide()
        }

        webViewPageFastScroller?.close()
        webViewAutoScrollManager?.stop()

        oldTab?.run {
            mWebView.setOnMyCreateContextMenuListener(null)
            mWebView.setWebViewTouchDetector(null)
            mWebView.scrollableChangeListener = null
            webFrameLayout.removeView(mWebView.view)
            webViewBehavior.setWebView(null)
            webViewFastScroller.detachWebView()
        }

        newTab.mWebView.let {
            it.resumeTimers()
            it.onResume()
            (it.view.parent as? ViewGroup)?.removeView(it.view)
            webFrameLayout.addView(it.view, 0)
            webViewBehavior.setWebView(it)
            webViewFastScroller.attachWebView(it)

            it.setOnMyCreateContextMenuListener(userActionManager.onCreateContextMenuListener)
            //TODO Rewrite
            it.setMyOnScrollChangedListener(scrollChangedListener)
            it.scrollableChangeListener = scrollableChangeListener
            userActionManager.setGestureDetector(it)
        }
        CookieManager.getInstance().setAcceptCookie(newTab.isEnableCookie)

        if (oldTab == null || oldTab.mWebView.isScrollable != newTab.mWebView.isScrollable) {
            adjustBrowserPadding(newTab)
        }
    }

    private fun addTab(index: Int, tab: MainTabData) {
        tabManager.addTab(index, tab)
        if (AppData.save_closed_tab.get()) {
            closedTabs?.poll()
        }
    }

    override fun removeTab(target: Int, error: Boolean, destroy: Boolean): Boolean {
        if (tabManagerIn.size() <= 1) { // Last tab
            return false
        }

        val oldData = tabManagerIn[target]

        if (oldData.isPinning) {
            if (error) Toast.makeText(applicationContext, R.string.pinned_tab_warning, Toast.LENGTH_SHORT).show()
            return true
        }

        if (tabManagerIn.currentTabData == oldData) {
            setCurrentTab(getNewTabNo(target, oldData))
        }

        val oldWeb = oldData.mWebView

        if (AppData.save_closed_tab.get()) {
            val outState = Bundle()
            oldWeb.saveState(outState)
            outState.putInt(TAB_TYPE, oldData.tabType)
            if (closedTabs == null)
                closedTabs = ArrayDeque()
            closedTabs!!.push(outState)
        }

        oldWeb.setEmbeddedTitleBarMethod(null)

        tabManagerIn.remove(target)
        toolbar.notifyChangeWebState()

        if (destroy)
            oldWeb.destroy()
        return true
    }

    private fun getNewTabNo(no: Int, oldData: MainTabData): Int {
        if (AppData.move_to_parent.get() && oldData.tabType == TabType.WINDOW && oldData.parent != 0L) {
            val newNo = tabManagerIn.searchParentTabNo(oldData.parent)
            if (newNo >= 0) {
                return newNo
            }
        }
        return if (AppData.move_to_left_tab.get()) {
            if (no == 0) 1 else no - 1
        } else {
            if (no == tabManagerIn.lastTabNo) no - 1 else no + 1
        }
    }

    override fun swapTab(i: Int, j: Int) {
        tabManagerIn.swap(i, j)
    }

    override fun loadUrl(url: String, target: Int) {
        loadUrl(tabManagerIn.currentTabData, url, target, TabType.WINDOW)
    }

    override fun loadUrl(tab: MainTabData, url: String, shouldOpenInNewTab: Boolean) {
        webClient.loadUrl(tab, url, shouldOpenInNewTab)
    }

    override fun loadUrl(tab: MainTabData, url: String, target: Int, type: Int) {
        webClient.loadUrl(tab, url, target, type)
    }

    override fun onAdBlockListUpdate() {
        webClient.updateAdBlockList()
    }

    override fun showTabList(action: TabListSingleAction) {
        if (tabListView != null) return

        tabListView = TabListLayout(this, action.mode, action.isLeftButton, action.lastTabMode).apply {
            setTabManager(tabManagerIn)
            setCallback(object : TabListLayout.Callback {
                override fun requestTabListClose() {
                    superFrameLayout.removeView(tabListView)
                    tabListView = null
                }

                override fun requestShowTabHistory(no: Int) {
                    showTabHistory(no)
                }

                override fun requestAddTab(index: Int, data: MainTabData) {
                    addTab(index, data)
                }

                override fun requestSelectTab(no: Int) {
                    setCurrentTab(no)
                    toolbar.scrollTabTo(no)
                }

                override fun requestAddTab() {
                    openInNewTab(AppData.home_page.get(), TabType.DEFAULT)
                }

                override fun requestMoveTab(positionFrom: Int, positionTo: Int) {
                    moveTab(positionFrom, positionTo)
                }

                override fun requestRemoveTab(no: Int, destroy: Boolean) {
                    removeTab(no, false, destroy)
                }

                override fun requestCloseAllTab() {
                    openInNewTab(AppData.home_page.get(), TabType.DEFAULT)
                    for (i in tabManagerIn.lastTabNo - 1 downTo 0) {
                        removeTab(i, false)
                    }
                }

                override fun requestFinish(alert: Boolean) {
                    if (alert) {
                        finishAlert(-1)
                    } else {
                        finishQuick(-1)
                    }
                }
            })
        }
        superFrameLayout.addView(tabListView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    override fun showTabHistory(target: Int) {
        val tab = tabManagerIn[target]
        val historyList = tab.mWebView.copyMyBackForwardList()

        val adapter = object : ArrayAdapter<CustomWebHistoryItem>(applicationContext, 0, historyList) {
            override fun getView(position: Int, view: View?, parent: ViewGroup): View {
                val v = view ?: layoutInflater.inflate(R.layout.tab_history_list_item, parent, false)

                val item = getItem(position)
                if (item != null) {
                    (v.findViewById<View>(R.id.siteTitleText) as TextView).text = item.title
                    (v.findViewById<View>(R.id.siteUrlText) as TextView).text = item.url
                    (v.findViewById<View>(R.id.siteIconImageView) as ImageView).setImageBitmap(item.favicon)
                }
                return v
            }
        }

        val listView = ListView(this).also { it.adapter = adapter }

        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.tab_history)
                .setView(listView)
                .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            val next = position - historyList.current
            if (tab.mWebView.canGoBackOrForward(next)) {
                if (tab.isNavLock) {
                    val item = historyList.getBackOrForward(next)
                    if (item != null)
                        performNewTabLink(BrowserManager.LOAD_URL_TAB_NEW_RIGHT, tab, item.url, TabType.DEFAULT)
                    else
                        tab.mWebView.goBackOrForward(next)
                } else {
                    tab.mWebView.goBackOrForward(next)
                }
                handler.postDelayed(paddingReset, 50)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun restoreTab() {
        val bundle = closedTabs?.poll()
        if (bundle == null) {
            if (AppData.save_closed_tab.get())
                Toast.makeText(applicationContext, R.string.tab_restored_failed, Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(applicationContext, R.string.tab_restored_setting_error, Toast.LENGTH_LONG).show()
        } else {
            openInNewTab(bundle)
            Toast.makeText(applicationContext, R.string.tab_restored_succeed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun showSearchBox(query: String, target: Int, openNewTab: Boolean, reverse: Boolean) {
        val data = Bundle().apply {
            putInt(EXTRA_DATA_TARGET, target)
        }

        val intent = Intent(applicationContext, SearchActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(SearchActivity.EXTRA_QUERY, query)
            putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, isFullscreenMode && DisplayUtils.isNeedFullScreenFlag())
            putExtra(SearchActivity.EXTRA_REVERSE, reverse)
            putExtra(SearchActivity.EXTRA_APP_DATA, data)
            putExtra(SearchActivity.EXTRA_OPEN_NEW_TAB, openNewTab)
        }

        startActivity(intent, BrowserController.REQUEST_SEARCHBOX)
    }

    override fun showSubGesture() {
        val manager = GestureManager.getInstance(applicationContext, GestureManager.GESTURE_TYPE_SUB)

        subGestureView = GestureOverlayView(this).apply {
            isEventsInterceptionEnabled = true
            gestureStrokeType = GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE
            gestureStrokeWidth = 8.0f
            setBackgroundColor(0x70000000)
            addOnGesturePerformedListener { _, gesture ->
                manager.recognize(gesture)?.let {
                    actionController.run(it)
                    superFrameLayout.removeView(subGestureView)
                    subGestureView = null
                }
            }
        }

        superFrameLayout.addView(subGestureView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    override fun showMenu(button: View?, action: OpenOptionsMenuAction) {
        if (button != null) {
            menuWindow.showAsDropDown(button)
        } else {
            menuWindow.show(superFrameLayout, action.gravity)
        }
    }

    override fun showCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {
        if (webCustomViewHandler == null) {
            webCustomViewHandler = WebCustomViewHandler(fullscreenLayout)
        }
        webCustomViewHandler!!.showCustomView(this, view, AppData.web_customview_oritentation.get(), callback)
    }

    override fun hideCustomView() {
        webCustomViewHandler?.hideCustomView(this)
    }

    override fun requestAdjustWebView() {
        val data = tabManagerIn.currentTabData ?: return
        data.mWebView.computeVerticalScrollRangeMethod()
        adjustBrowserPadding(data)
    }

    override fun expandToolbar() {
        appBarLayout.setExpanded(true, false)
        bottomBarBehavior.setExpanded(true)
    }

    override fun getVideoLoadingProgressView(): View? {
        if (videoLoadingProgressView == null) {
            videoLoadingProgressView = View.inflate(this, R.layout.video_loading, null)
        }
        return videoLoadingProgressView
    }

    override fun finishAlert(clearTabNo: Int) {
        FinishAlertDialog(this)
                .setPositiveButton(android.R.string.yes)
                .setNegativeButton(android.R.string.no)
                .setNeutralButton(R.string.minimize)
                .setClearTabNo(clearTabNo)
                .show(supportFragmentManager)
    }

    override fun onFinishPositiveButtonClicked(clearTabNo: Int, newSetting: Int) {
        finishQuick(clearTabNo, newSetting)
    }

    override fun onFinishNeutralButtonClicked(clearTabNo: Int, newSetting: Int) {
        if (clearTabNo >= 0 && tabManagerIn.size() >= 2)
            removeTab(clearTabNo)
        moveTaskToBack(true)
    }

    override fun finishQuick(clearTabNo: Int, finish_clear: Int) {
        if (clearTabNo >= 0) {
            if (tabManagerIn.size() >= 2)
                removeTab(clearTabNo)
        }

        if (finish_clear and 0x01 != 0) {
            BrowserManager.clearCache(applicationContext)
        }
        if (finish_clear and 0x02 != 0) {
            CookieManager.getInstance().removeAllCookies(null)
        }
        if (finish_clear and 0x04 != 0) {
            BrowserManager.clearWebDatabase()
        }
        if (finish_clear and 0x08 != 0) {
            WebViewDatabase.getInstance(applicationContext).clearHttpAuthUsernamePassword()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && finish_clear and 0x10 != 0) {
            @Suppress("DEPRECATION")
            WebViewDatabase.getInstance(applicationContext).clearFormData()
        }
        if (finish_clear and 0x20 != 0) {
            val manager = BrowserHistoryManager.getInstance(this)
            manager.deleteAll()
        }
        if (finish_clear and 0x40 != 0) {
            contentResolver.delete((applicationContext as BrowserApplication).providerManager.suggestProvider.uriLocal, null, null)
        }
        if (finish_clear and 0x80 != 0) {
            BrowserManager.clearGeolocation()
        }
        if (finish_clear and 0x100 != 0) {
            FaviconManager.getInstance(applicationContext).clear()
        }

        handler.removeCallbacks(saveTabsRunnable)
        if (AppData.save_last_tabs.get() && finish_clear and 0x1000 == 0) {
            tabManagerIn.saveData()
        } else if (AppData.save_pinned_tabs.get()) {
            tabManagerIn.clearExceptPinnedTab()
            tabManagerIn.saveData()
        } else {
            tabManagerIn.clear()
        }

        browserState.isNeedLoad = true
        finish()
    }

    private fun addNewTab(@TabType type: Int): MainTabData {
        return addNewTab(webViewFactory.mode, type)
    }

    private fun addNewTab(@WebViewType cacheType: Int, @TabType type: Int): MainTabData {
        val web = makeWebView(cacheType)
        // TODO: Restore this when Google fixes the bug where the WebView is blank after calling onPause followed by onResume.
        //        if (AppData.pause_web_tab_change.get())
        //            web.onPause();
        val tab = tabManagerIn.add(web, type)
        if (ThemeData.isEnabled())
            tab.onMoveTabToBackground(resources, theme)

        toolbar.notifyChangeWebState()
        return tab
    }

    private fun moveTab(a: Int, b: Int): Boolean {
        if (a == b) return false
        tabManagerIn.move(a, b)
        return true
    }

    override fun makeWebView(@WebViewType cacheType: Int): CustomWebView {
        val web = webViewFactory.create(this, cacheType)
        web.view.id = View.generateViewId()
        web.webView.isFocusableInTouchMode = true
        web.webView.isFocusable = true
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            web.webView.isDrawingCacheEnabled = false
            web.webView.setWillNotCacheDrawing(true)
        }
        webClient.initWebSetting(web)
        val enableCookie = if (AppData.private_mode.get())
            AppData.accept_cookie.get() && AppData.accept_cookie_private.get()
        else
            AppData.accept_cookie.get()
        web.setAcceptThirdPartyCookies(CookieManager.getInstance(), enableCookie && AppData.accept_third_cookie.get())
        return web
    }

    private fun openInNewTab(webTransport: WebView.WebViewTransport) {
        webTransport.webView = openNewTab(TabType.WINDOW).mWebView.webView
    }

    override fun openInNewTab(tab: MainTabData) {
        val bundle = Bundle()
        tab.mWebView.saveState(bundle)
        openNewTab(webViewFactory.getMode(bundle), bundle.getInt(TAB_TYPE, tab.tabType)).run {
            mWebView.restoreState(bundle)
            mWebView.identityId = id
            url = tab.url
            title = tab.title
            parent = tab.parent
        }
    }

    override fun openInNewTab(url: String, type: Int, shouldOpenInNewTab: Boolean) {
        loadUrl(openNewTab(type), url, shouldOpenInNewTab)
    }

    private fun openInNewTab(state: Bundle) {
        openNewTab(webViewFactory.getMode(state), state.getInt(TAB_TYPE, 0)).mWebView.restoreState(state)
    }

    private fun openNewTab(@TabType type: Int): MainTabData {
        return openNewTab(webViewFactory.mode, type)
    }

    private fun openNewTab(@WebViewType cacheType: Int, @TabType type: Int): MainTabData {
        val tab = addNewTab(cacheType, type)
        setCurrentTab(tabManagerIn.lastTabNo)
        toolbar.scrollTabRight()
        return tab
    }

    override fun openInBackground(url: String, type: Int) {
        val tab = addNewTab(type)
        tab.setUpBgTab()
        loadUrl(tab, url)
    }

    private fun openInBackground(webTransport: WebView.WebViewTransport) {
        val data = addNewTab(TabType.WINDOW)
        data.setUpBgTab()
        webTransport.webView = data.mWebView.webView
    }

    private fun openRightNewTab(@TabType type: Int): MainTabData {
        val tab = addNewTab(type)
        val from = tabManagerIn.lastTabNo
        val to = tabManagerIn.currentTabNo + 1
        setCurrentTab(from)
        if (!moveTab(from, to))
            toolbar.scrollTabRight()
        return tab
    }

    override fun openInRightNewTab(url: String, type: Int) {
        loadUrl(openRightNewTab(type), url)
    }

    private fun openInRightNewTab(webTransport: WebView.WebViewTransport) {
        val webView = openRightNewTab(TabType.WINDOW).mWebView.webView
        webTransport.webView = webView
    }

    private fun openRightBgTab(@TabType type: Int): MainTabData {
        val tab = addNewTab(type)
        val from = tabManagerIn.lastTabNo
        val to = tabManagerIn.currentTabNo + 1
        moveTab(from, to)
        tab.setUpBgTab()
        return tab
    }

    override fun openInRightBgTab(url: String, type: Int) {
        loadUrl(openRightBgTab(type), url)
    }

    private fun openInRightBgTab(webTransport: WebView.WebViewTransport) {
        webTransport.webView = openRightBgTab(TabType.WINDOW).mWebView.webView
    }

    override fun checkNewTabLink(perform: Int, transport: WebView.WebViewTransport): Boolean {
        when (perform) {
            BrowserManager.LOAD_URL_TAB_NEW -> {
                openInNewTab(transport)
                return true
            }
            BrowserManager.LOAD_URL_TAB_BG -> {
                openInBackground(transport)
                return true
            }
            BrowserManager.LOAD_URL_TAB_NEW_RIGHT -> {
                openInRightNewTab(transport)
                return true
            }
            BrowserManager.LOAD_URL_TAB_BG_RIGHT -> {
                openInRightBgTab(transport)
                return true
            }
            else -> throw IllegalArgumentException("Unknown perform:$perform")
        }
    }

    override fun performNewTabLink(perform: Int, tab: MainTabData, url: String, type: Int): Boolean {
        when (perform) {
            BrowserManager.LOAD_URL_TAB_CURRENT -> {
                loadUrl(tab, url)
                return true
            }
            BrowserManager.LOAD_URL_TAB_NEW -> {
                openInNewTab(url, type)
                return true
            }
            BrowserManager.LOAD_URL_TAB_BG -> {
                openInBackground(url, type)
                return true
            }
            BrowserManager.LOAD_URL_TAB_NEW_RIGHT -> {
                openInRightNewTab(url, type)
                return true
            }
            BrowserManager.LOAD_URL_TAB_BG_RIGHT -> {
                openInRightBgTab(url, type)
                return true
            }
            else -> throw IllegalArgumentException("Unknown perform:$perform")
        }
    }

    override fun addBookmark(tab: MainTabData) {
        showAddBookmarkDialog(this, supportFragmentManager, tab.title,
                tab.url ?: tab.mWebView.url ?: "")
    }

    override fun savePage(tab: MainTabData) {
        readItLater(this, contentResolver, tab.originalUrl, tab.mWebView)
    }

    override fun onSaveWebViewToFile(root: androidx.documentfile.provider.DocumentFile, file: DownloadFile, webViewNo: Int) {
        tabManagerIn[webViewNo].mWebView.saveArchive(root, file)
    }

    override fun startActivity(intent: Intent, @RequestCause cause: Int) {
        startActivityForResult(intent, cause)
    }

    override fun onWebViewCreated(tab: MainTabData) {
        webClient.checkPatternMatch(tab, tab.url, true)
    }

    override fun showActionName(text: String?) {
        if (text != null) {
            actionNameTextView.visibility = View.VISIBLE
            actionNameTextView.text = text
        }
    }

    override fun hideActionName() {
        actionNameTextView.visibility = View.GONE
    }

    override val resourcesByInfo: Resources
        get() = resources

    override val themeByInfo: Resources.Theme
        get() = theme

    override val applicationContextInfo: Context
        get() = applicationContext

    override var isImeShown: Boolean = false

    override val tabManager: TabManager
        get() = tabManagerIn

    override val appBarLayout: AppBarLayout
        get() = appbar

    override val superFrameLayoutInfo: androidx.coordinatorlayout.widget.CoordinatorLayout
        get() = superFrameLayout

    override val activity: AppCompatActivity
        get() = this

    override val toolbarManager: ToolbarManager
        get() = toolbar

    override val actionNameArray: ActionNameArray by lazy { ActionNameArray(this) }

    override var requestedOrientationByCtrl: Int
        get() = requestedOrientation
        set(value) {
            requestedOrientation = value
        }

    override var renderingMode: Int
        get() = webClient.renderingMode
        set(value) {
            webClient.renderingMode = value
        }

    override val pagePaddingHeight: Int
        get() = topAlwaysToolbar.height + bottomAlwaysToolbar.height +
                (if (toolbarPadding.visibility == View.VISIBLE) toolbarPadding.height else 0)

    override var isFullscreenMode: Boolean = false
        set(enable) {
            if (field == enable) return

            field = enable

            toolbar.onFullscreenChanged(enable)

            if (enable) {
                val visibility = DisplayUtils.getFullScreenVisibility()
                window.decorView.systemUiVisibility = visibility
                menuWindow.setSystemUiVisibility(visibility)
                if (DisplayUtils.isNeedFullScreenFlag())
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                val flag = ThemeData.getSystemUiVisibilityFlag()
                window.decorView.systemUiVisibility = flag
                menuWindow.setSystemUiVisibility(flag)
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }

    override var isPrivateMode: Boolean = false
        set(isPrivate) {
            if (isPrivate == field) return

            field = isPrivate
            val noPrivate = !isPrivate
            val enableCookie = if (isPrivate)
                AppData.accept_cookie.get() && AppData.accept_cookie_private.get()
            else
                AppData.accept_cookie.get()

            webClient.isEnableHistory = noPrivate && AppData.save_history.get()
            CookieManager.getInstance().setAcceptCookie(enableCookie)

            tabManagerIn.loadedData.forEach {
                val settings = it.mWebView.webSettings

                it.mWebView.setAcceptThirdPartyCookies(
                        CookieManager.getInstance(), enableCookie && AppData.accept_third_cookie.get())

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    @Suppress("DEPRECATION")
                    settings.saveFormData = noPrivate && AppData.save_formdata.get()
                }
                settings.databaseEnabled = noPrivate && AppData.web_db.get()
                settings.domStorageEnabled = noPrivate && AppData.web_dom_db.get()
                settings.geolocationEnabled = noPrivate && AppData.web_geolocation.get()
                settings.appCacheEnabled = noPrivate && AppData.web_app_cache.get()
                settings.setAppCachePath(appCacheFilePath)
            }

            toolbar.notifyChangeWebState()
        }

    override val isEnableFastPageScroller: Boolean
        get() = webViewPageFastScroller != null

    override fun showFastPageScroller(target: Int) {
        if (webViewPageFastScroller == null) {
            webViewPageFastScroller = WebViewPageFastScroller(this).apply {
                toolbar.bottomToolbarAlwaysLayout.addView(this)
                show(tabManagerIn.currentTabData.mWebView)
                setOnEndListener {
                    toolbar.bottomToolbarAlwaysLayout.removeView(webViewPageFastScroller)
                    webViewPageFastScroller = null
                    true
                }
            }
        }
    }

    override fun closeFastPageScroller() {
        webViewPageFastScroller?.close()
    }

    override val isEnableAutoScroll: Boolean
        get() = webViewAutoScrollManager != null

    override fun startAutoScroll(target: Int, action: AutoPageScrollAction) {
        if (webViewAutoScrollManager == null) {
            webViewAutoScrollManager = WebViewAutoScrollManager().apply {
                setOnStopListener { webViewAutoScrollManager = null }
                start(tabManagerIn.currentTabData.mWebView, action.scrollSpeed)
            }
        }
    }

    override fun stopAutoScroll() {
        webViewAutoScrollManager?.stop()
    }

    override val isEnableMousePointer: Boolean
        get() = mouseCursorView != null

    override fun showMousePointer(isBackFinish: Boolean) {
        if (mouseCursorView == null) {
            mouseCursorView = PointerView(this).apply {
                backFinish = isBackFinish
                setView(tabManagerIn.currentTabData.mWebView.view)
                webFrameLayout.addView(this, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
        }
    }

    override fun closeMousePointer() {
        mouseCursorView?.run {
            setView(null)
            webFrameLayout.removeView(this)
            mouseCursorView = null
        }
    }

    private val webRtcHandler by lazy { WebRtcPermissionHandler(applicationContext, asyncPermissions) }

    override suspend fun requestPermissions(permissions: List<String>): Boolean {
        return webRtcHandler.requestPermissions(permissions)
    }

    override fun requestPagePermission(host: String, resources: Array<String>, onGrant: (Boolean) -> Unit) {
        AlertDialog.Builder(this)
                .setTitle(R.string.permission_request)
                .setMessage(getRequestRtcList(host, resources))
                .setPositiveButton(R.string.allow) { _, _ -> onGrant(true) }
                .setNegativeButton(R.string.block) { _, _ -> onGrant(false) }
                .setOnCancelListener { onGrant(false) }
                .show()
    }

    private fun getRequestRtcList(host: String, resources: Array<String>): String {
        val builder = StringBuilder()
        resources.forEach {
            when (it) {
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> builder.append(getString(R.string.mic)).append('\n')
                PermissionRequest.RESOURCE_MIDI_SYSEX -> builder.append(getString(R.string.midi_device)).append('\n')
                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> builder.append(getString(R.string.protected_media_id)).append('\n')
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> builder.append(getString(R.string.camera)).append('\n')
            }
        }
        return getString(R.string.permission_request_mes, host, builder.toString())
    }

    override val webRtcRequest: WebRtcRequest
        get() = this

    override var isEnableFindOnPage: Boolean
        get() = findOnPage?.isVisible == true
        set(enable) {
            if (enable == isEnableFindOnPage) return

            if (findOnPage == null) {
                findOnPage = WebViewFindDialogFactory.createInstance(this, toolbar.findOnPage)
            }

            if (enable) {
                findOnPage!!.show(tabManagerIn.currentTabData.mWebView)
            } else {
                findOnPage!!.hide()
            }
        }

    override var isEnableFlick: Boolean
        get() = AppData.flick_enable.get()
        set(value) {
            AppData.flick_enable.set(value)
            AppData.commit(applicationContext, AppData.flick_enable)
        }

    override var isEnableUserScript: Boolean
        get() = webClient.isEnableUserScript
        set(value) {
            webClient.isEnableUserScript = value
        }

    override var isEnableQuickControl: Boolean
        get() = userActionManager.isPieEnabled
        set(value) {
            userActionManager.setPieEnable(value, webFrameLayout)
        }

    override var isEnableMultiFingerGesture: Boolean
        get() = userActionManager.isEnableMultiFingerGesture
        set(value) {
            userActionManager.isEnableMultiFingerGesture = value
        }

    override var isEnableAdBlock: Boolean
        get() = webClient.isEnableAdBlock
        set(value) {
            webClient.isEnableAdBlock = value
        }

    override var isEnableGesture: Boolean
        get() = webGestureOverlayView.isEnabled
        set(enable) {
            webGestureOverlayView.isEnabled = enable
        }

    companion object {
        private const val TAG = "BrowserActivity"
        private const val TAB_TYPE = "tabType"
        private const val EXTRA_DATA_TARGET = "BrowserActivity.target"
        const val EXTRA_WINDOW_MODE = "window_mode"
        const val EXTRA_SHOULD_OPEN_IN_NEW_TAB = "shouldOpenInNewTab"
    }
}