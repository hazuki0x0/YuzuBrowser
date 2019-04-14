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

package jp.hazuki.yuzubrowser.adblock.core

import android.net.Uri
import jp.hazuki.yuzubrowser.adblock.filter.ArrayFilter
import jp.hazuki.yuzubrowser.adblock.filter.Filter
import jp.hazuki.yuzubrowser.adblock.filter.SingleFilter
import java.util.regex.Pattern

class FilterMatcher(iterator: Iterator<SingleFilter>) {
    private val filtersByKey = HashMap<String, Filter>()
    private val keyByFilter = HashMap<String, String>()

    init {
        iterator.forEach(::add)
    }

    fun clear() {
        filtersByKey.clear()
        keyByFilter.clear()
    }

    fun addAll(filters: List<SingleFilter>) {
        filters.forEach { add(it) }
    }

    fun add(filter: SingleFilter) {
        if (keyByFilter.containsKey(filter.pattern)) {
            return
        }

        val keyword = findKey(filter)
        val entry = filtersByKey[keyword]
        when {
            entry is ArrayFilter -> entry.add(filter)
            entry != null -> filtersByKey[keyword] = ArrayFilter().apply {
                add(entry)
                add(filter)
            }
            else -> filtersByKey[keyword] = filter
        }

        keyByFilter[filter.pattern] = keyword
    }

    fun remove(filter: SingleFilter) {
        val key = keyByFilter[filter.pattern] ?: return

        val list = filtersByKey[key]!!
        if (list is ArrayFilter) {
            list.remove(filter)
        } else {
            filtersByKey.remove(key)
        }

        keyByFilter.remove(filter.pattern)
    }

    private fun findKey(filter: SingleFilter): String {
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

    fun hasFilter(filter: SingleFilter) = keyByFilter.containsKey(filter.pattern)

    fun getKeyword(filter: SingleFilter) = keyByFilter[filter.pattern]

    fun getFilter(key: String) = filtersByKey[key]

    fun match(key: String, uri: Uri, pageUrl: Uri, contentType: Int, isThirdParty: Boolean): Boolean {
        return filtersByKey[key]?.match(uri, pageUrl, contentType, isThirdParty) ?: false
    }

    fun find(key: String, uri: Uri, pageUrl: Uri, contentType: Int, isThirdParty: Boolean): Filter? {
        return filtersByKey[key]?.find(uri, pageUrl, contentType, isThirdParty)
    }

    fun getFastMatchFilters(): Iterator<Filter> {
        return object : Iterator<Filter> {
            private val it = filtersByKey.entries.iterator()
            private var filter: Iterator<Filter>? = null
            private var next: Filter? = null

            override fun hasNext(): Boolean {
                if (next != null) return true

                do {
                    val result = ensureNext()
                } while (result && next == null)

                return next != null
            }

            override fun next(): Filter {
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
                val filter = this.filter
                if (filter != null) {
                    if (filter.hasNext()) {
                        next = filter.next()
                    } else {
                        this.filter = null
                    }
                    return true
                }

                if (it.hasNext()) {
                    val data = it.next().value
                    if (data is ArrayFilter) {
                        this.filter = data.iterator()
                    } else {
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