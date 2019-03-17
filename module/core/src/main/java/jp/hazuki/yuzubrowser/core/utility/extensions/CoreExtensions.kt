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

inline fun <T> List<T>.contains(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

inline fun <T> Array<T>.contains(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}