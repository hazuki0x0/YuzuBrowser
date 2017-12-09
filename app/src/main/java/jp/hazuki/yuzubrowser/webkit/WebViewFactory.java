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

package jp.hazuki.yuzubrowser.webkit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import jp.hazuki.yuzubrowser.settings.data.AppData;

public class WebViewFactory {
    public static final int MODE_NORMAL = 1;
    public static final int MODE_CACHE = 2;
    public static final int MODE_LIMIT_CACHE = 3;

    @NonNull
    public static CustomWebView create(@NonNull Context context, @WebViewType int mode) {
        switch (mode) {
            default:
            case MODE_NORMAL:
                return new SwipeWebView(context);
            case MODE_CACHE:
                return new CacheWebView(context);
            case MODE_LIMIT_CACHE:
                return new LimitCacheWebView(context);
        }
    }

    @WebViewType
    public static int getMode(@NonNull Bundle bundle) {
        if (CacheWebView.isBundleCacheWebView(bundle)) {
            return MODE_CACHE;
        } else if (LimitCacheWebView.isBundleFastBackWebView(bundle)) {
            return MODE_LIMIT_CACHE;
        } else {
            return MODE_NORMAL;
        }
    }

    @WebViewType
    public static int getMode() {
        if (AppData.fast_back.get()) {
            switch (AppData.fast_back_cache_size.get()) {
                case 0:
                    return MODE_CACHE;
                case 1:
                    return MODE_NORMAL;
                default:
                    return MODE_LIMIT_CACHE;
            }
        } else {
            return MODE_NORMAL;
        }
    }
}
