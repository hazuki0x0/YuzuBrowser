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

package jp.hazuki.yuzubrowser.adblock

import android.util.Patterns
import android.webkit.WebResourceRequest
import jp.hazuki.yuzubrowser.core.utility.utils.getMimeTypeFromExtension
import okhttp3.internal.publicsuffix.PublicSuffixDatabase

const val BROADCAST_ACTION_UPDATE_AD_BLOCK_DATA = "jp.hazuki.yuzubrowser.adblock.broadcast.update.adblock"

const val AD_BLOCK_OTHER = 1
const val AD_BLOCK_SCRIPT = 2
const val AD_BLOCK_IMAGE = 4
const val AD_BLOCK_STYLE_SHEET = 8
const val AD_BLOCK_SUB_DOCUMENT = 16
const val AD_BLOCK_DOCUMENT = 32
const val AD_BLOCK_MEDIA = 64
const val AD_BLOCK_FONT = 128
const val AD_BLOCK_POPUP = 256
const val AD_BLOCK_WEBSOCKET = 512

fun WebResourceRequest.convertToAdBlockContentType(siteUrl: String): Int {
    var contentType = 0
    val scheme = url.scheme
    val url = url.toString()
    var isPage = false
    if (isForMainFrame) {
        if (url == siteUrl) {
            isPage = true
            contentType = contentType or AD_BLOCK_DOCUMENT
        }
    } else {
        contentType = contentType or AD_BLOCK_SUB_DOCUMENT
    }
    if (scheme == "ws" || scheme == "wss") {
        contentType = contentType or AD_BLOCK_WEBSOCKET
    }

    val lastDot = url.lastIndexOf('.')
    contentType = when {
        lastDot >= 0 -> when (val extension = url.substring(lastDot + 1).toLowerCase()) {
            "js" -> contentType or AD_BLOCK_SCRIPT
            "css" -> contentType or AD_BLOCK_STYLE_SHEET
            "otf", "ttf", "ttc", "woff", "woff2" -> contentType or AD_BLOCK_FONT
            else -> {
                val mimeType = getMimeTypeFromExtension(extension)
                when {
                    mimeType.startsWith("image/") -> contentType or AD_BLOCK_IMAGE
                    mimeType.startsWith("video/") || mimeType.startsWith("audio/") -> contentType or AD_BLOCK_MEDIA
                    mimeType.startsWith("font/") -> contentType or AD_BLOCK_FONT
                    else -> contentType or AD_BLOCK_OTHER
                }
            }
        }
        isPage -> contentType or AD_BLOCK_OTHER
        else -> contentType or AD_BLOCK_OTHER or AD_BLOCK_MEDIA or AD_BLOCK_IMAGE or
                AD_BLOCK_FONT or AD_BLOCK_STYLE_SHEET or AD_BLOCK_SCRIPT
    }

    return contentType
}

fun WebResourceRequest.isThirdParty(documentHost: String): Boolean {
    val hostName = url.host

    if (hostName == documentHost) return false

    if (Patterns.IP_ADDRESS.matcher(documentHost).matches() ||
            Patterns.IP_ADDRESS.matcher(hostName).matches()) return true

    val db = PublicSuffixDatabase.get()

    return db.getEffectiveTldPlusOne(documentHost) != db.getEffectiveTldPlusOne(hostName)
}
