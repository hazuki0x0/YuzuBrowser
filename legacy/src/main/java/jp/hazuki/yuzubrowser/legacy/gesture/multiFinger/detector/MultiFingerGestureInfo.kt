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

package jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.detector

import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.data.MultiFingerGestureItem
import java.util.*

class MultiFingerGestureInfo {
    private val traces = ArrayList<Int>()
    var fingers: Int = 0
    var trace: Int = 0
        set(value) {
            field = value
            traces.add(value)
        }

    fun clear() {
        fingers = 0
        trace = 0
        traces.clear()
    }

    fun match(item: MultiFingerGestureItem): Boolean {
        return fingers == item.fingers && traces == item.traces
    }
}
