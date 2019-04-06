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

package jp.hazuki.yuzubrowser.legacy.licenses

import android.content.Context
import jp.hazuki.yuzubrowser.core.utility.utils.IOUtils
import java.io.BufferedInputStream
import java.util.zip.GZIPInputStream

class LicenseFileExtractor(context: Context) {
    private val assetManager = context.resources.assets

    fun extract(): String {
        val input = assetManager.open(FILE)
        GZIPInputStream(BufferedInputStream(input)).use {
            return IOUtils.readString(it)
        }
    }

    companion object {
        private const val FILE = "licenses.compressed"
    }
}