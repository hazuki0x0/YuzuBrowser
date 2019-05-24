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

package jp.hazuki.yuzubrowser.bookmark.overflow.viewmodel

import android.app.Application
import android.view.MenuItem
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.hazuki.yuzubrowser.bookmark.overflow.model.OverflowMenuModel
import jp.hazuki.yuzubrowser.bookmark.repository.HideMenuRepository
import javax.inject.Inject


class OverflowMenuViewModel @Inject constructor(
    application: Application,
    private val repository: HideMenuRepository
) : AndroidViewModel(application) {

    val menuModels = MutableLiveData<List<OverflowMenuModel>>()

    fun setOverflowMenus(type: Int, menuList: List<MenuItem>) {
        val hides = repository.getHideMenu(type).toHashSet()
        menuModels.value = menuList.map { OverflowMenuModel(it.itemId, !hides.contains(it.itemId), it.title.toString()) }
    }

    fun save(type: Int) {
        menuModels.value?.let { models ->
            repository.saveHideMenu(type, models.filter { !it.enable }.map { it.id })
        }
    }


    class Factory(
        private val application: Application,
        private val repository: HideMenuRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return OverflowMenuViewModel(application, repository) as T
        }
    }
}
