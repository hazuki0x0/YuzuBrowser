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

package jp.hazuki.yuzubrowser.search.di

import android.app.Application
import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import jp.hazuki.yuzubrowser.bookmark.repository.BookmarkManager
import jp.hazuki.yuzubrowser.history.repository.BrowserHistoryManager
import jp.hazuki.yuzubrowser.search.domain.ISearchUrlRepository
import jp.hazuki.yuzubrowser.search.domain.ISuggestRepository
import jp.hazuki.yuzubrowser.search.domain.usecase.SearchSettingsViewUseCase
import jp.hazuki.yuzubrowser.search.domain.usecase.SearchViewUseCase
import jp.hazuki.yuzubrowser.search.presentation.search.SearchViewModel
import jp.hazuki.yuzubrowser.search.presentation.settings.SearchSettingsViewModel
import jp.hazuki.yuzubrowser.search.repository.SearchUrlManager
import jp.hazuki.yuzubrowser.search.repository.SuggestRepository
import jp.hazuki.yuzubrowser.ui.provider.ISuggestProvider

@Module
object SearchSubModule {

    @Provides
    @JvmStatic
    fun provideSuggestRepository(application: Application, suggestProvider: ISuggestProvider): ISuggestRepository {
        return SuggestRepository(application, suggestProvider)
    }

    @Provides
    @JvmStatic
    fun provideSearchUrlRepository(context: Context, moshi: Moshi): ISearchUrlRepository {
        return SearchUrlManager(context, moshi)
    }

    @Provides
    @JvmStatic
    internal fun provideSearchViewUseCase(context: Context, suggestRepository: ISuggestRepository, searchUrlRepository: ISearchUrlRepository): SearchViewUseCase {
        return SearchViewUseCase(
            BookmarkManager.getInstance(context),
            BrowserHistoryManager.getInstance(context),
            suggestRepository,
            searchUrlRepository
        )
    }

    @Provides
    @JvmStatic
    internal fun provideSearchViewModelFactory(application: Application, useCase: SearchViewUseCase): SearchViewModel.Factory {
        return SearchViewModel.Factory(application, useCase)
    }

    @Provides
    @JvmStatic
    internal fun provideSearchSettingsViewUseCase(searchUrlRepository: ISearchUrlRepository): SearchSettingsViewUseCase {
        return SearchSettingsViewUseCase(searchUrlRepository)
    }

    @Provides
    @JvmStatic
    internal fun provideSearchSettingsViewModelFactory(application: Application, useCase: SearchSettingsViewUseCase): SearchSettingsViewModel.Factory {
        return SearchSettingsViewModel.Factory(application, useCase)
    }
}
