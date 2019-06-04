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

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import jp.hazuki.yuzubrowser.YuzuBrowserApplication
import jp.hazuki.yuzubrowser.adblock.AdBlockModule
import jp.hazuki.yuzubrowser.adblock.AdBlockUiModule
import jp.hazuki.yuzubrowser.bookmark.BookmarkUiModule
import jp.hazuki.yuzubrowser.browser.di.ActivityModule
import jp.hazuki.yuzubrowser.download.DownloadModule
import jp.hazuki.yuzubrowser.history.di.HistoryModule
import jp.hazuki.yuzubrowser.legacy.di.LegacyUiModule
import jp.hazuki.yuzubrowser.legacy.settings.di.SettingsModule
import jp.hazuki.yuzubrowser.legacy.useragent.UserAgentModule
import jp.hazuki.yuzubrowser.legacy.webencode.WebEncodeModule
import jp.hazuki.yuzubrowser.search.di.SearchModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    ActivityModule::class,
    ProviderModule::class,
    SearchModule::class,
    WebEncodeModule::class,
    UserAgentModule::class,
    DownloadModule::class,
    AdBlockModule::class,
    AdBlockUiModule::class,
    BookmarkUiModule::class,
    HistoryModule::class,
    SettingsModule::class,
    LegacyUiModule::class
])
interface AppComponent : AndroidInjector<YuzuBrowserApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: YuzuBrowserApplication): Builder

        fun build(): AppComponent
    }
}
