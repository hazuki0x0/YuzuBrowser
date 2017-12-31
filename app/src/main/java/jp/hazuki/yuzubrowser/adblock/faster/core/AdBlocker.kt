/*
 * Copyright (C) 2017 Hazuki
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
import android.util.LruCache
import jp.hazuki.yuzubrowser.adblock.faster.findAll
import jp.hazuki.yuzubrowser.adblock.faster.utils.isThirdParty
import jp.hazuki.yuzubrowser.utils.extensions.set
import jp.hazuki.yuzubrowser.utils.fastmatch.FastMatcherList
import java.util.regex.Pattern

class AdBlocker(
        private val blackList: FilterMatcher,
        private val whiteList: FilterMatcher,
        private val whitePageList: FastMatcherList) {

    private val resultCache = LruCache<String, Boolean>(1000)
    private val candidatesCreator = Pattern.compile("[a-z0-9%]{3,}", Pattern.CASE_INSENSITIVE)

    fun clear() {
        blackList.clear()
        whiteList.clear()
    }

    private fun isBlock(pageUrl: Uri, requestUri: Uri, isThirdParty: Boolean): Boolean {
        val candidates = candidatesCreator.matcher(requestUri.toString()).findAll()
        candidates.add("")

        candidates.forEach {
            if (whiteList.match(it, pageUrl, requestUri, isThirdParty)) return false
        }

        candidates.forEach {
            if (blackList.match(it, pageUrl, requestUri, isThirdParty)) return true
        }

        return false
    }

    fun isBlock(pageUrl: Uri, requestUri: Uri): Boolean {
        if (whitePageList.match(pageUrl)) return false

        val isThird = isThirdParty(pageUrl, requestUri)
        val cacheKey = pageUrl.toString() + requestUri.toString() + isThird
        val cache = resultCache[cacheKey]

        if (cache != null) return cache

        val result = isBlock(pageUrl, requestUri, isThird)

        resultCache[cacheKey] = result

        return result
    }
}