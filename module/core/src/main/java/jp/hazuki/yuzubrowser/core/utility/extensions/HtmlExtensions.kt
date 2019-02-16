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

package jp.hazuki.yuzubrowser.core.utility.extensions

import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*

fun getNoCacheResponse(mimeType: String, sequence: CharSequence): WebResourceResponse {
    return getNoCacheResponse(mimeType, ByteArrayInputStream(sequence.toString().toByteArray(StandardCharsets.UTF_8)))
}

fun getNoCacheResponse(mimeType: String, stream: InputStream): WebResourceResponse {
    val response = WebResourceResponse(mimeType, "UTF-8", stream)
    response.responseHeaders = HashMap<String, String>().apply { put("Cache-Control", "no-cache") }
    return response
}