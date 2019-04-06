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

package jp.hazuki.yuzubrowser.legacy.utils.fastmatch

import android.net.Uri

internal abstract class SimpleCountMatcher : FastMatcher {

    var count: Int = 0
    override var id: Int = 0
    override var isUpdate: Boolean = false
    override var time: Long = 0
    override val frequency: Int
        get() = count

    protected abstract fun matchItem(uri: Uri): Boolean

    override fun match(uri: Uri): Boolean {
        if (matchItem(uri)) {
            if (count != Integer.MAX_VALUE)
                count++
            time = System.currentTimeMillis()
            isUpdate = true
            return true
        }
        return false
    }

    override fun saved() {
        isUpdate = false
    }
}
