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

package jp.hazuki.yuzubrowser.adblock.filter.abp

import assertk.assertions.isEqualTo
import org.junit.Test

class AbpUtilsKtTest {

    @Test
    fun testConvertToRegexText() {
        assertk.assertThat("test.com".convertToRegexText()).isEqualTo("test\\.com")
        assertk.assertThat("test.com/*/ads*".convertToRegexText()).isEqualTo("test\\.com/.*/ads")
        assertk.assertThat("||test.net".convertToRegexText()).isEqualTo("^[\\w\\-]+:\\/+(?!\\/)(?:[^\\/]+\\.)?test\\.net")
        assertk.assertThat("test.org^".convertToRegexText()).isEqualTo("test\\.org(?:[\\x00-\\x24\\x26-\\x2C\\x2F\\x3A-\\x40\\x5B-\\x5E\\x60\\x7B-\\x7F]|$)")
        assertk.assertThat("|ads.com|".convertToRegexText()).isEqualTo("^ads\\.com$")
    }
}