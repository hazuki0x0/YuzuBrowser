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

import android.content.Context
import com.google.re2j.Pattern
import jp.hazuki.yuzubrowser.adblock.filter.*
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpEntity
import jp.hazuki.yuzubrowser.core.utility.extensions.replace
import jp.hazuki.yuzubrowser.core.utility.extensions.replaceEnd
import jp.hazuki.yuzubrowser.core.utility.extensions.replaceStart
import java.io.File
import java.io.InputStream
import java.io.OutputStream

internal const val ABP_TYPE_CONTAINS = 1
internal const val ABP_TYPE_START = 2
internal const val ABP_TYPE_END = 3
internal const val ABP_TYPE_START_END = 4
internal const val ABP_TYPE_RE2_REGEX = 5
internal const val ABP_TYPE_JVM_REGEX = 6
internal const val ABP_TYPE_RE2_SSP_REGEX = 7
internal const val ABP_TYPE_JVM_SSP_REGEX = 8

internal const val ABP_DIR = "adblock_abp"
internal const val ABP_PREFIX_BLACK = "b_"
internal const val ABP_PREFIX_WHITE = "w_"
internal const val ABP_PREFIX_WHITE_PAGE = "wp_"
internal const val ABP_CACHE_HEADER = "YZBABPFI\u0000\u0001\u0000"

internal fun OutputStream.writeVariableInt(num: Int, shortBuf: ByteArray, intBuf: ByteArray) {
    if (shortBuf.size != 2) throw LengthException()
    if (intBuf.size != 4) throw LengthException()

    if (num < 0xffff) {
        write(num.toShortByteArray(shortBuf))
    } else {
        write(0xff)
        write(0xff)
        write(num.toByteArray(intBuf))
    }
}

internal fun InputStream.readVariableInt(shortBuf: ByteArray, intBuf: ByteArray): Int {
    if (shortBuf.size != 2) throw LengthException()
    if (intBuf.size != 4) throw LengthException()

    if (read(shortBuf) != 2) return -1
    var result = shortBuf.toShortInt()
    if (result == 0xffff) {
        if (read(intBuf) != 4) return -1
        result = intBuf.toInt()
    }
    return result
}

private val REPLACE_WILDCARDS = Pattern.compile("""\*+""")

internal fun String.convertToRegexText(): String {
    val text = REPLACE_WILDCARDS.matcher(this).replaceAll("*")

    if (text == "*") return "*"
    val builder = StringBuilder(text)
    if (builder[0] == '*') builder.deleteCharAt(0)
    if (builder[builder.lastIndex] == '*') builder.deleteCharAt(builder.length - 1)

    return builder.replaceEnd("^|", "^")
            .escapeRegexSymbols()
            .replace("*", ".*")
            .replace("^", "(?:[\\x00-\\x24\\x26-\\x2C\\x2F\\x3A-\\x40\\x5B-\\x5E\\x60\\x7B-\\x7F]|$)")
            .replaceStart("\\|", "^")
            .replaceEnd("\\|", "$")
            .toString()
}

internal fun Context.getAbpDir(): File {
    return getDir(ABP_DIR, Context.MODE_PRIVATE)
}

internal fun File.getAbpBlackListFile(entity: AbpEntity): File {
    return File(this, ABP_PREFIX_BLACK + entity.entityId)
}

internal fun File.getAbpWhiteListFile(entity: AbpEntity): File {
    return File(this, ABP_PREFIX_WHITE + entity.entityId)
}

internal fun File.getAbpWhitePageListFile(entity: AbpEntity): File {
    return File(this, ABP_PREFIX_WHITE_PAGE + entity.entityId)
}

fun List<AbpEntity>.checkNeedUpdate(): Boolean {
    val now = System.currentTimeMillis()
    forEach {
        if (it.isNeedUpdate()) return true
        if (now - it.lastLocalUpdate >= (if (it.expires > 0) it.expires * AN_HOUR else A_DAY)) {
            return true
        }
    }
    return false
}

fun AbpEntity.isNeedUpdate(): Boolean {
    val now = System.currentTimeMillis()
    if (now - lastLocalUpdate >= (if (expires > 0) expires * AN_HOUR else A_DAY)) {
        return true
    }
    return false
}

private const val AN_HOUR = 60 * 60 * 1000

private const val A_DAY = 24 * AN_HOUR
