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

package jp.hazuki.yuzubrowser.legacy.toolbar

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.support.design.widget.AppBarLayout
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import jp.hazuki.utility.extensions.getResColor
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionController
import jp.hazuki.yuzubrowser.legacy.action.manager.ActionIconManager
import jp.hazuki.yuzubrowser.legacy.settings.container.ToolbarVisibilityContainer
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.theme.ThemeData
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_BOTTOM
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_BOTTOM_ALWAYS
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_BOTTOM_OVERLAY_ALWAYS
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_FIXED_WEB
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_FLOAT_BOTTOM
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_LEFT
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_RIGHT
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_TOP
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_TOP_ALWAYS
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_UNDEFINED
import jp.hazuki.yuzubrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_WEB
import jp.hazuki.yuzubrowser.legacy.toolbar.main.*
import jp.hazuki.yuzubrowser.legacy.utils.view.tab.TabLayout
import jp.hazuki.yuzubrowser.legacy.webkit.CustomWebView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.browser_activity.*

open class BrowserToolbarManager(context: Context, override val containerView: View, controller: ActionController, iconManager: ActionIconManager, requestCallback: RequestCallback) : ToolbarManager, LayoutContainer, OnWebViewScrollChangeListener {
    override val tabBar = TabBar(context, controller, iconManager, requestCallback)
    override val urlBar = if (AppData.toolbar_url_box.get()) WhiteUrlBar(context, controller, iconManager, requestCallback) else UrlBar(context, controller, iconManager, requestCallback)
    override val progressBar = ProgressToolBar(context, requestCallback)
    override val customBar = CustomToolbar(context, controller, iconManager, requestCallback)
    private var mIsWebToolbarCombined = false

    override val findOnPage: View
        get() = find
    override val bottomToolbarAlwaysLayout: LinearLayout
        get() = bottomAlwaysToolbarLayout


