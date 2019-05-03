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

package jp.hazuki.yuzubrowser.core.utility.extensions

import android.util.SparseArray

inline fun <reified T> Any?.isInstanceOf(action: (T) -> Unit) {
    if (this is T) action(this)
}

fun StringBuilder.replace(oldValue: String, newValue: String): StringBuilder {
    var index = indexOf(oldValue)
    while (index >= 0) {
        replace(index, index + oldValue.length, newValue)
        index += newValue.length
        index = indexOf(oldValue, index)
    }
    return this
}

fun StringBuilder.replaceStart(oldValue: String, newValue: String): StringBuilder {
    if (startsWith(oldValue)) {
        replace(0, oldValue.length, newValue)
    }
    return this
}

fun StringBuilder.replaceEnd(oldValue: String, newValue: String): StringBuilder {
    if (endsWith(oldValue)) {
        replace(length - oldValue.length, length, newValue)
    }
    return this
}

inline fun <T> List<T>.contains(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

inline fun <T> Array<T>.contains(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

inline fun <T> List<T>.binarySearch(fromIndex: Int = 0, toIndex: Int = size, comparison: (T) -> Int): Int {
    rangeCheck(size, fromIndex, toIndex)

    var low = fromIndex
    var high = toIndex - 1

    while (low <= high) {
        val mid = (low + high).ushr(1) // safe from overflows
        val midVal = get(mid)
        val cmp = comparison(midVal)

        when {
            cmp < 0 -> low = mid + 1
            cmp > 0 -> high = mid - 1
            else -> return mid // key found
        }
    }
    return -(low + 1)  // key not found
}

inline fun <T> List<T>.binarySearchLong(fromIndex: Int = 0, toIndex: Int = size, comparison: (T) -> Long): Int {
    rangeCheck(size, fromIndex, toIndex)

    var low = fromIndex
    var high = toIndex - 1

    while (low <= high) {
        val mid = (low + high).ushr(1) // safe from overflows
        val midVal = get(mid)
        val cmp = comparison(midVal)

        when {
            cmp < 0 -> low = mid + 1
            cmp > 0 -> high = mid - 1
            else -> return mid // key found
        }
    }
    return -(low + 1)  // key not found
}

/**
 * Checks that `from` and `to` are in
 * the range of [0..size] and throws an appropriate exception, if they aren't.
 */
fun rangeCheck(size: Int, fromIndex: Int, toIndex: Int) {
    when {
        fromIndex > toIndex -> throw IllegalArgumentException("fromIndex ($fromIndex) is greater than toIndex ($toIndex).")
        fromIndex < 0 -> throw IndexOutOfBoundsException("fromIndex ($fromIndex) is less than zero.")
        toIndex > size -> throw IndexOutOfBoundsException("toIndex ($toIndex) is greater than size ($size).")
    }
}

fun <T : Comparable<T>> Sequence<T>.toSortedList(): List<T> {
    val sortedList = toMutableList()
    sortedList.sort()
    return sortedList
}

inline fun <T> T?.isNotNull(notNull: (T) -> Unit, other: () -> Unit) {
    if (this != null) notNull(this) else other()
}

inline fun <T> SparseArray<T>.getOrPut(key: Int, defaultValue: () -> T): T {
    val value = get(key)
    return if (value == null) {
        val answer = defaultValue()
        put(key, answer)
        answer
    } else {
        value
    }
}
