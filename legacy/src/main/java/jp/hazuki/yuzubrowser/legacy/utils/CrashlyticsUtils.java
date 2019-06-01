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

package jp.hazuki.yuzubrowser.legacy.utils;

import android.content.Context;
import android.webkit.WebSettings;

import com.crashlytics.android.Crashlytics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.hazuki.yuzubrowser.ui.settings.AppPrefs;

public final class CrashlyticsUtils {
    private static Pattern VERSION_REGEX = Pattern.compile("Chrome/([.0-9]+)");
    private static final String CHROME_VERSION = "chrome version";
    private static final String WEB_VIEW_MODE = "WebView mode";

    public static void setChromeVersion(Context context) {
        Crashlytics.setString(CHROME_VERSION, getChromeVersion(context));
    }

    public static void setWebViewMode() {
        if (AppPrefs.fast_back.get()) {
            int size = AppPrefs.fast_back_cache_size.get();
            if (size == 0) {
                Crashlytics.setString(WEB_VIEW_MODE, "Infinite cache");
                return;
            } else if (size > 1) {
                Crashlytics.setString(WEB_VIEW_MODE, "Limit cache");
                return;
            }
        }
        Crashlytics.setString(WEB_VIEW_MODE, "Normal");
    }

    public static String getChromeVersion(Context context) {
        try {
            Matcher chrome = VERSION_REGEX.matcher(WebSettings.getDefaultUserAgent(context));
            if (chrome.find()) {
                return chrome.group(1);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return "unknown";
    }
}