    private val webToolbarLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }
    private val fixedWebToolbarLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(0xff000000.toInt())
    }

    companion object {
        private val TOOLBAR_LAYOUT_PARAMS = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    }

    init {
        bottomAlwaysToolbarLayout.background =
                ColorDrawable(context.getResColor(R.color.deep_gray))

        overlayToolbarScrollPadding.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            overlayToolbarScrollPadding.height = bottom - top
        }
    }

    override fun addToolbarView(isPortrait: Boolean) {
        for (i in 1..4) {
            addSingleToolbarView(i, tabBar, isPortrait)
            addSingleToolbarView(i, urlBar, isPortrait)
            addSingleToolbarView(i, progressBar, isPortrait)
            addSingleToolbarView(i, customBar, isPortrait)
        }
    }

    private fun addSingleToolbarView(priority: Int, toolbar: ToolbarBase, isPortrait: Boolean) {
        val toolbarPreference = toolbar.toolbarPreferences
        var toolbarPriority: Int
        if (isPortrait) {
            toolbarPriority = toolbarPreference.location_priority.get()
        } else {
            toolbarPriority = toolbarPreference.location_landscape_priority.get()
            if (toolbarPriority < 0)
                toolbarPriority = toolbarPreference.location_priority.get()
        }
        if (toolbarPriority != priority)
            return

        var location: Int
        if (isPortrait) {
            location = toolbarPreference.location.get()
        } else {
            location = toolbarPreference.location_landscape.get()
            if (location == LOCATION_UNDEFINED)
                location = toolbarPreference.location.get()
        }

        when (location) {
            LOCATION_TOP -> topToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_BOTTOM, LOCATION_FLOAT_BOTTOM -> bottomToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_WEB -> webToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_FIXED_WEB -> fixedWebToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_LEFT -> {
                (toolbar.findViewById<View>(R.id.linearLayout) as LinearLayout).orientation = LinearLayout.VERTICAL
                leftToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            }
            LOCATION_RIGHT -> {
                (toolbar.findViewById<View>(R.id.linearLayout) as LinearLayout).orientation = LinearLayout.VERTICAL
                rightToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            }
            LOCATION_TOP_ALWAYS -> topAlwaysToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_BOTTOM_ALWAYS -> bottomToolbarAlwaysLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_BOTTOM_OVERLAY_ALWAYS -> bottomOverlayItemLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            else -> throw IllegalStateException("Unknown location:" + toolbar.toolbarPreferences.location.get())
        }
    }

    override fun changeCurrentTab(to_id: Int, from: MainTabData?, to: MainTabData?) {
        tabBar.changeCurrentTab(to_id)

        from?.mWebView?.setEmbeddedTitleBarMethod(null)
        if (to == null) return
        setWebViewTitleBar(to.mWebView, !to.isInPageLoad)

        notifyChangeWebState(to)
    }

    override fun moveCurrentTabPosition(id: Int) = tabBar.changeCurrentTab(id)

    override fun swapTab(a: Int, b: Int) = tabBar.swapTab(a, b)

    override fun moveTab(from: Int, to: Int, newCurrent: Int) = tabBar.moveTab(from, to, newCurrent)

    override fun onPreferenceReset() {
        tabBar.onPreferenceReset()
        urlBar.onPreferenceReset()
        progressBar.onPreferenceReset()
        customBar.onPreferenceReset()

        val params = topToolbarLayout.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = if (AppData.snap_toolbar.get()) {
            (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                    or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP)
        } else {
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        }
        bottomToolbarLayout.background.alpha = AppData.overlay_bottom_alpha.get()
        bottomOverlayItemLayout.background.alpha = AppData.overlay_bottom_alpha.get()
    }

    override fun onThemeChanged(themeData: ThemeData?) {
        tabBar.onThemeChanged(themeData)
        urlBar.onThemeChanged(themeData)
        progressBar.onThemeChanged(themeData)
        customBar.onThemeChanged(themeData)

        if (themeData != null && themeData.toolbarBackgroundColor != 0) {
            topToolbarLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
            bottomToolbarLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
            bottomOverlayItemLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
            webToolbarLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
            fixedWebToolbarLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
            leftToolbarLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
            rightToolbarLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
            topAlwaysToolbarLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
            bottomToolbarAlwaysLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
        } else {
            topToolbarLayout.setBackgroundResource(R.color.deep_gray)
            bottomToolbarLayout.setBackgroundResource(R.color.deep_gray)
            bottomOverlayItemLayout.setBackgroundResource(R.color.deep_gray)
            webToolbarLayout.setBackgroundResource(R.color.deep_gray)
            fixedWebToolbarLayout.setBackgroundResource(R.color.deep_gray)
            leftToolbarLayout.setBackgroundResource(R.color.deep_gray)
            rightToolbarLayout.setBackgroundResource(R.color.deep_gray)
            topAlwaysToolbarLayout.setBackgroundResource(R.color.deep_gray)
            bottomToolbarAlwaysLayout.setBackgroundResource(R.color.deep_gray)
        }

        bottomToolbarLayout.background.alpha = AppData.overlay_bottom_alpha.get()
        bottomOverlayItemLayout.background.alpha = AppData.overlay_bottom_alpha.get()

        for (i in 0 until bottomToolbarAlwaysLayout.childCount) {
            (bottomToolbarLayout.getChildAt(i) as? SubToolbar)?.run {
                applyTheme(themeData)
            }
        }
    }

    override fun onActivityConfigurationChanged(config: Configuration) {
        onActivityConfigurationChangedSingle(tabBar, config)
        onActivityConfigurationChangedSingle(urlBar, config)
        onActivityConfigurationChangedSingle(progressBar, config)
        onActivityConfigurationChangedSingle(customBar, config)

        addToolbarView(config.orientation == Configuration.ORIENTATION_PORTRAIT)
    }

    private fun onActivityConfigurationChangedSingle(toolbar: ToolbarBase, config: Configuration) {
        val parent = toolbar.parent as? ViewGroup ?: throw NullPointerException()
        if (parent === leftToolbarLayout || parent === rightToolbarLayout)
            (toolbar.findViewById<View>(R.id.linearLayout) as LinearLayout).orientation = LinearLayout.HORIZONTAL
        parent.removeView(toolbar)
        toolbar.onActivityConfigurationChanged(config)
    }

    override fun onFullscreenChanged(isFullscreen: Boolean) {
        tabBar.onFullscreenChanged(isFullscreen)
        urlBar.onFullscreenChanged(isFullscreen)
        progressBar.onFullscreenChanged(isFullscreen)
        customBar.onFullscreenChanged(isFullscreen)
    }

    override fun onImeChanged(isShown: Boolean) {
        bottomOverlayLayout.visibility = if (isShown) View.GONE else View.VISIBLE
    }

    override fun notifyChangeProgress(data: MainTabData) = progressBar.changeProgress(data)

    override fun notifyChangeWebState(data: MainTabData?) {
        if (data != null)
            setWebViewTitleBar(data.mWebView, !data.isInPageLoad)

        tabBar.notifyChangeWebState(data)
        urlBar.notifyChangeWebState(data)
        progressBar.notifyChangeWebState(data)
        customBar.notifyChangeWebState(data)
    }

    override fun resetToolBar() {
        tabBar.resetToolBar()
        urlBar.resetToolBar()
        customBar.resetToolBar()
    }

    override fun addNewTabView() = tabBar.addNewTabView()

    override fun scrollTabRight() = tabBar.fullScrollRight()

    override fun scrollTabLeft() = tabBar.fullScrollLeft()

    override fun scrollTabTo(position: Int) = tabBar.scrollToPosition(position)

    override fun removeTab(no: Int) = tabBar.removeTab(no)

    override fun addTab(id: Int, view: View) {
        tabBar.addTab(id, view)
    }

    override fun setWebViewTitleBar(web: CustomWebView, combine: Boolean) {
        if (fixedWebToolbarLayout.parent is ViewGroup) {
            (fixedWebToolbarLayout.parent as ViewGroup).removeView(fixedWebToolbarLayout)
        }
        if (combine) {
            if (!mIsWebToolbarCombined) {
                topToolbarLayout.removeView(webToolbarLayout)
                fixedWebToolbarLayout.addView(webToolbarLayout, 0)
            }
            web.setEmbeddedTitleBarMethod(fixedWebToolbarLayout)
        } else {
            if (mIsWebToolbarCombined) {
                fixedWebToolbarLayout.removeView(webToolbarLayout)
                topToolbarLayout.addView(webToolbarLayout)
            }
            web.setEmbeddedTitleBarMethod(fixedWebToolbarLayout)
        }
        mIsWebToolbarCombined = combine
    }

    override fun setOnTabClickListener(l: TabLayout.OnTabClickListener) {
        tabBar.setOnTabClickListener(l)
    }

    override fun showGeolocationPermissionPrompt(view: View) {
        bottomOverlayItemLayout.addView(view, 0)
    }

    override fun hideGeolocationPermissionPrompt(view: View) {
        bottomOverlayItemLayout.removeView(view)
    }

    override fun isContainsWebToolbar(ev: MotionEvent): Boolean {
        val rc = Rect()
        fixedWebToolbarLayout.getGlobalVisibleRect(rc)
        return rc.contains(ev.rawX.toInt(), ev.rawY.toInt())
    }

    override fun onWebViewScroll(web: CustomWebView, e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) {
        if (topToolbarLayout.height == 0 && web.isScrollable) {
            val translationY = bottomOverlayLayout.translationY
            val newTrans: Float

            newTrans = when {
                distanceY < 0 -> Math.max(0f, distanceY + translationY) //down
                distanceY > 0 -> Math.min(bottomToolbarLayout.height.toFloat(), distanceY + translationY) //up
                else -> return
            }

            bottomOverlayLayout.translationY = newTrans
        }
    }

    override fun onScrollChanged(webView: CustomWebView, x: Int, y: Int) {
        val height = overlayToolbarScrollPadding.realHeight
        if (height > 0) {
            val target = webView.computeVerticalScrollRangeMethod() - webView.computeVerticalScrollExtentMethod()
            if (y >= target - height) {
                if (!overlayToolbarScrollPadding.visible) {
                    overlayToolbarScrollPadding.visible = true
                }
            } else if (y >= target - height * 2 && overlayToolbarScrollPadding.visible) {
                overlayToolbarScrollPadding.visible = false
            }
        }
    }

    override fun onWebViewTapUp() {
        if (topToolbarLayout.height == 0 && AppData.snap_toolbar.get()) {
            val trans = bottomOverlayLayout.translationY
            val animator: ObjectAnimator
            var duration: Int
            val bottomBarHeight = bottomToolbarLayout.height
            if (trans > bottomBarHeight / 2) {
                animator = ObjectAnimator.ofFloat(bottomOverlayLayout, "translationY", trans, bottomBarHeight.toFloat())
                duration = (((bottomBarHeight - trans) / bottomBarHeight + 1) * 150).toInt()
            } else {
                animator = ObjectAnimator.ofFloat(bottomOverlayLayout, "translationY", trans, 0f)
                duration = ((trans / bottomBarHeight + 1) * 150).toInt()
            }
            if (duration < 0) {
                duration = 0
            }
            animator.duration = duration.toLong()
            animator.start()
        }
    }

    interface RequestCallback {
        fun shouldShowToolbar(visibility: ToolbarVisibilityContainer, tabData: MainTabData?, config: Configuration? = null): Boolean
    }
}