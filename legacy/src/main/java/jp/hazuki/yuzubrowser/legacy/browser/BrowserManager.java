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

package jp.hazuki.yuzubrowser.legacy.browser;

import android.content.Context;
import android.webkit.GeolocationPermissions;
import android.webkit.WebStorage;

import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils;

public class BrowserManager {
    //same as pref_newtab_values
    public static final int LOAD_URL_TAB_CURRENT = 0;
    public static final int LOAD_URL_TAB_NEW = 1;
    public static final int LOAD_URL_TAB_BG = 2;
    public static final int LOAD_URL_TAB_NEW_RIGHT = 3;
    public static final int LOAD_URL_TAB_BG_RIGHT = 4;
    public static final int LOAD_URL_TAB_CURRENT_FORCE = 5;

    public static void clearAppCacheFile(Context context) {
        FileUtils.deleteFile(context.getDir("appcache", Context.MODE_PRIVATE));
    }

    public static void clearWebDatabase() {
        WebStorage.getInstance().deleteAllData();
    }

    public static void clearCache(Context context) {
        FileUtils.deleteDirectoryContents(context.getCacheDir());
    }

    public static void clearGeolocation() {
        GeolocationPermissions.getInstance().clearAll();
    }

}
