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

package jp.hazuki.yuzubrowser.webview.utility

import android.webkit.WebSettings

interface WebViewUtility {

    fun String.shouldLoadSameTabAuto() = regionMatches(0, "about:", 0, 6, true)

    fun String.shouldLoadSameTabScheme() = regionMatches(0, "intent:", 0, 7, true)
            || regionMatches(0, "yuzu:", 0, 5, true)
            && isSpeedDial()

    fun String.isSpeedDial() = equals("yuzu:speeddial", true)

    fun String.shouldLoadSameTabUser() = regionMatches(0, "javascript:", 0, 11, true)

    fun WebSettings.setDisplayZoomButtons(show: Boolean) {
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = show
    }

    fun WebSettings.copyDisplayZoomButtonsTo(to: WebSettings) {
        to.setSupportZoom(true)
        to.builtInZoomControls = true
        to.displayZoomControls = displayZoomControls
    }
}