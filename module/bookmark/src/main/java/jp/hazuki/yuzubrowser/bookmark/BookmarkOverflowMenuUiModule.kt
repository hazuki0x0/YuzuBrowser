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

package jp.hazuki.yuzubrowser.bookmark

import android.app.Application
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import jp.hazuki.yuzubrowser.bookmark.overflow.viewmodel.OverflowMenuViewModel
import jp.hazuki.yuzubrowser.bookmark.repository.HideMenuRepository
import jp.hazuki.yuzubrowser.bookmark.repository.HideMenuSource

@Module
object BookmarkOverflowMenuUiModule {

    @JvmStatic
    @Provides
    fun provideHideMenuRepository(application: Application, moshi: Moshi): HideMenuRepository {
        return HideMenuSource(application, moshi)
    }

    @JvmStatic
    @Provides
    fun provideOverflowMenuViewModelFactory(application: Application, hideMenuRepository: HideMenuRepository): OverflowMenuViewModel.Factory {
        return OverflowMenuViewModel.Factory(application, hideMenuRepository)
    }
}
