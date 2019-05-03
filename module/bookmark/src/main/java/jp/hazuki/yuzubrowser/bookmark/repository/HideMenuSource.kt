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

package jp.hazuki.yuzubrowser.bookmark.repository

import android.app.Application
import android.content.Context
import android.util.SparseArray
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import jp.hazuki.yuzubrowser.bookmark.overflow.HideMenuType
import jp.hazuki.yuzubrowser.bookmark.overflow.MenuType
import jp.hazuki.yuzubrowser.bookmark.overflow.model.HideModel
import jp.hazuki.yuzubrowser.core.utility.extensions.getOrPut
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import javax.inject.Inject

class HideMenuSource @Inject constructor(val application: Application, private val moshi: Moshi) : HideMenuRepository {

    private val root = application.getDir("bookmark1", Context.MODE_PRIVATE)

    private val cache = SparseArray<List<HideModel>>(2)

    override fun getHideMenu(@HideMenuType type: Int): List<HideModel> {
        return cache.getOrPut(type) { getInternal(getFile(type)) }
    }

    override fun saveHideMenu(@HideMenuType type: Int, hides: List<HideModel>) {
        cache.put(type, hides)
        saveInternal(getFile(type), hides)
    }

    private fun getFile(@HideMenuType type: Int): File {
        return when (type) {
            MenuType.SITE -> File(root, SITE)
            MenuType.FOLDER -> File(root, FOLDER)
            else -> throw IllegalArgumentException()
        }
    }

    private fun getInternal(file: File): List<HideModel> {
        val adapter = moshi.adapter<List<HideModel>>(hideListType)
        if (file.exists()) {
            try {
                file.inputStream().source().buffer().use {
                    adapter.fromJson(it)?.let { list ->
                        return list
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }

    private fun saveInternal(file: File, hides: List<HideModel>) {
        val adapter = moshi.adapter<List<HideModel>>(hideListType)
        try {
            file.sink().buffer().use {
                adapter.toJson(it, hides)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val hideListType = Types.newParameterizedType(List::class.java, Integer::class.java)

    companion object {
        private const val SITE = "hide_menu_site"

        private const val FOLDER = "hide_menu_folder"
    }
}
