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

import androidx.annotation.ColorInt

val COLORS = intArrayOf(-0xbbcca, -0x16e19d, -0x63d850, -0x98c549, -0xc0ae4b, -0xde690d, -0xfc560c, -0xff432c, -0xff6978, -0xb350b0, -0x743cb6, -0x3223c7, -0x14c5, -0x3ef9, -0x6800, -0xa8de, -0x86aab8, -0x616162, -0x9f8275, -0xdededf)

internal val randomColor: Int
    @ColorInt
    get() = COLORS[(Math.random() * COLORS.size).toInt()]

@ColorInt
internal fun String.getIdentityColor(): Int {
    return COLORS[Math.abs(hashCode() % COLORS.size)]
}
