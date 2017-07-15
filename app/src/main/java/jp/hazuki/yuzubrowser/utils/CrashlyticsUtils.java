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

package jp.hazuki.yuzubrowser.utils;

import android.content.Context;
import android.webkit.WebSettings;

import com.crashlytics.android.Crashlytics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CrashlyticsUtils {
    private static Pattern VERSION_REGEX = Pattern.compile("Chrome/([.0-9]+)");
    private static final String CHROME_VERSION = "chrome version";

    public static void setChromeVersion(Context context) {
        Matcher chrome = VERSION_REGEX.matcher(WebSettings.getDefaultUserAgent(context));
        if (chrome.find()) {
            Crashlytics.setString(CHROME_VERSION, chrome.group(1));
        }
    }
}
