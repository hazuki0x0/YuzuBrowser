/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.theme

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.legacy.utils.JsonUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class ThemeManifest @Throws(IllegalManifestException::class, IOException::class)
private constructor(val version: String, val name: String, val id: String) {

    class IllegalManifestException internal constructor(message: String, val errorType: Int) : Exception(message)

    companion object {
        val MANIFEST = "manifest.json"

        private val FORMAT_VERSION = 1

        private val FIELD_FORMAT_VERSION = "format_version"
        private val FIELD_VERSION = "version"
        private val FIELD_NAME = "name"
        private val FIELD_ID = "id"

        fun getManifest(themeFolder: File): ThemeManifest? {
            try {
                return decodeManifest(File(themeFolder, MANIFEST))
            } catch (e: IllegalManifestException) {
                e.printStackTrace()
            }

            return null
        }

        @Throws(ThemeManifest.IllegalManifestException::class)
        fun decodeManifest(manifestFile: File): ThemeManifest? {
            if (manifestFile.exists() && manifestFile.isFile) {
                try {
                    BufferedInputStream(FileInputStream(manifestFile)).use { JsonUtils.getFactory().createParser(it).use { parser -> return decode(parser) } }
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw ThemeManifest.IllegalManifestException("unknown error", 0)
                }

            } else {
                return null
            }
        }

        @Throws(IllegalManifestException::class, IOException::class)
        private fun decode(parser: JsonParser): ThemeManifest {
            if (parser.nextToken() != JsonToken.START_OBJECT)
                throw IllegalManifestException("broken manifest file", 1)

            var version: String? = null
            var name: String? = null
            var id: String? = null
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME)
                    throw IllegalManifestException("broken manifest file", 1)
                val field = parser.text
                parser.nextToken()

                if (FIELD_FORMAT_VERSION.equals(field, ignoreCase = true)) {
                    if (parser.intValue > FORMAT_VERSION)
                        throw IllegalManifestException("unknown version of format", 2)
                    continue
                }

                if (FIELD_VERSION.equals(field, ignoreCase = true)) {
                    version = parser.text.trim { it <= ' ' }
                    continue
                }

                if (FIELD_NAME.equals(field, ignoreCase = true)) {
                    name = parser.text.trim { it <= ' ' }
                    continue
                }

                if (FIELD_ID.equals(field, ignoreCase = true)) {
                    id = parser.text.trim { it <= ' ' }
                    continue
                }

                if (parser.currentToken != JsonToken.START_OBJECT && parser.currentToken != JsonToken.START_ARRAY) {
                    parser.nextValue()
                } else {
                    parser.skipChildren()
                }
            }

            if (version == null || name == null || id == null)
                throw IllegalManifestException("broken manifest file", 1)

            return ThemeManifest(version, name, id)
        }
    }
}
