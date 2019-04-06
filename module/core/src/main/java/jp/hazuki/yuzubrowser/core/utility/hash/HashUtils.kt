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

package jp.hazuki.yuzubrowser.core.utility.hash


fun formatHashString(hash: Long): String {
    return String.format("%016x", hash)
}

fun parseHashString(hashString: String): Long {
    val len = hashString.length
    return if (len <= 15 || hashString[0] == '0') {
        hashString.toLong(16)
    } else {
        val first = hashString.substring(0, len - 1).toLong(16)
        val second = Character.digit(hashString[len - 1], 16)
        (first shl 4) + second
    }
}

