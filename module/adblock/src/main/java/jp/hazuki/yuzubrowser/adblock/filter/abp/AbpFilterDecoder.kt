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

import com.google.re2j.Pattern
import jp.hazuki.yuzubrowser.adblock.*
import jp.hazuki.yuzubrowser.core.utility.extensions.forEachLine
import java.io.BufferedReader
import java.io.IOException
import java.nio.charset.Charset
import com.google.re2j.PatternSyntaxException as Re2PatternSyntaxException
import java.util.regex.PatternSyntaxException as JvmPatternSyntaxException

class AbpFilterDecoder {
    private val contentRegex = Pattern.compile(CONTENT_FILTER_REGEX)

    fun checkHeader(reader: BufferedReader, charset: Charset): Boolean {
        reader.mark(1024)
        when (charset) {
            Charsets.UTF_8, Charsets.UTF_16, Charsets.UTF_16LE, Charsets.UTF_16BE -> {
                if (reader.read() == 0xfeff) { // Skip BOM
                    reader.mark(1024)
                } else {
                    reader.reset()
                }
            }
        }
        val header = reader.readLine() ?: return false
        if (header.isNotEmpty()) {
            return if (header[0] == '!') {
                reader.reset()
                true
            } else {
                header.startsWith(HEADER)
            }
        }
        return false
    }

    @Throws(OnRedirectException::class)
    fun decode(reader: BufferedReader, url: String?): AbpFilterSet {
        val info = DecoderInfo()
        val black = mutableListOf<AbpFilter>()
        val white = mutableListOf<AbpFilter>()
        val whitePage = mutableListOf<AbpFilter>()
        reader.forEachLine { line ->
            if (line.isEmpty()) return@forEachLine
            when {
                line[0] == '!' -> line.decodeComment(url, info)?.let {
                    throw OnRedirectException(it)
                }
                else -> {
                    if (!contentRegex.matches(line)) {
                        line.decodeFilter(black, white, whitePage)
                    }
                }
            }
        }
        return AbpFilterSet(info, black, white, whitePage)
    }

    private fun String.decodeFilter(blackList: MutableList<AbpFilter>, whiteList: MutableList<AbpFilter>, pageWhiteList: MutableList<AbpFilter>) {
        var contentType = 0
        var ignoreCase = false
        var domain: String? = null
        var thirdParty = -1
        var filter = this
        val blocking = if (filter.startsWith("@@")) {
            filter = substring(2)
            false
        } else {
            true
        }
        val optionsIndex = filter.lastIndexOf('$')
        if (optionsIndex >= 0) {
            filter.substring(optionsIndex + 1).split(',').forEach {
                var option = it
                var value: String? = null
                val separatorIndex = option.indexOf('=')
                if (separatorIndex >= 0) {
                    value = option.substring(separatorIndex + 1)
                    option = option.substring(0, separatorIndex)
                }
                if (option.isEmpty()) return@forEach

                val inverse = option[0] == '~'
                if (inverse) {
                    option = option.substring(1)
                }

                option = option.toLowerCase()
                val type = option.getOptionBit()
                if (type == -1) return@forEach

                if (type > 0) {
                    contentType = if (inverse) {
                        if (contentType == 0) contentType = 0xffff
                        contentType and (type.inv())
                    } else {
                        contentType or type
                    }
                } else {
                    when (option) {
                        "match-case" -> ignoreCase = inverse
                        "domain" -> {
                            if (value == null) return
                            domain = value
                        }
                        "third-party" -> thirdParty = if (inverse) 0 else 1
                        "sitekey" -> Unit
                        else -> return
                    }
                }
            }
            filter = filter.substring(0, optionsIndex)
        }

        val domains = domain?.let {
            if (it.isEmpty()) return@let null
            val items = it.split('|')
            if (items.size == 1) {
                if (items[0][0] == '~') {
                    SingleDomainMap(true, items[0].substring(1))
                } else {
                    SingleDomainMap(false, items[0])
                }
            } else {
                val domains = ArrayDomainMap(items.size)
                items.forEach { domain ->
                    if (domain.isEmpty()) return@forEach
                    if (domain[0] == '~') {
                        domains[domain.substring(1)] = false
                    } else {
                        domains[domain] = true
                        domains.include = false
                    }
                }
                domains
            }

        }
        if (contentType == 0) contentType = 0xffff

        val abpFilter = if (filter.length >= 2 && filter[0] == '/' && filter[filter.lastIndex] == '/') {
            createRegexFilter(filter, contentType, ignoreCase, domains, thirdParty) ?: return
        } else {
            val isStartsWith = filter.startsWith("||")
            val isEndWith = filter.endsWith('^')
            val content = filter.substring(if (isStartsWith) 2 else 0, if (isEndWith) filter.length - 1 else filter.length)
            val isLiteral = content.isLiteralFilter()
            if (isLiteral) {
                when {
                    isStartsWith && isEndWith -> AbpStartEndFilter(content, contentType, ignoreCase, domains, thirdParty)
                    isStartsWith -> AbpStartsWithFilter(content, contentType, ignoreCase, domains, thirdParty)
                    isEndWith -> AbpEndWithFilter(content, contentType, ignoreCase, domains, thirdParty)
                    else -> AbpContainsFilter(content, contentType, ignoreCase, domains, thirdParty)
                }
            } else {
                if (isStartsWith) {
                    createSspRegexFilter(filter, contentType, ignoreCase, domains, thirdParty)
                            ?: return
                } else {
                    createRegexFilter(filter, contentType, ignoreCase, domains, thirdParty)
                            ?: return
                }
            }
        }

        if (blocking) {
            blackList.add(abpFilter)
        } else {
            if (contentType.and(AD_BLOCK_DOCUMENT) > 0) {
                pageWhiteList.add(abpFilter)
            } else {
                whiteList.add(abpFilter)
            }
        }
    }

