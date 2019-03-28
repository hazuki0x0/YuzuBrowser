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
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.pickValue
import okio.buffer
import okio.sink
import okio.source
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ShareScreenshotSingleActionTest {

    @Test
    fun testDecodeEncode() {
        val fiftyJson = """{"0":2}"""


        val reader = JsonReader.of(ByteArrayInputStream(fiftyJson.toByteArray()).source().buffer())
        val action = ShareScreenshotSingleAction(0, reader)

        assertThat(action.pickValue<Int>("mSsType")).isEqualTo(2)
        assertThat(reader.peek()).isEqualTo(JsonReader.Token.END_DOCUMENT)

        val os = ByteArrayOutputStream()
        val writer = JsonWriter.of(os.sink().buffer())
        writer.beginArray()
        action.writeIdAndData(writer)
        writer.endArray()
        writer.flush()
        assertThat(os.toString()).isEqualTo("""[0,{"0":2}]""")
    }
}