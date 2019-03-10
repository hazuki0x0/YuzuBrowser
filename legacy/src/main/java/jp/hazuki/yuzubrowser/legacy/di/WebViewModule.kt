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

package jp.hazuki.yuzubrowser.legacy.di

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import jp.hazuki.yuzubrowser.core.settings.WebViewPrefs
import jp.hazuki.yuzubrowser.webview.WebViewFactory

@Module
object WebViewModule {

    @Provides
    @JvmStatic
    fun provideWebViewFactory(moshi: Moshi, webViewPrefs: WebViewPrefs): WebViewFactory {
        return WebViewFactory(moshi, webViewPrefs)
    }

    @Provides
    @JvmStatic
    fun provideWebViewPrefs(prefs: SharedPreferences): WebViewPrefs {
        return WebViewPrefs(prefs)
    }
}