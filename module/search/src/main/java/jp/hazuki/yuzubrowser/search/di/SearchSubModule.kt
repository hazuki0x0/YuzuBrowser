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
import jp.hazuki.yuzubrowser.search.blogic.ISearchUrlRepository
import jp.hazuki.yuzubrowser.search.blogic.ISuggestRepository
import jp.hazuki.yuzubrowser.search.blogic.usecase.SearchViewUseCase
import jp.hazuki.yuzubrowser.search.presentation.search.SearchViewModel
import jp.hazuki.yuzubrowser.search.repository.SearchPrefsProvider
import jp.hazuki.yuzubrowser.search.repository.SearchUrlManager
import jp.hazuki.yuzubrowser.search.repository.SuggestRepository
import jp.hazuki.yuzubrowser.ui.provider.ISuggestProvider

@Module
object SearchSubModule {

    @Provides
    @JvmStatic
    internal fun provideSearchPrefProvider(context: Context): SearchPrefsProvider {
        return SearchPrefsProvider(context)
    }

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
    internal fun provideSearchViewModelFactory(application: Application, useCase: SearchViewUseCase, provider: SearchPrefsProvider): SearchViewModel.Factory {
        return SearchViewModel.Factory(application, useCase, provider)
    }
}
