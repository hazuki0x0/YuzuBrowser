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

import jp.hazuki.yuzubrowser.adblock.filter.unified.SingleDomainMap
import jp.hazuki.yuzubrowser.adblock.filter.unified.element.ElementFilter
import jp.hazuki.yuzubrowser.adblock.filter.unified.element.ElementHideFilter
import jp.hazuki.yuzubrowser.adblock.filter.unified.element.ExcludeElementFilter
import jp.hazuki.yuzubrowser.core.cache.LRUCache
import kotlin.math.min

class ElementBlocker {
    private val defaultDomain = SingleDomainMap(true, "")

    private val excludeFilters = mutableMapOf<String, MutableList<String>>()

    private val filters = mutableMapOf<String, MutableList<FilterSet>>()

    private var defaultStyleSheet: String? = null

    private var styleSheetCache = LRUCache<String, String>(32)

    private fun addFilter(contentHideFilter: ElementHideFilter) {
        clearCache()

        val domains = contentHideFilter.domains ?: defaultDomain
        for (i in 0 until domains.size) {
            val domain = domains.getKey(i)
            val include = domains.getValue(i)

            if (!include && domain.isEmpty()) continue
            val filterSet = filters.getOrPut(domain) { ArrayList(1) }
            filterSet.add(FilterSet(contentHideFilter, include))
        }
    }

    private fun addExcludeFilters(excludeContentFilter: ExcludeElementFilter) {
        clearCache()

        val item = excludeFilters.getOrPut(excludeContentFilter.selector) { mutableListOf() }
        item.addAll(excludeContentFilter.domains)
    }

    fun addAll(elementFilterList: List<ElementFilter>) {
        elementFilterList.forEach {
            when (it) {
                is ElementHideFilter -> addFilter(it)
                is ExcludeElementFilter -> addExcludeFilters(it)
            }
        }
    }

    fun getStyleSheet(host: String, excludeUniversal: Boolean): String {
        return styleSheetCache.getOrPut("$host;$excludeUniversal") {
            val created = createStyleSheet(host)
            if (excludeUniversal) created else created + getDefaultStyleSheet()
        }
    }

    private fun createStyleSheet(host: String): String {
        val filters = getTargetFilters(host)
        if (filters.isEmpty()) return ""

        val builder = StringBuilder()
        var start = 0
        while (start < filters.size) {
            for (i in start until min(STYLE_SELECTOR_LIMIT + start, filters.size)) {
                if (i > 0) builder.append(", ")
                builder.append(filters[i].selector)
            }
            start += STYLE_SELECTOR_LIMIT
        }
        builder.append(" {display: none !important;}\n")
        return builder.toString()
    }

    private fun getTargetFilters(host: String): List<ElementHideFilter> {
        val exclude = mutableSetOf<ElementHideFilter>()
        val include = mutableListOf<ElementHideFilter>()
        var index = 0
        do {
            val domain = host.substring(index)
            filters[domain]?.let { search ->
                search.forEach {
                    if (it.include) {
                        exclude.add(it.filter)
                    } else {
                        include.add(it.filter)
                    }
                }
            }
            index = host.indexOf('.', index + 1) + 1
        } while (0 < index)
        return include.filterNot { exclude.contains(it) || isExcludeFilter(host, it) }
    }

    private fun isExcludeFilter(host: String, filter: ElementHideFilter): Boolean {
        val blockedDomains = excludeFilters[filter.selector] ?: return false
        blockedDomains.forEach {
            if (host.contains(it)) return true
        }
        return false
    }

    private fun getDefaultStyleSheet(): String {
        val def = defaultStyleSheet
        return if (def == null) {
            val ss = createStyleSheet("")
            defaultStyleSheet = ss
            ss
        } else {
            def
        }
    }

    private fun clearCache() {
        defaultStyleSheet = ""
        styleSheetCache.clear()
    }

    private class FilterSet(val filter: ElementHideFilter, val include: Boolean)

    companion object {
        private const val STYLE_SELECTOR_LIMIT = 8192
    }
}
