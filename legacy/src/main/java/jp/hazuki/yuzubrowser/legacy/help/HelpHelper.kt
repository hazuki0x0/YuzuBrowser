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

package jp.hazuki.yuzubrowser.legacy.help

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import jp.hazuki.yuzubrowser.core.utility.extensions.getNoCacheResponse
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.utils.getMimeType
import java.io.IOException

fun Uri.isHelpUrl(): Boolean {
    return scheme == "yuzu" && host == "help"
}

fun Uri.getHelpResponse(context: Context): WebResourceResponse? {
    val path = path ?: return null

    val typeEnd = path.indexOf('/', 1)
    if (typeEnd >= 0 && typeEnd != path.lastIndex) {
        val type = path.substring(1, typeEnd)
        if (type == "raw") {
            val filePath = path.substring(typeEnd + 1)
            val mimeType = getMimeType(filePath)
            val lastDot = filePath.lastIndexOf('.')
            if (lastDot < 0) return null
            val fileName = filePath.substring(0, lastDot)
            val id = context.resources.getIdentifier("help_$fileName", "raw", context.packageName)
            if (id == 0) return null
            val input = context.resources.openRawResource(id)
            return getNoCacheResponse(mimeType, input)
        }

    }

    val mimeType = getMimeType(path)
    try {
        val input = context.assets.open("help$path")
        return getNoCacheResponse(mimeType, input)
    } catch (e: IOException) {
        ErrorReport.printAndWriteLog(e)
    }
    return null
}

const val BROWSER_HELP_URL = "yuzu://help/index.html"
