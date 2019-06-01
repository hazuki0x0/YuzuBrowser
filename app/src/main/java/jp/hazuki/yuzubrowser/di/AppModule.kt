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

package jp.hazuki.yuzubrowser.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import jp.hazuki.yuzubrowser.YuzuBrowserApplication
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import jp.hazuki.yuzubrowser.kotshi.ApplicationJsonAdapterFactory
import jp.hazuki.yuzubrowser.legacy.kotshi.LegacyJsonAdapterFactory
import jp.hazuki.yuzubrowser.provider.SuggestProviderBridge
import jp.hazuki.yuzubrowser.search.di.SearchJsonAdapterFactory
import jp.hazuki.yuzubrowser.ui.BrowserApplication
import jp.hazuki.yuzubrowser.ui.provider.ISuggestProvider
import jp.hazuki.yuzubrowser.webview.kotshi.WebViewJsonAdapterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object AppModule {
    private const val PREFERENCE_NAME = "main_preference"

    @Provides
    @JvmStatic
    fun provideContext(app: YuzuBrowserApplication): Context {
        return app
    }

    @Provides
    @JvmStatic
    fun provideBrowserApplication(app: YuzuBrowserApplication): BrowserApplication {
        return app
    }

    @Provides
    @JvmStatic
    fun provideApplication(app: YuzuBrowserApplication): Application {
        return app
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(ApplicationJsonAdapterFactory.INSTANCE)
            .add(LegacyJsonAdapterFactory.INSTANCE)
            .add(WebViewJsonAdapterFactory.INSTANCE)
            .add(SearchJsonAdapterFactory.INSTANCE)
            .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @JvmStatic
    fun provideSuggestProvider(): ISuggestProvider {
        return SuggestProviderBridge()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideFaviconManager(context: Context): FaviconManager {
        return FaviconManager(context)
    }
}
