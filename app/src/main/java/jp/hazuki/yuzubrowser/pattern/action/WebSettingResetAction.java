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

package jp.hazuki.yuzubrowser.pattern.action;

import android.webkit.WebSettings;

import jp.hazuki.yuzubrowser.tab.manager.MainTabData;

public class WebSettingResetAction {
    private String userAgent;
    private boolean javaScriptEnable;
    private boolean navLockEnable;
    private WebSettingPatternAction patternAction;

    public WebSettingResetAction(MainTabData tabData) {
        WebSettings settings = tabData.mWebView.getSettings();
        userAgent = settings.getUserAgentString();
        javaScriptEnable = settings.getJavaScriptEnabled();
        navLockEnable = tabData.isNavLock();
    }

    public void reset(MainTabData tab) {
        WebSettings settings = tab.mWebView.getSettings();

        settings.setUserAgentString(userAgent);
        settings.setJavaScriptEnabled(javaScriptEnable);
        tab.setNavLock(navLockEnable);

    }

    public WebSettingPatternAction getPatternAction() {
        return patternAction;
    }

    public void setPatternAction(WebSettingPatternAction patternAction) {
        this.patternAction = patternAction;
    }
}
