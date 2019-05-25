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

import android.content.Context
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import jp.hazuki.yuzubrowser.core.android.utils.calcImageHash
import jp.hazuki.yuzubrowser.core.cache.DiskLruCache
import jp.hazuki.yuzubrowser.core.utility.hash.formatHashString
import jp.hazuki.yuzubrowser.core.utility.hash.parseHashString
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class FaviconManager(context: Context) : FaviconCache.OnIconCacheOverFlowListener, DiskLruCache.OnTrimCacheListener {

    private val diskCache = DiskLruCache.open(context.getDir(CACHE_FOLDER, Context.MODE_PRIVATE), 1, 1, DISK_CACHE_SIZE.toLong())
    private val diskCacheIndex = FaviconCacheIndex(context.applicationContext, CACHE_FOLDER)
    private val ramCache = FaviconCache(RAM_CACHE_SIZE, this)
    private val ramCacheIndex: MutableMap<String, Long> = HashMap()

    init {
        diskCache.setOnTrimCacheListener(this)
    }

    operator fun set(url: String, icon: Bitmap?) {
        if (icon == null || TextUtils.isEmpty(url)) return

        val normalizedUrl = getNormalUrl(url)

        val vec = icon.calcImageHash()
        val hash = formatHashString(vec)
        if (!ramCache.containsKey(vec)) {
            ramCache[vec] = icon
            addToDiskCache(hash, icon)
        }

        diskCacheIndex.add(normalizedUrl, vec)

        synchronized(ramCacheIndex) {
            ramCacheIndex.put(normalizedUrl, vec)
        }
    }

    operator fun get(url: String): Bitmap? {
        if (TextUtils.isEmpty(url)) return null

        val normalizedUrl = getNormalUrl(url)

        synchronized(ramCacheIndex) {
            val icon = ramCacheIndex[normalizedUrl]
            if (icon != null) {
                return ramCache[icon]
            }
        }

        val result = diskCacheIndex[normalizedUrl]
        if (result.exists) {
            val icon = getFromDiskCache(formatHashString(result.hash))
            if (icon != null) {
                ramCache[result.hash] = icon
                synchronized(ramCacheIndex) {
                    ramCacheIndex.put(normalizedUrl, result.hash)
                }
            } else {
                try {
                    diskCacheIndex.remove(result.hash)
                } catch (e: SQLiteException) {
                    e.printStackTrace()
                }

            }
            return icon
        }

        return null
    }

    fun getFaviconBytes(url: String): ByteArray? {
        if (TextUtils.isEmpty(url)) return null

        val normalizedUrl = getNormalUrl(url)

        synchronized(ramCacheIndex) {
            val icon = ramCacheIndex[normalizedUrl]
            if (icon != null) {
                val os = ByteArrayOutputStream()
                ramCache[icon]!!.compress(Bitmap.CompressFormat.PNG, 100, os)
                return os.toByteArray()
            }
        }

        val result = diskCacheIndex[normalizedUrl]
        return if (result.exists) {
            getFromDiskCacheBytes(formatHashString(result.hash))
        } else null

    }

    fun save() {
        try {
            diskCache.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun clear() {
        diskCacheIndex.clear()
        diskCache.clear()
        ramCacheIndex.clear()
        ramCache.clear()
    }

    fun destroy() {
        ramCache.clear()
        ramCacheIndex.clear()
        try {
            diskCache.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getNormalUrl(url: String): String {
        var resolveUrl = url
        var index = resolveUrl.indexOf('?')
        if (index > -1) {
            resolveUrl = resolveUrl.substring(0, index)
        }
        index = resolveUrl.indexOf('#')
        if (index > -1) {
            resolveUrl = resolveUrl.substring(0, index)
        }
        return resolveUrl
    }

    private fun addToDiskCache(key: String, bitmap: Bitmap) {
        synchronized(diskCache) {
            var out: OutputStream? = null
            try {
                val snapshot = diskCache.get(key)
                if (snapshot == null) {
                    val editor = diskCache.edit(key)
                    if (editor != null) {
                        out = editor.newOutputStream(DISK_CACHE_INDEX)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        editor.commit()
                        out!!.close()
                    }
                } else {
                    snapshot.getInputStream(DISK_CACHE_INDEX).close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    out?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun getFromDiskCache(key: String): Bitmap? {
        synchronized(diskCache) {
            try {
                val snapshot = diskCache.get(key)
                if (snapshot != null) {
                    snapshot.getInputStream(DISK_CACHE_INDEX)?.use {
                        return BitmapFactory.decodeStream(it)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }

    private fun getFromDiskCacheBytes(key: String): ByteArray? {

        synchronized(diskCache) {
            try {
                val snapshot = diskCache.get(key)
                if (snapshot != null) {
                    snapshot.getInputStream(DISK_CACHE_INDEX)?.use {
                        return it.readBytes()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    override fun onCacheOverflow(hash: Long) {
        synchronized(ramCacheIndex) {
            val iterator = ramCacheIndex.entries.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (hash == item.value) {
                    iterator.remove()
                }
            }
        }
    }

    override fun onTrim(key: String) {
        diskCacheIndex.remove(parseHashString(key))
    }

    companion object {

        private const val DISK_CACHE_SIZE = 10 * 1024 * 1024
        private const val RAM_CACHE_SIZE = 1024 * 1024
        private const val CACHE_FOLDER = "favicon"
        private const val DISK_CACHE_INDEX = 0
    }
}
