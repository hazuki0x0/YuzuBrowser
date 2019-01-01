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

package jp.hazuki.yuzubrowser.legacy.tab

import jp.hazuki.yuzubrowser.legacy.browser.BrowserController
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.legacy.tab.manager.TabManager
import jp.hazuki.yuzubrowser.legacy.webkit.TabType
import jp.hazuki.yuzubrowser.webview.CustomWebView

class BrowserTabManager(private val tabManager: TabManager, private val controller: BrowserController) : UiTabManager, TabManager by tabManager {

    private val toolbarManager
        get() = controller.toolbarManager

    override fun add(web: CustomWebView, @TabType type: Int): MainTabData {
        val tab = add(web, toolbarManager.addNewTabView())
        tab.tabType = type
        if (type == TabType.WINDOW) {
            tab.parent = tabManager.getIndexData(tabManager.currentTabNo).id
        }
        return tab
    }

    override fun addTab(index: Int, tabData: MainTabData) {
        val oldCurrent = tabManager.currentTabNo
        tabManager.addTab(index, tabData)
        toolbarManager.addTab(index, tabData.tabView)

        if (oldCurrent == tabManager.currentTabNo) {
            toolbarManager.moveCurrentTabPosition(tabManager.currentTabNo)
        }
    }

    override fun setCurrentTab(no: Int, from: MainTabData?, to: MainTabData) {
        tabManager.setCurrentTab(no)
        toolbarManager.changeCurrentTab(no, from, to)
    }

    override fun remove(no: Int) {
        tabManager.remove(no)
        toolbarManager.removeTab(no)
    }

    override fun move(from: Int, to: Int): Int {
        val current = tabManager.move(from, to)
        toolbarManager.moveTab(from, to, current)
        if (current == from) {
            toolbarManager.scrollTabTo(to)
        }
        return current
    }

    override fun swap(a: Int, b: Int) {
        if (a == b) return

        val oldCurrentTab = tabManager.currentTabNo
        val newCurrentTab = if (a == oldCurrentTab) b else if (b == oldCurrentTab) a else -1

        if (newCurrentTab >= 0) {
            tabManager.setCurrentTab(newCurrentTab)
        }
        tabManager.swap(a, b)
        toolbarManager.swapTab(a, b)
    }
}