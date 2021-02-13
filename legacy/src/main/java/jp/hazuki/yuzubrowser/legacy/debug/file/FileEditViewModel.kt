/*
 * Copyright 2020 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.debug.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.hazuki.yuzubrowser.core.lifecycle.KotlinLiveData
import java.io.File
import java.io.IOException

class FileEditViewModel(
    private val file: File
) : ViewModel() {
    val text = KotlinLiveData("")

    init {
        load()
    }

    private fun load() {
        try {
            text *= file.readText()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            file.writeText(text.value)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    class Factory(val file: File) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FileEditViewModel(file) as T
        }
    }
}
