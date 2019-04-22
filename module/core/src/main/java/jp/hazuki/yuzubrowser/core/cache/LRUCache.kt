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

package jp.hazuki.yuzubrowser.core.cache

class LRUCache<K : Any, V : Any>(
    var cacheSize: Int,
    private val listener: OnCacheOverFlowListener<V>? = null
) : LinkedHashMap<K, V>(cacheSize, 0.75f, true) {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>): Boolean {
        val result = size > cacheSize
        if (result && listener != null) {
            listener.onCacheOverflow(eldest.value)
        }
        return result
    }

    interface OnCacheOverFlowListener<V> {
        fun onCacheOverflow(tabData: V)
    }
}
