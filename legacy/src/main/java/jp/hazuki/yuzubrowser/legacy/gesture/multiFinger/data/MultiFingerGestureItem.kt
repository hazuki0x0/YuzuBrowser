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

package jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.data

import android.os.Parcel
import android.os.Parcelable
import jp.hazuki.yuzubrowser.legacy.action.Action
import java.util.*

class MultiFingerGestureItem : Parcelable {
    val traces = ArrayList<Int>()
    var fingers = 1
    var action: Action

    constructor() {
        action = Action()
    }

    fun addTrace(action: Int) {
        traces.add(action)
    }

    fun removeLastTrace() {
        if (traces.size > 0)
            traces.removeAt(traces.size - 1)
    }

    fun checkTrace(trace: Int): Boolean {
        return traces.size <= 0 || traces[traces.size - 1] != trace
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeList(traces)
        dest.writeInt(fingers)
        dest.writeParcelable(action, flags)
    }

    private constructor(parcel: Parcel) {
        parcel.readList(traces, null)
        fingers = parcel.readInt()
        action = parcel.readParcelable(Action::class.java.classLoader)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<MultiFingerGestureItem> = object : Parcelable.Creator<MultiFingerGestureItem> {
            override fun createFromParcel(`in`: Parcel): MultiFingerGestureItem {
                return MultiFingerGestureItem(`in`)
            }

            override fun newArray(size: Int): Array<MultiFingerGestureItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
