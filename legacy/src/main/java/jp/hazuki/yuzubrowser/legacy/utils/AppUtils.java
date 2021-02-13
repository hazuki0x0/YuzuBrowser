/*
 * Copyright (C) 2017-2021 Hazuki
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
import android.os.Build;

import jp.hazuki.yuzubrowser.core.utility.extensions.ContextExtensionsKt;
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs;

public class AppUtils {

    public static String getVersionDeviceInfo(Context context) {
        return "Yuzu " + ContextExtensionsKt.getVersionName(context) + "/" +
            Build.MANUFACTURER + "/" +
            Build.MODEL + "/" +
            Build.VERSION.RELEASE + "/" +
            getWebViewMode();
    }

    private static String getWebViewMode() {
        if (AppPrefs.fast_back.get()) {
            int size = AppPrefs.fast_back_cache_size.get();
            if (size == 0) {
                return "I";
            } else if (size > 1) {
                return "L";
            }
        }
        return "N";
    }
}
