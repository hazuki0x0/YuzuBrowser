/*
 * Copyright (C) 2017-2018 Hazuki
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

import android.content.Intent
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import jp.hazuki.yuzubrowser.action.ActionNameArray
import jp.hazuki.yuzubrowser.action.item.AutoPageScrollAction
import jp.hazuki.yuzubrowser.action.item.OpenOptionsMenuAction
import jp.hazuki.yuzubrowser.action.item.TabListSingleAction
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.tab.manager.TabManager
import jp.hazuki.yuzubrowser.toolbar.ToolbarManager
import jp.hazuki.yuzubrowser.webkit.CustomWebView
import jp.hazuki.yuzubrowser.webkit.TabType
import jp.hazuki.yuzubrowser.webrtc.core.WebRtcRequest

interface BrowserController : BrowserInfo {
    fun getTab(target: Int): MainTabData = getTabOrNull(target) ?: throw IndexOutOfBoundsException("$target not found")
    fun getTabOrNull(target: Int): MainTabData?
    fun getTabOrNull(target: CustomWebView): MainTabData?
    fun indexOf(id: Long): Int
    fun dispatchKeyEvent(event: KeyEvent): Boolean
    fun notifyChangeWebState(tab: MainTabData? = currentTabData) = toolbarManager.notifyChangeWebState(tab)
    fun notifyChangeProgress(tab: MainTabData) = toolbarManager.notifyChangeProgress(tab)
    fun setCurrentTab(target: Int)
    fun removeTab(target: Int, error: Boolean = true, destroy: Boolean = true): Boolean
    fun swapTab(i: Int, j: Int)
    fun loadUrl(url: String, target: Int)
    fun loadUrl(tab: MainTabData = currentTabData!!, url: String, shouldOpenInNewTab: Boolean = false)
    fun loadUrl(tab: MainTabData = currentTabData!!, url: String, target: Int, @TabType type: Int = TabType.WINDOW)
    fun showTabList(action: TabListSingleAction)
    fun showTabHistory(target: Int)
    fun restoreTab()
    fun showSearchBox(query: String, target: Int, openNewTab: Boolean, reverse: Boolean)
    fun showSubGesture()
    fun showMenu(button: View?, action: OpenOptionsMenuAction)
    fun finishAlert(clearTabNo: Int)
    fun finishQuick(clearTabNo: Int, finish_clear: Int = AppData.finish_alert_default.get())
    fun moveTaskToBack(root: Boolean): Boolean
    fun openInNewTab(tab: MainTabData)
    fun openInNewTab(url: String, @TabType type: Int, shouldOpenInNewTab: Boolean = false)
    fun openInBackground(url: String, @TabType type: Int)
    fun openInRightNewTab(url: String, @TabType type: Int)
    fun openInRightBgTab(url: String, @TabType type: Int)
    fun checkNewTabLink(perform: Int, transport: WebView.WebViewTransport): Boolean
    fun performNewTabLink(perform: Int, tab: MainTabData, url: String, @TabType type: Int): Boolean
    fun addBookmark(tab: MainTabData)
    fun savePage(tab: MainTabData)
    fun startActivity(intent: Intent)
    fun startActivity(intent: Intent, @RequestCause cause: Long)
    fun showCustomView(view: View, callback: WebChromeClient.CustomViewCallback)
    fun hideCustomView()
    fun getVideoLoadingProgressView(): View?
    fun showActionName(text: String?)
    fun hideActionName()
    fun requestIconChange() = notifyChangeWebState()
    fun requestAdjustWebView()
    fun expandToolbar()
    fun adjustBrowserPadding(tab: MainTabData)

    val tabManager: TabManager
    val superFrameLayoutInfo: CoordinatorLayout
    val activity: AppCompatActivity
    val toolbarManager: ToolbarManager
    val appBarLayout: AppBarLayout
    val currentTabNo: Int
        get() = tabManager.currentTabNo
    override val tabSize: Int
        get() = tabManager.size()
    override val currentTabData: MainTabData?
        get() = tabManager.currentTabData
    val pagePaddingHeight: Int
    val actionNameArray: ActionNameArray
    val webRtcRequest: WebRtcRequest

    var requestedOrientationByCtrl: Int
    var renderingMode: Int
    var isFullscreenMode: Boolean
    override var isPrivateMode: Boolean
    val isEnableFastPageScroller: Boolean
    fun showFastPageScroller(target: Int)
    fun closeFastPageScroller()
    val isEnableAutoScroll: Boolean
    fun startAutoScroll(target: Int, action: AutoPageScrollAction)
    fun stopAutoScroll()
    val isEnableMousePointer: Boolean
    fun showMousePointer(isBackFinish: Boolean)
    fun closeMousePointer()
    var isEnableFindOnPage: Boolean
    var isEnableFlick: Boolean
    override var isEnableUserScript: Boolean
    override var isEnableQuickControl: Boolean
    override var isEnableMultiFingerGesture: Boolean
    override var isEnableAdBlock: Boolean
    override var isEnableGesture: Boolean

    companion object {
        const val REQUEST_WEB_UPLOAD = 1L
        const val REQUEST_SEARCHBOX = 2L
        const val REQUEST_BOOKMARK = 3L
        const val REQUEST_HISTORY = 4L
        const val REQUEST_SETTING = 5L
        const val REQUEST_USERAGENT = 6L
        const val REQUEST_DEFAULT_USERAGENT = 7L
        const val REQUEST_USERJS_SETTING = 8L
        const val REQUEST_WEB_ENCODE_SETTING = 9L
        const val REQUEST_SHARE_IMAGE = 10L
        const val REQUEST_ACTION_LIST = 11L
    }
}