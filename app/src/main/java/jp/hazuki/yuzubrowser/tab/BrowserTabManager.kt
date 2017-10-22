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

package jp.hazuki.yuzubrowser.tab

import android.view.View
import jp.hazuki.yuzubrowser.browser.BrowserController
import jp.hazuki.yuzubrowser.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.tab.manager.OnWebViewCreatedListener
import jp.hazuki.yuzubrowser.tab.manager.TabIndexData
import jp.hazuki.yuzubrowser.tab.manager.TabManager
import jp.hazuki.yuzubrowser.webkit.CustomWebView
import jp.hazuki.yuzubrowser.webkit.TabType

class BrowserTabManager(private val tabManager: TabManager, private val controller: BrowserController) : UiTabManager {

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

    override fun add(web: CustomWebView?, view: View?): MainTabData {
        return tabManager.add(web, view)
    }

    override fun addTab(index: Int, tabData: MainTabData) {
        val oldCurrent = tabManager.currentTabNo
        tabManager.addTab(index, tabData)
        toolbarManager.addTab(index, tabData.tabView)

        if (oldCurrent == tabManager.currentTabNo) {
            toolbarManager.moveCurrentTabPosition(tabManager.currentTabNo)
        }
    }

    override fun setCurrentTab(no: Int) {
        tabManager.setCurrentTab(no)
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

    override fun indexOf(id: Long): Int {
        return tabManager.indexOf(id)
    }

    override fun size(): Int {
        return tabManager.size()
    }

    override fun isEmpty(): Boolean {
        return tabManager.isEmpty
    }

    override fun isFirst(): Boolean {
        return tabManager.isFirst
    }

    override fun isLast(): Boolean {
        return tabManager.isLast
    }

    override fun isFirst(no: Int): Boolean {
        return tabManager.isFirst(no)
    }

    override fun isLast(no: Int): Boolean {
        return tabManager.isLast(no)
    }

    override fun getLastTabNo(): Int {
        return tabManager.lastTabNo
    }

    override fun getCurrentTabNo(): Int {
        return tabManager.currentTabNo
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

    override fun getCurrentTabData(): MainTabData? {
        return tabManager.currentTabData
    }

    override fun get(no: Int): MainTabData {
        return tabManager[no]
    }

    override fun get(web: CustomWebView?): MainTabData {
        return tabManager[web]
    }

    override fun getIndexData(no: Int): TabIndexData {
        return tabManager.getIndexData(no)
    }

    override fun getIndexData(id: Long): TabIndexData {
        return tabManager.getIndexData(id)
    }

    override fun searchParentTabNo(id: Long): Int {
        return tabManager.searchParentTabNo(id)
    }

    override fun destroy() {
        return tabManager.destroy()
    }

    override fun saveData() {
        tabManager.saveData()
    }

    override fun loadData() {
        tabManager.loadData()
    }

    override fun clear() {
        tabManager.clear()
    }

    override fun clearExceptPinnedTab() {
        tabManager.clearExceptPinnedTab()
    }

    override fun onPreferenceReset() {
        tabManager.onPreferenceReset()
    }

    override fun onLayoutCreated() {
        tabManager.onLayoutCreated()
    }

    override fun getLoadedData(): MutableList<MainTabData> {
        return tabManager.loadedData
    }

    override fun getIndexDataList(): MutableList<TabIndexData> {
        return tabManager.indexDataList
    }

    override fun takeThumbnailIfNeeded(data: MainTabData?) {
        tabManager.takeThumbnailIfNeeded(data)
    }

    override fun removeThumbnailCache(url: String?) {
        tabManager.removeThumbnailCache(url)
    }

    override fun forceTakeThumbnail(data: MainTabData?) {
        tabManager.forceTakeThumbnail(data)
    }

    override fun setOnWebViewCreatedListener(listener: OnWebViewCreatedListener?) {
        tabManager.setOnWebViewCreatedListener(listener)
    }
}