/*
 * Copyright (C) 2017-2018 Hazuki
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

package jp.hazuki.yuzubrowser.download.core.utils

import android.support.v4.provider.DocumentFile
import jp.hazuki.yuzubrowser.download.core.downloader.client.HttpClient
import jp.hazuki.yuzubrowser.utils.FileUtils
import jp.hazuki.yuzubrowser.utils.createUniqueFileName
import java.net.URLDecoder
import java.util.regex.Pattern

private val NAME_UTF_8 = Pattern.compile("filename\\*=UTF-8''(\\S+)")
private val NAME_NORMAL = Pattern.compile("filename=\"(.*)\"")

fun HttpClient.getFileName(root: DocumentFile, url: String, mimeType: String?, defaultExt: String?): String {
    headerFields["Content-Disposition"]?.let { contents ->
        for (raw in contents) {
            val utf8 = NAME_UTF_8.matcher(raw)
            if (utf8.find()) { /* RFC 6266 */
                return createUniqueFileName(root, URLDecoder.decode(utf8.group(1), "UTF-8"))
            }
            val normal = NAME_NORMAL.matcher(raw)
            if (normal.find()) {
                return try {
                    createUniqueFileName(root, URLDecoder.decode(normal.group(1), "UTF-8"))
                } catch (e: IllegalArgumentException) {
                    createUniqueFileName(root, FileUtils.replaceProhibitionWord(normal.group(1)))
                }
            }
        }
    }

    return guessDownloadFileName(root, url, null, mimeType, defaultExt)
}

val HttpClient.mimeType: String
    get() {
        val lines = headerFields["Content-Type"]
        if (lines != null) {
            if (lines.isNotEmpty()) {
                var mineType = lines[0]
                val index = mineType.indexOf(';')
                if (index > -1) {
                    mineType = mineType.substring(0, index)
                }
                return mineType
            }
        }
        return "application/octet-stream"
    }

val HttpClient.contentLength: Long
    get() {
        val str = getHeaderField("Content-Length")
        try {
            return str?.toLong() ?: -1
        } catch (e: NumberFormatException) {
        }
        return -1
    }

val HttpClient.isResumable: Boolean
    get() {
        val range = getHeaderField("Accept-Ranges")
        return range == "bytes"
    }