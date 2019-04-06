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

package jp.hazuki.yuzubrowser.favicon

import android.graphics.Bitmap
import java.util.*

internal class FaviconCache(
        private var maxMemSize: Int,
        private val mListener: OnIconCacheOverFlowListener
) : LinkedHashMap<Long, Bitmap>(maxMemSize, 0.75f, true) {
    private var cachedMemSize = 0

    override fun put(key: Long, value: Bitmap): Bitmap? {
        cachedMemSize += value.byteCount
        val result = super.put(key, value)
        if (result != null) {
            cachedMemSize -= result.byteCount
        }
        trimSize()
        return result
    }

    override fun clear() {
        super.clear()
        cachedMemSize = 0
    }

    fun setSize(size: Int) {
        maxMemSize = size
    }

    private fun trimSize() {
        if (cachedMemSize <= maxMemSize) return

        val items = entries.iterator()
        while (items.hasNext() && cachedMemSize > maxMemSize) {
            val item = items.next()
            items.remove()

            cachedMemSize -= item.value.byteCount
            mListener.onCacheOverflow(item.key)
        }
        if (size == 0 && cachedMemSize > 0) cachedMemSize = 0
    }

    internal interface OnIconCacheOverFlowListener {
        fun onCacheOverflow(hash: Long)
    }
}