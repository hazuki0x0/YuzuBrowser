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

package jp.hazuki.yuzubrowser.search.domain

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

private const val GOOGLE_IMAGE_SEARCH = "https://www.google.com/searchbyimage?image_url="

fun String.makeGoogleImageSearch(): String {
    try {
        return GOOGLE_IMAGE_SEARCH + URLEncoder.encode(this, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    }

    return ""
}
