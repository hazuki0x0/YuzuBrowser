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
import jp.hazuki.yuzubrowser.core.MIME_TYPE_UNKNOWN
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
const val AD_BLOCK_XML_HTTP_REQUEST = 1024

fun WebResourceRequest.convertToAdBlockContentType(siteUrl: String): Int {
    var contentType = 0
    val scheme = url.scheme
    var isPage = false
    if (isForMainFrame) {
        if (url.toString() == siteUrl) {
            isPage = true
            contentType = contentType or AD_BLOCK_DOCUMENT
        }
    } else {
        contentType = contentType or AD_BLOCK_SUB_DOCUMENT
    }
    if (scheme == "ws" || scheme == "wss") {
        contentType = contentType or AD_BLOCK_WEBSOCKET
    }

    if (requestHeaders["X-Requested-With"] == "XMLHttpRequest") {
        contentType = contentType or AD_BLOCK_XML_HTTP_REQUEST
    }

    val path = url.path ?: url.toString()
    val lastDot = path.lastIndexOf('.')
    if (lastDot >= 0) {
        when (val extension = path.substring(lastDot + 1).toLowerCase()) {
            "js" -> return contentType or AD_BLOCK_SCRIPT
            "css" -> return contentType or AD_BLOCK_STYLE_SHEET
            "otf", "ttf", "ttc", "woff", "woff2" -> return contentType or AD_BLOCK_FONT
            "php" -> Unit
            else -> {
                val mimeType = getMimeTypeFromExtension(extension)
                if (mimeType != MIME_TYPE_UNKNOWN) {
                    return contentType or mimeType.getContentTypeFromMimeType()
                }
            }
        }
    }

    if (isPage) {
        return contentType or AD_BLOCK_OTHER
    }

    val accept = requestHeaders["Accept"]
    return if (accept != null && accept != "*/*") {
        val mimeType = accept.split(',')[0]
        contentType or mimeType.getContentTypeFromMimeType()
    } else {
        contentType or AD_BLOCK_OTHER or AD_BLOCK_MEDIA or AD_BLOCK_IMAGE or
            AD_BLOCK_FONT or AD_BLOCK_STYLE_SHEET or AD_BLOCK_SCRIPT
    }
}

private fun String.getContentTypeFromMimeType(): Int {
    return when (this) {
        "application/javascript", "application/x-javascript", "text/javascript", "application/json" -> AD_BLOCK_SCRIPT
        "text/css" -> AD_BLOCK_STYLE_SHEET
        else -> when {
            startsWith("image/") -> AD_BLOCK_IMAGE
            startsWith("video/") || startsWith("audio/") -> AD_BLOCK_MEDIA
            startsWith("font/") -> AD_BLOCK_FONT
            else -> AD_BLOCK_OTHER
        }
    }
}

fun WebResourceRequest.isThirdParty(documentHost: String): Boolean {
    val hostName = url.host ?: return true

    if (hostName == documentHost) return false

    if (Patterns.IP_ADDRESS.matcher(documentHost).matches() ||
            Patterns.IP_ADDRESS.matcher(hostName).matches()) return true

    val db = PublicSuffixDatabase.get()

    return db.getEffectiveTldPlusOne(documentHost) != db.getEffectiveTldPlusOne(hostName)
}
