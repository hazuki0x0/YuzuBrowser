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

package jp.hazuki.yuzubrowser.legacy.settings.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.hazuki.yuzubrowser.legacy.settings.activity.ImportExportFragment
import jp.hazuki.yuzubrowser.legacy.settings.preference.ClearBrowserDataAlertDialog

@Module
abstract class SettingsModule {

    @ContributesAndroidInjector
    abstract fun contributeClearBrowserDataAlertDialog(): ClearBrowserDataAlertDialog.ClearDialog

    @ContributesAndroidInjector
    abstract fun contributeImportExportFragment(): ImportExportFragment
}
