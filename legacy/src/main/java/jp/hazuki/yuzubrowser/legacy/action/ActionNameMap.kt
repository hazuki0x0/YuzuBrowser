/*
 * Copyright (C) 2017-2021 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.action

import android.content.res.Resources
import androidx.collection.SparseArrayCompat
import androidx.collection.set
import jp.hazuki.yuzubrowser.legacy.R

class ActionNameMap(resources: Resources) {
    private val names = SparseArrayCompat<String>()

    init {
        val actionList = resources.getStringArray(R.array.action_list)
        val actionValues = resources.getIntArray(R.array.action_values)

        require(actionList.size == actionValues.size)
        for (i in actionList.indices) {
            names[actionValues[i]] = actionList[i]
        }
    }

    operator fun get(key: Int) = names[key]

    operator fun get(action: Action?): String? {
        return if (action == null || action.size == 0) {
            null
        } else {
            get(action[0].id)
        }
    }
}
