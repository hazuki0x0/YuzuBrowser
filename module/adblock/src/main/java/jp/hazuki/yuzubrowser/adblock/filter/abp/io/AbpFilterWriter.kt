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

package jp.hazuki.yuzubrowser.adblock.filter.abp.io

import jp.hazuki.yuzubrowser.adblock.filter.abp.ABP_CACHE_HEADER
import jp.hazuki.yuzubrowser.adblock.filter.abp.AbpFilter
import jp.hazuki.yuzubrowser.adblock.filter.abp.writeVariableInt
import jp.hazuki.yuzubrowser.adblock.filter.toByteArray
import jp.hazuki.yuzubrowser.adblock.filter.toShortByteArray
import java.io.OutputStream
import kotlin.math.min

class AbpFilterWriter {
    private val intBuf = ByteArray(4)
    private val shortBuf = ByteArray(2)

    fun write(os: OutputStream, filters: List<AbpFilter>) {
        writeHeader(os)
        writeAll(os, filters)
    }

    private fun writeHeader(os: OutputStream) {
        os.write(ABP_CACHE_HEADER.toByteArray())
    }

    private fun writeAll(os: OutputStream, filters: List<AbpFilter>) {
        os.write(filters.size.toByteArray(intBuf))

        filters.forEach {
            os.write(it.type and 0xff)
            os.write(it.contentType.toShortByteArray(shortBuf))
            os.write(if (it.ignoreCase) 1 else 0)
            os.write(it.thirdParty and 0xff)
            os.writeVariableInt(it.pattern.length, shortBuf, intBuf)
            os.write(it.pattern.toByteArray())
            os.write(it.domains?.size ?: 0)
            it.domains?.let { map ->
                for (i in 0 until min(map.size, 256)) {
                    val key = map.getKey(i)
                    os.writeVariableInt(key.length, shortBuf, intBuf)
                    os.write(key.toByteArray())
                    os.write(if (map.getValue(i)) 1 else 0)
                }
            }
        }
    }
}