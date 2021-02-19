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
import android.graphics.drawable.Drawable
import android.util.SparseIntArray
import androidx.core.util.set
import jp.hazuki.yuzubrowser.legacy.R

class ActionIconMap(resources: Resources) {
    private val iconPos: SparseIntArray
    private val icons = resources.obtainTypedArray(R.array.action_icons)

    init {
        val iconPosDB = resources.getIntArray(R.array.action_values)
        iconPos = SparseIntArray(iconPosDB.size)
        iconPosDB.forEachIndexed { index, i ->
            iconPos[i] = index
        }
    }

    operator fun get(id: Int): Drawable? {
        val pos = iconPos.get(id, -1)
        if (pos < 0) return null

        return icons.getDrawable(pos)
    }

    operator fun get(action: Action?): Drawable? {
        return if (action == null || action.size == 0) {
            null
        } else {
            get(action[0].id)
        }
    }
}
