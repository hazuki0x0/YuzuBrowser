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

package jp.hazuki.yuzubrowser.adblock.filter.unified.io

import jp.hazuki.yuzubrowser.adblock.filter.toInt
import jp.hazuki.yuzubrowser.adblock.filter.unified.ArrayDomainMap
import jp.hazuki.yuzubrowser.adblock.filter.unified.ELEMENT_FILTER_CACHE_HEADER
import jp.hazuki.yuzubrowser.adblock.filter.unified.SingleDomainMap
import jp.hazuki.yuzubrowser.adblock.filter.unified.element.ElementFilter
import jp.hazuki.yuzubrowser.adblock.filter.unified.element.ElementHideFilter
import jp.hazuki.yuzubrowser.adblock.filter.unified.element.ExcludeElementFilter
import jp.hazuki.yuzubrowser.adblock.filter.unified.readVariableInt
import java.io.InputStream

class ElementReader(private val input: InputStream) {

    fun checkHeader(): Boolean {
        val header = ELEMENT_FILTER_CACHE_HEADER.toByteArray()
        val data = ByteArray(header.size)
        input.read(data)
        return header contentEquals data
    }

    fun readAll(): List<ElementFilter> {
        val intBuf = ByteArray(4)
        val shortBuf = ByteArray(2)
        input.read(intBuf)
        val size = intBuf.toInt()
        val list = ArrayList<ElementFilter>(size)
        var patternBuffer = ByteArray(32)

        loop@ for (loop in 0 until size) {
            val type = input.read()
            if (type < 0) break

            val selectorLength = input.readVariableInt(shortBuf, intBuf)
            if (selectorLength < 0) break
            if (patternBuffer.size < selectorLength) {
                patternBuffer = ByteArray(selectorLength)
            }
            if (input.read(patternBuffer, 0, selectorLength) != selectorLength) break
            val selector = String(patternBuffer, 0, selectorLength)

            val domainsSize = input.read()
            if (domainsSize < 0) break

            when (type) {
                ElementFilter.TYPE_HIDE -> {
                    val domains = when (domainsSize) {
                        0 -> null
                        1 -> {
                            val textSize = input.readVariableInt(shortBuf, intBuf)
                            if (textSize == -1) break@loop
                            if (patternBuffer.size < textSize) {
                                patternBuffer = ByteArray(textSize)
                            }
                            if (input.read(patternBuffer, 0, textSize) != textSize) break@loop
                            val domain = String(patternBuffer, 0, textSize)
                            val include = when (input.read()) {
                                0 -> false
                                1 -> true
                                else -> break@loop
                            }
                            SingleDomainMap(include, domain)
                        }
                        else -> {
                            val map = ArrayDomainMap(domainsSize)
                            for (i in 0 until domainsSize) {
                                val textSize = input.readVariableInt(shortBuf, intBuf)
                                if (textSize == -1) break@loop
                                if (patternBuffer.size < textSize) {
                                    patternBuffer = ByteArray(textSize)
                                }
                                if (input.read(patternBuffer, 0, textSize) != textSize) break@loop
                                val domain = String(patternBuffer, 0, textSize)
                                val include = when (input.read()) {
                                    0 -> false
                                    1 -> true
                                    else -> break@loop
                                }
                                map[domain] = include
                            }
                            map
                        }
                    }
                    list.add(ElementHideFilter(selector, domains))
                }
                ElementFilter.TYPE_EXCLUDE -> {
                    val domains = mutableListOf<String>()
                    for (i in 0 until domainsSize) {
                        val textSize = input.readVariableInt(shortBuf, intBuf)
                        if (textSize == -1) break@loop
                        if (patternBuffer.size < textSize) {
                            patternBuffer = ByteArray(textSize)
                        }
                        if (input.read(patternBuffer, 0, textSize) != textSize) break@loop
                        domains.add(String(patternBuffer, 0, textSize))
                    }
                    list.add(ExcludeElementFilter(selector, domains))
                }
                else -> break@loop
            }
        }
        return list
    }
}
