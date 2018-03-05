/*
 * Copyright (C) 2017-2018 Hazuki
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

package jp.hazuki.yuzubrowser.adblock.faster.core

import android.net.Uri
import jp.hazuki.yuzubrowser.adblock.faster.BaseFilter
import jp.hazuki.yuzubrowser.adblock.faster.Filter
import jp.hazuki.yuzubrowser.adblock.faster.ListFilter
import jp.hazuki.yuzubrowser.adblock.faster.findAll
import jp.hazuki.yuzubrowser.utils.fastmatch.FastMatcher
import java.util.regex.Pattern

class FilterMatcher(iterator: Iterator<Filter>) {
    private val filtersByKey = HashMap<String, BaseFilter>()
    private val keyByFilter = HashMap<String, String>()

    init {
        iterator.forEach(::add)
    }

    fun clear() {
        filtersByKey.clear()
        keyByFilter.clear()
    }

    fun add(filter: Filter) {
        if (keyByFilter.containsKey(filter.pattern)) {
            return
        }

        val keyword = findKey(filter)
        val entry = filtersByKey[keyword]
        if (entry != null) {
            if (entry is ListFilter) {
                entry.add(filter)
            } else {
                filtersByKey[keyword] = ListFilter(entry as Filter, filter)
            }
        } else {
            filtersByKey[keyword] = filter
        }

        keyByFilter[filter.pattern] = keyword
    }

    fun remove(filter: Filter) {
        val key = keyByFilter[filter.pattern] ?: return

        val list = filtersByKey[key]!!
        if (list is ListFilter && list.size > 1) {
            list.remove(filter)
        } else {
            filtersByKey.remove(key)
        }

        keyByFilter.remove(filter.pattern)
    }

    private fun findKey(filter: Filter): String {
        var text = filter.pattern
        if (regexMatcher.matcher(text).find()) {
            return ""
        }

        val matcher = optionMatcher.matcher(text)
        if (matcher.find()) {
            val result = matcher.toMatchResult()
            text = text.substring(0, result.start())
        }

        if (text.startsWith("@@")) {
            text = text.substring(2)
        }

        val candidates = keyWordDecodeMatcher.matcher(text.toLowerCase()).findAll()

        if (candidates.isEmpty()) {
            return ""
        }

        var resultLength = 0
        var resultCount = 0xFFFFFF
        var result = ""
        for (it in candidates) {
            val candidate = it.substring(1)
            val count = filtersByKey[candidate]?.size ?: 0

            if (count < resultCount || (count == resultCount && candidate.length > resultLength)) {
                result = candidate
                resultCount = count
                resultLength = candidate.length
            }
        }

        return result
    }

    fun hasFilter(filter: Filter) = keyByFilter.containsKey(filter.pattern)

    fun getKeyword(filter: Filter) = keyByFilter[filter.pattern]

    fun getFilter(key: String) = filtersByKey[key]

    fun match(key: String, pageUrl: Uri, requestUri: Uri, isThirdParty: Boolean): Boolean {
        return filtersByKey[key]?.match(key, pageUrl, requestUri, isThirdParty) ?: false
    }

    fun find(key: String, pageUrl: Uri, requestUri: Uri, isThirdParty: Boolean): Filter? {
        return filtersByKey[key]?.find(key, pageUrl, requestUri, isThirdParty)
    }

    fun getFastMatchFilters(): Iterator<FastMatcher> {
        return object : Iterator<FastMatcher> {
            private val it = filtersByKey.entries.iterator()
            private var filterList: Iterator<Filter>? = null
            private var next: FastMatcher? = null

            override fun hasNext(): Boolean {
                do {
                    val result = ensureNext()
                } while (result && next == null)

                return next != null
            }

            override fun next(): FastMatcher {
                if (next == null) {
                    if (!hasNext()) {
                        throw IllegalStateException("No item")
                    }
                }

                val data = next!!
                next = null

                return data
            }

            private fun ensureNext(): Boolean {
                val list = filterList
                if (list != null) {
                    if (list.hasNext()) {
                        val data = list.next()
                        if (data is FastMatcher) {
                            next = data
                        }
                        if (!list.hasNext()) {
                            filterList = null
                        }
                    }
                    return true
                }

                if (it.hasNext()) {
                    val data = it.next().value
                    if (data is ListFilter) {
                        filterList = data.iterator()
                    } else if (data is FastMatcher) {
                        next = data
                    }
                    return true
                }

                return false
            }
        }
    }

    companion object {
        private val regexMatcher = Pattern.compile("""^(@@)?/.*/(?:\$~?[\w-]+(?:=[^,\s]+)?(?:,~?[\w-]+(?:=[^,\s]+)?)*)?$""")
        private val optionMatcher = Pattern.compile("""\$(~?[\w-]+(?:=[^,\s]+)?(?:,~?[\w-]+(?:=[^,\s]+)?)*)$""")
        private val keyWordDecodeMatcher = Pattern.compile("""[^a-z0-9%*][a-z0-9%]{3,}(?=[^a-z0-9%*])""", Pattern.CASE_INSENSITIVE)
    }
}