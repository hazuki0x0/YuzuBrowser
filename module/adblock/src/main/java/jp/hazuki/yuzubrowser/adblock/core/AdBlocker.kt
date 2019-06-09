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
import jp.hazuki.yuzubrowser.adblock.filter.Filter
import java.util.*
import java.util.regex.Pattern

class AdBlocker(
        val blackList: FilterMatcher,
        val whiteList: FilterMatcher,
        val whitePageList: FilterMatcherList) {

    private var whitePageCache: String? = null
    private var whitePageCacheResult: Boolean = false
    private val resultCache = Collections.synchronizedMap(LruCache<String, Filter?>())
    private val candidatesCreator = Pattern.compile("[a-z0-9%]{3,}", Pattern.CASE_INSENSITIVE)

    fun clear() {
        blackList.clear()
        whiteList.clear()
    }

    private fun isBlockSearch(uri: Uri, pageUrl: Uri, contentType: Int, isThirdParty: Boolean): Filter? {
        val candidates = candidatesCreator.matcher(uri.toString()).findAll()
        candidates.add("")

        for (it in candidates) {
            if (whiteList.match(it, uri, pageUrl, contentType, isThirdParty)) return null
        }

        for (it in candidates) {
            blackList.find(it, uri, pageUrl, contentType, isThirdParty)?.let { return it }
        }

        return null
    }

    fun isWhitePage(pageUrl: Uri, contentType: Int, isThird: Boolean): Boolean {
        val url = pageUrl.toString()
        if (whitePageCache != url) {
            whitePageCache = url
            whitePageCacheResult = whitePageList.match(pageUrl, pageUrl, contentType, isThird)
        }
        return whitePageCacheResult
    }

    fun isBlock(pageUrl: Uri, url: Uri, contentType: Int, isThird: Boolean): Filter? {
        val cacheKey = pageUrl.toString() + url.toString() + isThird
        val cache = resultCache[cacheKey]

        if (cache != null) return cache

        val result = isBlockSearch(url, pageUrl, contentType, isThird)

        resultCache[cacheKey] = result

        return result
    }

    private open class LruCache<K, V> : LinkedHashMap<K, V>(INITIAL_CAPACITY, LOAD_FACTOR, true) {

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > CACHE_SIZE
        }

        companion object {
            private const val serialVersionUID = -1882071901467368406L
            internal const val INITIAL_CAPACITY = 16
            internal const val LOAD_FACTOR = 0.75f
            internal const val CACHE_SIZE = 1024
        }
    }
}
