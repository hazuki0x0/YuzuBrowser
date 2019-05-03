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

package jp.hazuki.yuzubrowser.bookmark.repository

import android.content.Context
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkFolder
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkItem
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkSite
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.regex.Pattern

class BookmarkManager private constructor(context: Context) : Serializable {
    val file = File(context.getDir("bookmark1", Context.MODE_PRIVATE), "bookmark1.dat")
    val root = BookmarkFolder(null, null, -1)
    private val siteComparator: Comparator<BookmarkSite> = Comparator { s1, s2 -> s1.url.hashCode().compareTo(s2.url.hashCode()) }
    private val siteIndex = ArrayList<BookmarkSite>()

    init {
        load()
    }

    companion object {
        private var instance: BookmarkManager? = null

        @JvmStatic
        fun getInstance(context: Context): BookmarkManager {
            if (instance == null) {
                instance = BookmarkManager(context.applicationContext)
            }

            return instance!!
        }
    }

    fun load(): Boolean {
        root.clear()

        if (!file.exists() || file.isDirectory) return true

        try {
            JsonReader.of(file.source().buffer()).use {
                root.readForRoot(it)
                createIndex()

                return true
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }
        return false
    }

    fun save(): Boolean {
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        try {
            JsonWriter.of(file.sink().buffer()).use {
                root.writeForRoot(it)
                return true
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }

        return false
    }

    fun addFirst(folder: BookmarkFolder, item: BookmarkItem) {
        folder.list.add(0, item)
        if (item is BookmarkSite) {
            addToIndex(item)
        }
    }

    fun add(folder: BookmarkFolder, item: BookmarkItem) {
        folder.add(item)
        if (item is BookmarkSite) {
            addToIndex(item)
        }
    }

    fun moveToFirst(folder: BookmarkFolder, item: BookmarkItem) {
        folder.list.remove(item)
        folder.list.add(0, item)
    }

    fun addAll(folder: BookmarkFolder, addlist: Collection<BookmarkItem>) {
        folder.list.addAll(addlist)
        addlist.filter { it is BookmarkSite }
                .forEach { addToIndex(it as BookmarkSite) }
    }

    fun remove(folder: BookmarkFolder, item: BookmarkItem) {
        remove(folder, folder.list.indexOf(item))
    }

    fun remove(folder: BookmarkFolder, index: Int) {
        val item = folder.list.removeAt(index)

        if (item is BookmarkSite) {
            removeFromIndex(item)
        }
    }

    fun removeAll(folder: BookmarkFolder, items: List<BookmarkItem>) {
        folder.list.removeAll(items)
        items.filter { it is BookmarkSite }
                .forEach { removeFromIndex(it as BookmarkSite) }
    }

    fun removeAll(url: String) {
        val it = siteIndex.iterator()
        while (it.hasNext()) {
            val site = it.next()
            if (site.url == url) {
                it.remove()
                deepRemove(root, site)
            }
        }
    }

    private fun deepRemove(folder: BookmarkFolder, item: BookmarkItem): Boolean {
        val it = folder.list.iterator()
        while (it.hasNext()) {
            val child = it.next()
            if (child is BookmarkFolder) {
                if (deepRemove(child, item))
                    return true
            } else if (child is BookmarkSite) {
                if (child == item) {
                    it.remove()
                    return true
                }
            }
        }
        return false
    }

    fun moveTo(from: BookmarkFolder, to: BookmarkFolder, siteIndex: Int) {
        val item = from.list.removeAt(siteIndex)
        to.list.add(item)
        if (item is BookmarkFolder) {
            item.parent = to
        }
    }

    fun moveAll(from: BookmarkFolder, to: BookmarkFolder, items: List<BookmarkItem>) {
        from.list.removeAll(items)
        to.list.addAll(items)
        items.filterIsInstance<BookmarkFolder>().forEach { it.parent = to }
    }

    fun search(query: String): List<BookmarkSite> {
        val list = mutableListOf<BookmarkSite>()

        val pattern = Pattern.compile("[^a-zA-Z]\\Q$query\\E")

        search(list, root, pattern)

        return list
    }

    private fun search(list: MutableList<BookmarkSite>, root: BookmarkFolder, pattern: Pattern) {
        root.list.forEach {
            if (it is BookmarkSite) {
                if (pattern.matcher(it.url).find() || pattern.matcher(it.title).find()) {
                    list.add(it)
                }
            }
            if (it is BookmarkFolder) {
                search(list, it, pattern)
            }
        }
    }

    fun isBookmarked(url: String?): Boolean {
        if (url == null) return false

        var low = 0
        var high = siteIndex.size - 1
        val hash = url.hashCode()

        while (low <= high) {
            val mid = (low + high).ushr(1)
            val itemHash = siteIndex[mid].url.hashCode()
            when {
                itemHash < hash -> low = mid + 1
                itemHash > hash -> high = mid - 1
                else -> {
                    if (url == siteIndex[mid].url) {
                        return true
                    }
                    for (i in mid - 1 downTo 0) {
                        val nowHash = siteIndex[i].hashCode()
                        if (hash != nowHash) {
                            break
                        }
                        if (siteIndex[i].url == url) {
                            return true
                        }
                    }
                    for (i in mid + 1 until siteIndex.size) {
                        val nowHash = siteIndex[i].hashCode()
                        if (hash != nowHash) {
                            break
                        }
                        if (siteIndex[i].url == url) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    operator fun get(id: Long): BookmarkItem? {
        return if (id < 0) null else get(id, root)
    }

    private fun get(id: Long, root: BookmarkFolder): BookmarkItem? {
        for (item in root.list) {
            if (item.id == id) {
                return item
            } else if (item is BookmarkFolder) {
                val inner = get(id, item)
                if (inner != null) {
                    return inner
                }
            }
        }
        return null
    }

    private fun createIndex() {
        siteIndex.clear()
        addToIndexFromFolder(root)
    }

    private fun addToIndexFromFolder(folder: BookmarkFolder) {
        folder.list.forEach {
            if (it is BookmarkFolder) {
                addToIndexFromFolder(it)
            }
            if (it is BookmarkSite) {
                addToIndex(it)
            }
        }
    }

    private fun addToIndex(site: BookmarkSite) {
        val hash = site.url.hashCode()
        val index = siteIndex.binarySearch(site, siteComparator)
        if (index < 0) {
            siteIndex.add(index.inv(), site)
        } else {
            if (siteIndex[index] != site) {
                for (i in index - 1 downTo 0) {
                    val itemHash = siteIndex[i].url.hashCode()
                    if (hash != itemHash) {
                        break
                    }
                    if (siteIndex[i] == site) {
                        return
                    }
                }

                for (i in index + 1 until siteIndex.size) {
                    val itemHash = siteIndex[i].url.hashCode()
                    if (hash != itemHash) {
                        break
                    }
                    if (siteIndex[i] == site) {
                        return
                    }
                }
                siteIndex.add(index, site)
            }
        }
    }

    private fun removeFromIndex(site: BookmarkSite) {
        val hash = site.url.hashCode()
        val index = siteIndex.binarySearch(site, siteComparator)
        if (index >= 0) {
            if (siteIndex[index] == site) {
                siteIndex.removeAt(index)
                return
            }
            for (i in index - 1 downTo 0) {
                val itemHash = siteIndex[i].url.hashCode()
                if (hash != itemHash) {
                    break
                }
                if (siteIndex[i] == site) {
                    siteIndex.removeAt(index)
                    return
                }
            }
            for (i in index + 1 until siteIndex.size) {
                val itemHash = siteIndex[i].url.hashCode()
                if (hash != itemHash) {
                    break
                }
                if (siteIndex[i] == site) {
                    siteIndex.removeAt(index)
                    return
                }
            }
        }
    }
}
