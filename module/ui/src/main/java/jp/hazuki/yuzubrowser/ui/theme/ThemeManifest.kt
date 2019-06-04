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

package jp.hazuki.yuzubrowser.ui.theme

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import okio.buffer
import okio.source
import java.io.File
import java.io.IOException

class ThemeManifest @Throws(IllegalManifestException::class, IOException::class)
private constructor(val version: String, val name: String, val id: String) {

    class IllegalManifestException internal constructor(message: String, val errorType: Int) : Exception(message)

    companion object {
        const val MANIFEST = "manifest.json"

        private const val FORMAT_VERSION = 1

        private const val FIELD_FORMAT_VERSION = "format_version"
        private const val FIELD_VERSION = "version"
        private const val FIELD_NAME = "name"
        private const val FIELD_ID = "id"

        fun getManifest(themeFolder: File): ThemeManifest? {
            try {
                return decodeManifest(File(themeFolder, MANIFEST))
            } catch (e: IllegalManifestException) {
                e.printStackTrace()
            }

            return null
        }

        @Throws(IllegalManifestException::class)
        fun decodeManifest(manifestFile: File): ThemeManifest? {
            if (manifestFile.exists() && manifestFile.isFile) {
                try {
                    JsonReader.of(manifestFile.source().buffer()).use { return decode(it) }
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw IllegalManifestException("unknown error", 0)
                }

            } else {
                return null
            }
        }

        @Throws(IllegalManifestException::class, IOException::class)
        private fun decode(reader: JsonReader): ThemeManifest {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT)
                throw IllegalManifestException("broken manifest file", 1)
            reader.beginObject()

            var version: String? = null
            var name: String? = null
            var id: String? = null
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME)
                    throw IllegalManifestException("broken manifest file", 1)

                val field = reader.nextName()
                if (FIELD_FORMAT_VERSION.equals(field, ignoreCase = true)) {
                    try {
                        if (reader.nextInt() > FORMAT_VERSION)
                            throw IllegalManifestException("unknown version of format", 2)
                    } catch (e: JsonDataException) {
                        throw IllegalManifestException("broken manifest file", 1)
                    }
                    continue
                }

                if (FIELD_VERSION.equals(field, ignoreCase = true)) {
                    try {
                        version = reader.nextString().trim { it <= ' ' }
                    } catch (e: JsonDataException) {
                        throw IllegalManifestException("broken manifest file", 1)
                    }
                    continue
                }

                if (FIELD_NAME.equals(field, ignoreCase = true)) {
                    try {
                        name = reader.nextString().trim { it <= ' ' }
                    } catch (e: JsonDataException) {
                        throw IllegalManifestException("broken manifest file", 1)
                    }
                    continue
                }

                if (FIELD_ID.equals(field, ignoreCase = true)) {
                    try {
                        id = reader.nextString().trim { it <= ' ' }
                    } catch (e: JsonDataException) {
                        throw IllegalManifestException("broken manifest file", 1)
                    }
                    continue
                }
                reader.skipValue()
            }
            reader.endObject()

            if (version == null || name == null || id == null)
                throw IllegalManifestException("broken manifest file", 1)

            return ThemeManifest(version, name, id)
        }
    }
}
