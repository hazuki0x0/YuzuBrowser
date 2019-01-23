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

package jp.hazuki.yuzubrowser.legacy.action.item

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionList
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.action.view.ActionListActivity
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import java.io.IOException

class CustomMenuSingleAction : SingleAction, Parcelable {
    var actionList: ActionList
        private set

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {
        actionList = ActionList()
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_NAME_ACTION_LIST -> {
                        actionList.loadAction(reader)
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(writer: JsonWriter) {
        writer.value(id)
        writer.beginObject()
        writer.name(FIELD_NAME_ACTION_LIST)
        actionList.writeAction(writer)
        writer.endObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeParcelable(actionList, flags)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        actionList = source.readParcelable(ActionList::class.java.classLoader)!!
    }

    override fun showMainPreference(context: ActionActivity): StartActivityInfo? {
        return showSubPreference(context)
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        return ActionListActivity.Builder(context)
                .setTitle(R.string.action_select_menu)
                .setActionNameArray(context.actionNameArray)
                .setDefaultActionList(actionList)
                .setOnActionListActivityResultListener { actionList -> this.actionList = actionList }
                .makeStartActivityInfo()
    }

    companion object {
        private const val FIELD_NAME_ACTION_LIST = "0"

        @JvmField
        val CREATOR: Parcelable.Creator<CustomMenuSingleAction> = object : Parcelable.Creator<CustomMenuSingleAction> {
            override fun createFromParcel(source: Parcel): CustomMenuSingleAction {
                return CustomMenuSingleAction(source)
            }

            override fun newArray(size: Int): Array<CustomMenuSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
