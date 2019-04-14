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

package jp.hazuki.yuzubrowser.adblock.filter.fastmatch

import android.content.Context
import jp.hazuki.yuzubrowser.adblock.filter.Filter
import jp.hazuki.yuzubrowser.adblock.filter.fastmatch.regex.LazyRegexHost
import jp.hazuki.yuzubrowser.adblock.filter.fastmatch.regex.LazyRegexUrl
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import java.io.*

private const val FOLDER = "fastMatcher"
private const val CACHE_HEADER = "\u008aYF\u00d8CACHE\u00d82"

class FastMatcherCache(context: Context, fileName: String) {
    private val path = File(context.cacheDir, "$FOLDER/$fileName")

    fun getLastTime(): Long {
        if (!path.exists()) return -1

        try {
            BufferedInputStream(FileInputStream(path)).use {
                val header = CACHE_HEADER.toByteArray()
                val data = ByteArray(header.size)
                it.read(data)
                if (header contentEquals data) {
                    val time = ByteArray(8)
                    it.read(time)
                    return time.toLong()
                }
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }
        return -1
    }

    fun load(): FastMatcherList {
        if (!path.exists()) return FastMatcherList()
        var dbTime = -1L
        val list = arrayListOf<FastMatcher>()

        try {
            BufferedInputStream(FileInputStream(path)).use {
                val header = CACHE_HEADER.toByteArray()
                val data = ByteArray(header.size)
                it.read(data)
                if (header contentEquals data) {
                    val longArray = ByteArray(8)
                    val intArray = ByteArray(4)
                    val shortArray = ByteArray(2)
                    var patternBuffer = ByteArray(32)

                    if (it.read(longArray) != 8) return FastMatcherList()
                    dbTime = longArray.toLong()

                    loop@ while (true) {
                        val type = it.read()
                        if (type < 0) break

                        if (it.read(intArray) != 4) break
                        val id = intArray.toInt()

                        if (it.read(intArray) != 4) break
                        val count = intArray.toInt()

                        if (it.read(longArray) != 8) break
                        val time = longArray.toLong()

                        if (it.read(shortArray) != 2) break
                        var patternLength = shortArray.toShortInt()
                        if (patternLength == 0xffff) {
                            if (it.read(intArray) != 4) break
                            patternLength = intArray.toInt()
                        }
                        if (patternBuffer.size < patternLength) {
                            patternBuffer = ByteArray(patternLength)
                        }
                        if (it.read(patternBuffer, 0, patternLength) != patternLength) break
                        val pattern = String(patternBuffer, 0, patternLength)

                        val matcher = when (type) {
                            1 -> SimpleHost(pattern)
                            2 -> SimpleUrl(pattern)
                            3 -> LazyRegexHost(pattern)
                            4 -> LazyRegexUrl(pattern)
                            5 -> ContainsHost(pattern)
                            else -> break@loop
                        }
                        matcher.id = id
                        matcher.count = count
                        matcher.time = time
                        list.add(matcher)
                    }
                }
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }

        return FastMatcherList(list, dbTime)
    }
}

fun FastMatcherList.save(context: Context, fileName: String) {
    val path = File(context.cacheDir, "$FOLDER/$fileName")
    if (!path.parentFile.exists()) {
        if (!path.parentFile.mkdirs()) return
    }

    try {
        BufferedOutputStream(FileOutputStream(path)).use { os ->
            os.write(CACHE_HEADER.toByteArray())
            os.write(dbTime.toByteArray())

            forEach { matcher ->
                os.write(matcher.type and 0xff)
                os.write(matcher.id.toByteArray())
                os.write(matcher.frequency.toByteArray())
                os.write(matcher.time.toByteArray())

                val text = matcher.pattern.toByteArray()
                val size = text.size
                if (size > 0xffff) {
                    os.write(0xff)
                    os.write(0xff)
                    os.write(size.toByteArray())
                } else {
                    os.write(size.toShortByteArray())
                }
                os.write(text)
            }
        }
    } catch (e: IOException) {
        ErrorReport.printAndWriteLog(e)
    }
}

fun save(context: Context, fileName: String, iterator: Iterator<Filter>) {
    val path = File(context.cacheDir, "$FOLDER/$fileName")
    if (!path.parentFile.exists()) {
        if (!path.parentFile.mkdirs()) return
    }

    try {
        BufferedOutputStream(FileOutputStream(path)).use { os ->
            os.write(CACHE_HEADER.toByteArray())
            os.write(System.currentTimeMillis().toByteArray())

            iterator.asSequence()
                    .filterIsInstance<FastMatcher>()
                    .forEach { matcher ->
                        os.write(matcher.type and 0xff)
                        os.write(matcher.id.toByteArray())
                        os.write(matcher.frequency.toByteArray())
                        os.write(matcher.time.toByteArray())

                        val text = matcher.pattern.toByteArray()
                        val size = text.size
                        if (size > 0xffff) {
                            os.write(0xff)
                            os.write(0xff)
                            os.write(size.toByteArray())
                        } else {
                            os.write(size.toShortByteArray())
                        }
                        os.write(text)
                    }
        }
    } catch (e: IOException) {
        ErrorReport.printAndWriteLog(e)
    }
}

fun needSave(context: Context, fileName: String): Boolean {
    return !File(context.cacheDir, "$FOLDER/$fileName").exists()
}

private fun Long.toByteArray(): ByteArray {
    val bytes = ByteArray(8)
    bytes[0] = and(0xff).toByte()
    bytes[1] = ushr(0x08).and(0xff).toByte()
    bytes[2] = ushr(0x10).and(0xff).toByte()
    bytes[3] = ushr(0x18).and(0xff).toByte()
    bytes[4] = ushr(0x20).and(0xff).toByte()
    bytes[5] = ushr(0x28).and(0xff).toByte()
    bytes[6] = ushr(0x30).and(0xff).toByte()
    bytes[7] = ushr(0x38).and(0xff).toByte()
    return bytes
}

private fun ByteArray.toLong(): Long {
    if (size != 8) throw LengthException()

    return this[0].toLong().and(0xff) or
            this[1].toLong().and(0xff).shl(0x08) or
            this[2].toLong().and(0xff).shl(0x10) or
            this[3].toLong().and(0xff).shl(0x18) or
            this[4].toLong().and(0xff).shl(0x20) or
            this[5].toLong().and(0xff).shl(0x28) or
            this[6].toLong().and(0xff).shl(0x30) or
            this[7].toLong().and(0xff).shl(0x38)
}

private fun Int.toByteArray(): ByteArray {
    val bytes = ByteArray(4)
    bytes[0] = and(0xff).toByte()
    bytes[1] = ushr(0x08).and(0xff).toByte()
    bytes[2] = ushr(0x10).and(0xff).toByte()
    bytes[3] = ushr(0x18).and(0xff).toByte()
    return bytes
}

private fun ByteArray.toInt(): Int {
    if (size != 4) throw LengthException()

    return this[0].toInt().and(0xff) or
            this[1].toInt().and(0xff).shl(0x08) or
            this[2].toInt().and(0xff).shl(0x10) or
            this[3].toInt().and(0xff).shl(0x18)
}

private fun Int.toShortByteArray(): ByteArray {
    val bytes = ByteArray(2)
    bytes[0] = and(0xff).toByte()
    bytes[1] = ushr(0x08).and(0xff).toByte()
    return bytes
}

private fun ByteArray.toShortInt(): Int {
    if (size != 2) throw LengthException()

    return this[0].toInt().and(0xff) or
            this[1].toInt().and(0xff).shl(0x08)
}

private class LengthException : IOException()