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

package jp.hazuki.yuzubrowser.legacy.action.item

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.squareup.moshi.JsonReader
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import okio.buffer
import okio.source
import org.junit.Test
import java.io.ByteArrayInputStream

class FinishSingleActionTest {

    @Test
    fun testTrueDecode() {
        val fiftyJson = """{"0":true, "1":true}"""


        val trueReader = JsonReader.of(ByteArrayInputStream(fiftyJson.toByteArray()).source().buffer())
        val trueAction = FinishSingleAction(SingleAction.PAGE_AUTO_SCROLL, trueReader)

        assertThat(trueAction.isCloseTab).isEqualTo(true)
        assertThat(trueAction.isShowAlert).isEqualTo(true)
        assertThat(trueReader.peek()).isEqualTo(JsonReader.Token.END_DOCUMENT)
    }

    @Test
    fun testFalseDecode() {
        val fiftyJson = """{"0":false, "1":false}"""


        val trueReader = JsonReader.of(ByteArrayInputStream(fiftyJson.toByteArray()).source().buffer())
        val trueAction = FinishSingleAction(SingleAction.PAGE_AUTO_SCROLL, trueReader)

        assertThat(trueAction.isCloseTab).isEqualTo(false)
        assertThat(trueAction.isShowAlert).isEqualTo(false)
        assertThat(trueReader.peek()).isEqualTo(JsonReader.Token.END_DOCUMENT)
    }
}