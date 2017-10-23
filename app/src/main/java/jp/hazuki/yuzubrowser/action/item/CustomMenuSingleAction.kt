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

package jp.hazuki.yuzubrowser.action.item

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.ActionList
import jp.hazuki.yuzubrowser.action.SingleAction
import jp.hazuki.yuzubrowser.action.view.ActionActivity
import jp.hazuki.yuzubrowser.action.view.ActionListActivity
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo
import java.io.IOException

class CustomMenuSingleAction : SingleAction, Parcelable {
    var actionList: ActionList
        private set

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {

        actionList = ActionList()
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME) return
                if (FIELD_NAME_ACTION_LIST != parser.currentName) {
                    parser.skipChildren()
                    continue
                }
                actionList.loadAction(parser)
            }
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(generator: JsonGenerator) {
        generator.writeNumber(id)
        generator.writeStartObject()
        actionList.writeAction(FIELD_NAME_ACTION_LIST, generator)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeParcelable(actionList, flags)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        actionList = source.readParcelable(ActionList::class.java.classLoader)
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
