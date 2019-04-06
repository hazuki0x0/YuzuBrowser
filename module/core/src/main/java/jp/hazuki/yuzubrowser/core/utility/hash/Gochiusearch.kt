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

/**
 * Calculate vector hash from ARGB image bytes
 */
fun ByteArray.getVectorHash(): Long {
    val mono = IntArray(size / 4)
    for (i in mono.indices) {
        mono[i] = 150 * this[i * 4 + 1] + 77 * this[i * 4 + 2] + 29 * this[i * 4 + 3] shr 8
    }

    var result: Long = 0
    var p = 0

    for (y in 0..7) {
        for (x in 0..7) {
            result = result shl 1 or if (mono[p] > mono[p + 1]) 1 else 0
            p++
        }
        p++
    }

    return result
}