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

package jp.hazuki.yuzubrowser.legacy.resblock.data

import android.content.Context
import android.webkit.WebResourceResponse
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.resblock.ResourceData
import java.io.IOException
import java.io.InputStream

class EmptyStringData : ResourceData {

    constructor()

    @Throws(IOException::class)
    constructor(reader: JsonReader) {
        //if(parser.nextToken() == JsonToken.VALUE_NULL) return;
    }

    override fun getTypeId(): Int {
        return EMPTY_STRING_DATA
    }

    override fun getTitle(context: Context): String? {
        return null
    }

    override fun getResource(context: Context): WebResourceResponse {
        return WebResourceResponse("text/html", "UTF-8", sInputStream)
    }

    @Throws(IOException::class)
    override fun write(writer: JsonWriter): Boolean {
        writer.value(ResourceData.EMPTY_STRING_DATA.toLong())
        return true
    }

    private class EmptyInputStream : InputStream() {
        @Throws(IOException::class)
        override fun read(): Int {
            return -1
        }
    }

    companion object {
        private val sInputStream = EmptyInputStream()
    }
}
