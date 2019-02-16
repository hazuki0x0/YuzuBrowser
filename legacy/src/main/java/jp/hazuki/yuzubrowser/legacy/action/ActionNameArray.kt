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

package jp.hazuki.yuzubrowser.legacy.action

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import jp.hazuki.yuzubrowser.legacy.R
import java.io.Serializable

class ActionNameArray : Serializable, Parcelable {

    val actionList: Array<String?>
    val actionValues: IntArray

    constructor(context: Context) {
        val res = context.resources
        actionList = res.getStringArray(R.array.action_list)
        actionValues = res.getIntArray(R.array.action_values)
    }

    constructor(context: Context, addListId: Int, addValuesId: Int) {
        val res = context.resources
        val baseList = res.getStringArray(R.array.action_list)
        val baseValues = res.getIntArray(R.array.action_values)
        val addList = res.getStringArray(addListId)
        val addValues = res.getIntArray(addValuesId)

        actionList = arrayOfNulls(baseList.size + addList.size)
        System.arraycopy(addList, 0, actionList, 0, addList.size)
        System.arraycopy(baseList, 0, actionList, addList.size, baseList.size)

        actionValues = IntArray(baseValues.size + addValues.size)
        System.arraycopy(addValues, 0, actionValues, 0, addValues.size)
        System.arraycopy(baseValues, 0, actionValues, addValues.size, baseValues.size)
    }

    constructor(source: Parcel) {
        actionList = source.createStringArray()!!
        actionValues = source.createIntArray()!!
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStringArray(actionList)
        dest.writeIntArray(actionValues)
    }

    fun size(): Int {
        return actionValues.size
    }

    companion object {
        private const val serialVersionUID = 442574116730542765L
        const val INTENT_EXTRA = "action.extra.actionNameArray"

        @JvmField
        val CREATOR: Parcelable.Creator<ActionNameArray> = object : Parcelable.Creator<ActionNameArray> {
            override fun createFromParcel(source: Parcel): ActionNameArray {
                return ActionNameArray(source)
            }

            override fun newArray(size: Int): Array<ActionNameArray?> {
                return arrayOfNulls(size)
            }
        }
    }
}
