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

package jp.hazuki.yuzubrowser.ui.extensions

import android.net.Uri
import android.text.TextPaint
import androidx.core.net.toUri
import jp.hazuki.yuzubrowser.core.utility.extensions.decodeUrl
import jp.hazuki.yuzubrowser.core.utility.extensions.toEncodePunyCodeUri
import java.net.IDN

fun String.decodePunyCodeUrlHost(): String? {
    val host = getHost()
    return if (!host.isNullOrEmpty()) IDN.toUnicode(host) else null
}

fun String.getHost(): String? {
    return Uri.parse(this).host
}

fun String?.decodePunyCodeUrl(): String? {
    return if (this == null) null else Uri.parse(this).decodeUrl()
}

fun String.toPunyCodeUrl(): String {
    return toUri().toEncodePunyCodeUri().toString()
}

fun CharSequence.ellipsizeUrl(p: TextPaint, avail: Float): CharSequence {
    val len = length

    val wid = p.measureText(this, 0, len)
    if (wid <= avail) {
        return this
    }

    val fit = p.breakText(this, 0, len, true, avail, null)

    return toString().substring(0, fit)
}