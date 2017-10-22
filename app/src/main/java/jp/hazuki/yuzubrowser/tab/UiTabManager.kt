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

import jp.hazuki.yuzubrowser.tab.manager.MainTabData
import jp.hazuki.yuzubrowser.tab.manager.TabManager
import jp.hazuki.yuzubrowser.webkit.CustomWebView
import jp.hazuki.yuzubrowser.webkit.TabType

interface UiTabManager : TabManager {
    fun add(web: CustomWebView, @TabType type: Int): MainTabData
    fun setCurrentTab(no: Int, from: MainTabData?, to: MainTabData)
}