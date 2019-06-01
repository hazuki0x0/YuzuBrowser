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

package jp.hazuki.yuzubrowser.browser.manager

import android.content.Context
import android.content.Intent
import android.gesture.Gesture
import android.gesture.GestureOverlayView
import android.net.Uri
import android.provider.ContactsContract
import android.view.ContextMenu
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebView
import jp.hazuki.yuzubrowser.browser.view.GestureFrameLayout
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.utils.MathUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.manager.*
import jp.hazuki.yuzubrowser.legacy.browser.BrowserController
import jp.hazuki.yuzubrowser.legacy.browser.ui.PieManager
import jp.hazuki.yuzubrowser.legacy.gesture.GestureManager
import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.data.MultiFingerGestureManager
import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.detector.MultiFingerGestureDetector
import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.detector.MultiFingerGestureInfo
import jp.hazuki.yuzubrowser.legacy.utils.extensions.setClipboardWithToast
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import jp.hazuki.yuzubrowser.ui.theme.ThemeData
import jp.hazuki.yuzubrowser.ui.widget.MultiTouchGestureDetector
import jp.hazuki.yuzubrowser.webview.CustomOnCreateContextMenuListener
import jp.hazuki.yuzubrowser.webview.CustomWebView
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class UserActionManager(private val context: Context, private val browser: BrowserController, private val controller: ActionController, iconManager: ActionIconManager) : MultiFingerGestureDetector.OnMultiFingerGestureListener, GestureOverlayView.OnGestureListener, GestureOverlayView.OnGesturePerformedListener {

    val onCreateContextMenuListener: CustomOnCreateContextMenuListener = MyOnCreateContextMenuListener()

    private val pieManager = PieManager(context, controller, iconManager)
    private val hardButton = HardButtonActionManager.getInstance(context.applicationContext)
    private val gestureDetector = MultiTouchGestureDetector(context, MyGestureListener())

    private var mWebGestureManager: GestureManager? = null
    private var multiFingerGestureManager: MultiFingerGestureManager? = null
    private var multiFingerGestureDetector: MultiFingerGestureDetector? = null

    var isPieEnabled = false
        private set
    private var isShowActionName = false

    fun setPieEnable(isEnable: Boolean, root: ViewGroup) {
        if (isEnable != isPieEnabled) {
            isPieEnabled = isEnable
            if (isEnable) {
                pieManager.attachToLayout(root)
                pieManager.onPreferenceReset()
                pieManager.onThemeChanged(ThemeData.getInstance())
            } else {
                pieManager.detachFromLayout(root)
            }
        }
    }

    var isEnableMultiFingerGesture: Boolean
        get() = multiFingerGestureDetector != null
        set(enable) {
            if (enable) {
                multiFingerGestureManager = MultiFingerGestureManager(context)
                if (multiFingerGestureDetector == null)
                    multiFingerGestureDetector = MultiFingerGestureDetector(context, this)
                multiFingerGestureDetector!!.setShowName(AppPrefs.multi_finger_gesture_show_name.get())
                multiFingerGestureDetector!!.setSensitivity(AppPrefs.multi_finger_gesture_sensitivity.get())
            } else {
                if (multiFingerGestureManager == null)
                    multiFingerGestureManager = null

                if (multiFingerGestureDetector == null)
                    multiFingerGestureDetector = null
            }
        }

    fun setEnableGesture(gestureLayout: GestureFrameLayout) {
        if (AppPrefs.gesture_enable_web.get()) {
            mWebGestureManager = GestureManager.getInstance(context.applicationContext, GestureManager.GESTURE_TYPE_WEB).apply { load() }

            gestureLayout.isEnabled = true
            gestureLayout.setGestureVisible(AppPrefs.gesture_line_web.get())

            gestureLayout.removeAllOnGestureListeners()
            gestureLayout.removeAllOnGesturePerformedListeners()

            gestureLayout.addOnGestureListener(this)
            gestureLayout.addOnGesturePerformedListener(this)
        } else {
            if (mWebGestureManager != null)
                mWebGestureManager = null

            gestureLayout.removeAllOnGestureListeners()
            gestureLayout.removeAllOnGesturePerformedListeners()
            gestureLayout.isEnabled = false
        }
    }

    fun onPreferenceReset() {
        pieManager.onPreferenceReset()
    }

    fun onThemeChanged(themeData: ThemeData?) {
        pieManager.onThemeChanged(themeData)
    }

    fun setGestureDetector(web: CustomWebView) {
        web.setWebViewTouchDetector(gestureDetector)
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return if (pieManager.isOpen) {
            multiFingerGestureDetector?.run {
                if (isTracking) {
                    stopTracking()
                }
            }
            false
        } else {
            multiFingerGestureDetector?.onTouchEvent(event) == true
        }
    }

    fun onVolumeKey(isUp: Boolean) =
            controller.run(if (isUp) hardButton.volume_up.action else hardButton.volume_down.action)

    fun isVolumeActionNotEmpty(isUp: Boolean) =
            (if (isUp) hardButton.volume_up.action else hardButton.volume_down.action).isNotEmpty()

    fun onCameraKey() = controller.run(hardButton.camera_press.action)

    fun onBackKey() {
        controller.run(hardButton.back_press.action)
    }

    fun onBackKeyLong() {
        controller.run(hardButton.back_lpress.action)
    }

    fun onSearchKey() {
        controller.run(hardButton.search_press.action)
    }

    /** Gesture */
    override fun onGestureStarted(overlay: GestureOverlayView, event: MotionEvent) {
        if (browser.toolbarManager.isContainsWebToolbar(event))
            overlay.cancelGesture()
    }

    override fun onGestureCancelled(overlay: GestureOverlayView?, event: MotionEvent?) {}

    override fun onGestureEnded(overlay: GestureOverlayView?, event: MotionEvent?) {}

    override fun onGesture(overlay: GestureOverlayView, event: MotionEvent) {
        if (event.pointerCount > 1)
            overlay.cancelGesture()//multiple touch is disabled
    }

    override fun onGesturePerformed(overlay: GestureOverlayView?, gesture: Gesture?) {
        mWebGestureManager!!.recognize(gesture)?.let {
            controller.run(it)
        }
    }

    override fun onGesturePerformed(info: MultiFingerGestureInfo): Boolean {
        if (!pieManager.isOpen) {
            multiFingerGestureManager?.run {
                gestureItems.forEach {
                    if (info.match(it)) {
                        controller.run(it.action)
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onShowGestureName(info: MultiFingerGestureInfo) {
        multiFingerGestureManager?.run {
            gestureItems.forEach {
                if (info.match(it)) {
                    isShowActionName = true
                    browser.showActionName(it.action.toString(browser.actionNameArray))
                    return
                }
            }
        }

        if (isShowActionName) {
            isShowActionName = false
            browser.hideActionName()
        }
    }

    override fun onDismissGestureName() {
        if (isShowActionName) {
            isShowActionName = false
            browser.hideActionName()
        }
    }

    private inner class MyOnCreateContextMenuListener : CustomOnCreateContextMenuListener() {
        override fun onCreateContextMenu(menu: ContextMenu, webView: CustomWebView, menuInfo: ContextMenu.ContextMenuInfo?) {
            val result = webView.hitTestResult ?: return

            val manager = LongPressActionManager.getInstance(browser.applicationContextInfo)

            when (result.type) {
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> controller.run(manager.link.action, ActionController.HitTestResultTargetInfo(webView, result))
                WebView.HitTestResult.IMAGE_TYPE -> controller.run(manager.image.action, ActionController.HitTestResultTargetInfo(webView, result))
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> controller.run(manager.image_link.action, ActionController.HitTestResultTargetInfo(webView, result))
                WebView.HitTestResult.PHONE_TYPE -> {
                    val extra = result.extra
                    menu.setHeaderTitle(Uri.decode(extra))
                    menu.add(R.string.dial).setOnMenuItemClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_TEL + extra))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        browser.startActivity(intent)
                        false
                    }
                    menu.add(R.string.add_contact).setOnMenuItemClickListener {
                        val intent = Intent(Intent.ACTION_INSERT_OR_EDIT)
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Uri.decode(extra))
                        intent.type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        browser.startActivity(intent)
                        false
                    }
                    menu.add(R.string.copy_phone_num).setOnMenuItemClickListener {
                        browser.applicationContextInfo.setClipboardWithToast(Uri.decode(extra))
                        false
                    }
                }

                WebView.HitTestResult.EMAIL_TYPE -> {
                    val extra = result.extra
                    menu.setHeaderTitle(extra)
                    menu.add(R.string.email).setOnMenuItemClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_MAILTO + extra))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        browser.startActivity(intent)
                        false
                    }
                    menu.add(R.string.add_contact).setOnMenuItemClickListener {
                        val intent = Intent(Intent.ACTION_INSERT_OR_EDIT)
                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, extra)
                        intent.type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        browser.startActivity(intent)
                        false
                    }
                    menu.add(R.string.copy_email_address).setOnMenuItemClickListener {
                        browser.applicationContextInfo.setClipboardWithToast(extra)
                        false
                    }
                }

                WebView.HitTestResult.GEO_TYPE -> {
                    val extra = result.extra
                    menu.setHeaderTitle(extra)
                    menu.add(R.string.open_map).setOnMenuItemClickListener {
                        try {
                            browser.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_GEO + URLEncoder.encode(extra, "UTF-8"))))
                        } catch (e: UnsupportedEncodingException) {
                            ErrorReport.printAndWriteLog(e)
                        }

                        false
                    }
                    menu.add(R.string.copy_map_address).setOnMenuItemClickListener {
                        browser.applicationContextInfo.setClipboardWithToast(extra)
                        false
                    }
                }

                WebView.HitTestResult.UNKNOWN_TYPE -> controller.run(manager.others.action)
            }
        }
    }

    private inner class MyGestureListener : MultiTouchGestureDetector.OnMultiTouchGestureListener, MultiTouchGestureDetector.OnMultiTouchDoubleTapListener {
        override fun onSingleTapUp(e: MotionEvent) = false

        override fun onShowPress(e: MotionEvent) {}

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            if (e1 == null || e2 == null)
                return false

            val tab = browser.currentTabData
            if (tab != null) {
                browser.toolbarManager.onWebViewScroll(tab.mWebView, e1, e2, distanceX, distanceY)
            }
            return false
        }

        override fun onLongPress(e: MotionEvent) {}

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null || e2 == null)
                return false

            val tab = browser.currentTabData ?: return false

            if (e1.pointerCount <= 1 || e2.pointerCount <= 1) {
                if (!AppPrefs.flick_enable.get())
                    return false

                if (AppPrefs.flick_disable_scroll.get() && tab.isMoved)
                    return false

                val dx = Math.abs(velocityX)
                val dy = Math.abs(velocityY)
                val dist = e2.x - e1.x

                if (dy > dx)
                    return false
                if (dx < AppPrefs.flick_sensitivity_speed.get() * 100)
                    return false
                if (Math.abs(dist) < AppPrefs.flick_sensitivity_distance.get() * 10)
                    return false
                if (e2.eventTime - e1.eventTime > 300L)
                    return false

                if (AppPrefs.flick_edge.get()) {
                    val x = e1.x
                    val slop = browser.resourcesByInfo.getDimension(R.dimen.flick_slop).toInt()

                    if (x <= tab.mWebView.view.width - slop && x >= slop) {
                        return false
                    }
                }

                val manager = FlickActionManager.getInstance(browser.applicationContextInfo)

                if (dist < 0)
                    controller.run(manager.flick_left.action)
                else
                    controller.run(manager.flick_right.action)
            } else {
                if (!AppPrefs.webswipe_enable.get())
                    return false

                val manager = WebSwipeActionManager.getInstance(browser.applicationContextInfo)

                val distX0 = e2.getX(0) - e1.getX(0)
                val distX1 = e2.getX(1) - e1.getX(1)
                val distY0 = e2.getY(0) - e1.getY(0)
                val distY1 = e2.getY(1) - e1.getY(1)

                val senseSpeed = AppPrefs.webswipe_sensitivity_speed.get() * 100
                val senseDist = AppPrefs.webswipe_sensitivity_distance.get() * 10

                if (checkWebSwipe(senseSpeed, senseDist, velocityX, distX0, distX1)) {
                    if (checkWebSwipe(senseSpeed, senseDist, velocityY, distY0, distY1))
                        return false
                    if (distX0 < 0)
                        controller.run(manager.double_left.action)
                    else
                        controller.run(manager.double_right.action)
                    return true
                }

                if (checkWebSwipe(senseSpeed, senseDist, velocityY, distY0, distY1)) {
                    if (checkWebSwipe(senseSpeed, senseDist, velocityX, distX0, distX1))
                        return false
                    if (distY0 < 0)
                        controller.run(manager.double_up.action)
                    else
                        controller.run(manager.double_down.action)
                    return true
                }
            }

            return false
        }

        private fun checkWebSwipe(sense_speed: Int, sense_dist: Int, velocity: Float, dist0: Float, dist1: Float) =
                Math.abs(velocity) >= sense_speed && MathUtils.equalsSign(dist0, dist1) && Math.abs(dist0) >= sense_dist && Math.abs(dist1) >= sense_dist

        override fun onUp(e: MotionEvent) {
            browser.toolbarManager.onWebViewTapUp()
        }

        override fun onDown(e: MotionEvent): Boolean {
            browser.stopAutoScroll()

            val tabData = browser.currentTabData ?: return false

            tabData.onDown()

            browser.tabManager.takeThumbnailIfNeeded(tabData)
            return false
        }

        override fun onPointerDown(e: MotionEvent) = false

        override fun onPointerUp(e: MotionEvent) = false

        override fun onSingleTapConfirmed(e: MotionEvent) = false

        override fun onDoubleTap(e: MotionEvent) = false

        override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
            if (e == null)
                return false
            val tab = browser.currentTabData ?: return false

            tab.mWebView.setDoubleTapFling(e.pointerCount == 1)
            return false
        }

        override fun onDoubleTapScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) = false

        override fun onDoubleTapFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (!AppPrefs.double_tap_flick_enable.get())
                return false

            if (e1 == null || e2 == null)
                return false

            if (e2.eventTime - e1.eventTime > 300L)
                return false

            if (e1.pointerCount <= 1 || e2.pointerCount <= 1) {

                val dx = Math.abs(velocityX)
                val dy = Math.abs(velocityY)

                val distX = e2.x - e1.x
                val distY = e2.y - e1.y

                val manager = DoubleTapFlickActionManager.getInstance(browser.applicationContextInfo)

                val returnValue: Boolean
                if (dy > dx) {
                    if (dy < AppPrefs.double_tap_flick_sensitivity_speed.get() * 100)
                        return false
                    if (Math.abs(distY) < AppPrefs.double_tap_flick_sensitivity_distance.get() * 10)
                        return false

                    returnValue = if (distY < 0)
                        controller.run(manager.flick_up.action)
                    else
                        controller.run(manager.flick_down.action)
                } else {
                    if (dx < AppPrefs.double_tap_flick_sensitivity_speed.get() * 100)
                        return false
                    if (Math.abs(distX) < AppPrefs.double_tap_flick_sensitivity_distance.get() * 10)
                        return false

                    returnValue = if (distX < 0)
                        controller.run(manager.flick_left.action)
                    else
                        controller.run(manager.flick_right.action)
                }
                return returnValue

            }
            return false
        }
    }
}
