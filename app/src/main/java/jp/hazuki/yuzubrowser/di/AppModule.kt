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

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import jp.hazuki.yuzubrowser.YuzuBrowserApplication
import jp.hazuki.yuzubrowser.kotshi.ApplicationJsonAdapterFactory
import jp.hazuki.yuzubrowser.legacy.BrowserApplication
import jp.hazuki.yuzubrowser.webview.kotshi.WebViewJsonAdapterFactory
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @JvmStatic
    fun provideBrowserApplication(app: YuzuBrowserApplication): BrowserApplication {
        return app
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
                .add(ApplicationJsonAdapterFactory.INSTANCE)
                .add(WebViewJsonAdapterFactory.INSTANCE)
                .build()
    }
}