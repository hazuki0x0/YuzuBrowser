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

package jp.hazuki.yuzubrowser.core.utility.utils

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

@ExperimentalUnsignedTypes
@Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
class HashUtilsKtTest {

    @Test
    fun formatHashString() {
        assertThat(jp.hazuki.yuzubrowser.core.utility.hash.formatHashString(1)).isEqualTo("0000000000000001")
        assertThat(jp.hazuki.yuzubrowser.core.utility.hash.formatHashString(0xff)).isEqualTo("00000000000000ff")
        assertThat(jp.hazuki.yuzubrowser.core.utility.hash.formatHashString(0x7fffffffffffffff)).isEqualTo("7fffffffffffffff")
        assertThat(jp.hazuki.yuzubrowser.core.utility.hash.formatHashString(-1)).isEqualTo("ffffffffffffffff")
        assertThat(jp.hazuki.yuzubrowser.core.utility.hash.formatHashString(0xffffffff00000000u.toLong())).isEqualTo("ffffffff00000000")
    }

    @Test
    fun parseHashString() {
        assertThat(jp.hazuki.yuzubrowser.core.utility.hash.parseHashString("0000000000000001")).isEqualTo(1)
        assertThat(jp.hazuki.yuzubrowser.core.utility.hash.parseHashString("00000000000000ff")).isEqualTo(0xff)
        assertThat(jp.hazuki.yuzubrowser.core.utility.hash.parseHashString("7fffffffffffffff")).isEqualTo(0x7fffffffffffffff)
        assertThat(jp.hazuki.yuzubrowser.core.utility.hash.parseHashString("ffffffffffffffff")).isEqualTo(0xffffffffffffffffu.toLong())
        assertThat(jp.hazuki.yuzubrowser.core.utility.hash.parseHashString("ffffffff00000000")).isEqualTo(0xffffffff00000000u.toLong())
    }
}