    private fun String.isLiteralFilter(): Boolean {
        forEach {
            when (it) {
                '*', '^', '|' -> return false
            }
        }
        return true
    }

    private fun String.getOptionBit(): Int {
        return when (this) {
            "other", "xbl", "dtd" -> AD_BLOCK_OTHER
            "script" -> AD_BLOCK_SCRIPT
            "image", "background" -> AD_BLOCK_IMAGE
            "stylesheet" -> AD_BLOCK_STYLE_SHEET
            "subdocument" -> AD_BLOCK_SUB_DOCUMENT
            "document" -> AD_BLOCK_DOCUMENT
            "websocket" -> AD_BLOCK_WEBSOCKET
            "media" -> AD_BLOCK_MEDIA
            "font" -> AD_BLOCK_FONT
            "popup" -> AD_BLOCK_POPUP
            "object", "webrtc", "csp", "ping", "xmlhttprequest",
            "object-subrequest", "genericblock", "elemhide", "generichide" -> -1
            else -> 0
        }
    }

    private fun String.decodeComment(url: String?, info: DecoderInfo): String? {
        val comment = split(':')
        if (comment.size < 2) return null

        when (comment[0].substring(1).trim().toLowerCase()) {
            "title" -> info.title = comment[1].trim()
            "homepage" -> info.homePage = comment[1].trim()
            "last updated" -> info.lastUpdate = comment[1].trim()
            "expires" -> info.expires = comment[1].trim().decodeExpires()
            "version" -> info.version = comment[1].trim()
            "redirect" -> {
                val redirect = comment[1].trim()
                if (url != null && url != redirect) {
                    return url
                }
            }
        }
        return null
    }

    private fun String.decodeExpires(): Int {
        val hours = indexOf("hours")
        if (hours > 0) {
            return try {
                substring(0, hours).trim().toInt()
            } catch (e: NumberFormatException) {
                -1
            }
        }
        val days = indexOf("days")
        if (days > 0) {
            return try {
                substring(0, days).trim().toInt() * 24
            } catch (e: NumberFormatException) {
                -1
            }
        }
        return -1
    }

    private fun createSspRegexFilter(filter: String, contentType: Int, ignoreCase: Boolean, domains: DomainMap?, thirdParty: Int): AbpFilter? {
        val pattern = filter.substring(2).convertToRegexText()
        val escaped = "//$pattern"
        try {
            AbpRe2SspFilter(Pattern.compile(escaped), pattern, contentType, ignoreCase, domains, thirdParty)
        } catch (e: Re2PatternSyntaxException) {
            try {
                AbpRegexSspFilter(escaped.toRegex(), pattern, contentType, ignoreCase, domains, thirdParty)
            } catch (e: JvmPatternSyntaxException) {
            }
        }
        return null
    }

    private fun createRegexFilter(filter: String, contentType: Int, ignoreCase: Boolean, domains: DomainMap?, thirdParty: Int): AbpFilter? {
        val pattern = filter.convertToRegexText()
        try {
            AbpRe2Filter(Pattern.compile(pattern), pattern, contentType, ignoreCase, domains, thirdParty)
        } catch (e: Re2PatternSyntaxException) {
            try {
                AbpRegexFilter(pattern.toRegex(), pattern, contentType, ignoreCase, domains, thirdParty)
            } catch (e: JvmPatternSyntaxException) {
            }
        }
        return null
    }

    class OnRedirectException(val url: String) : IOException()

    private class DecoderInfo : AbpFilterInfo(null, null, null, null, null) {
        override var expires: Int? = null
        override var homePage: String? = null
        override var lastUpdate: String? = null
        override var title: String? = null
        override var version: String? = null
    }

    companion object {
        const val HEADER = "[Adblock Plus"

        private const val CONTENT_FILTER_REGEX = "^([^/*|@\"!]*?)#([@?\$])?#(.+)\$"
    }
}
