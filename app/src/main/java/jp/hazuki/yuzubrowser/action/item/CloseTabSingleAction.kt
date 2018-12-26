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
import jp.hazuki.yuzubrowser.action.Action
import jp.hazuki.yuzubrowser.action.SingleAction
import jp.hazuki.yuzubrowser.action.view.ActionActivity
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo
import java.io.IOException

class CloseTabSingleAction : SingleAction, Parcelable {
    var defaultAction: Action
        private set

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {

        defaultAction = Action()
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME) return
                if (FIELD_NAME_ACTION != parser.currentName) {
                    parser.skipChildren()
                    continue
                }
                defaultAction.loadAction(parser)
            }
        } else {
            defaultAction.add(SingleAction.makeInstance(SingleAction.FINISH))
        }
    }

    constructor() : super(SingleAction.CLOSE_TAB) {

        defaultAction = Action()
    }

    @Throws(IOException::class)
    override fun writeIdAndData(generator: JsonGenerator) {
        generator.writeNumber(id)
        generator.writeStartObject()
        defaultAction.writeAction(FIELD_NAME_ACTION, generator)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeParcelable(defaultAction, flags)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        defaultAction = source.readParcelable(Action::class.java.classLoader)!!
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        return ActionActivity.Builder(context)
                .setActionNameArray(context.actionNameArray)
                .setDefaultAction(defaultAction)
                .setTitle(R.string.action_action_cant_close_tab)
                .setOnActionActivityResultListener { defaultAction = it }
                .makeStartActivityInfo()
    }

    companion object {
        private const val FIELD_NAME_ACTION = "0"

        @JvmField
        val CREATOR: Parcelable.Creator<CloseTabSingleAction> = object : Parcelable.Creator<CloseTabSingleAction> {
            override fun createFromParcel(source: Parcel): CloseTabSingleAction {
                return CloseTabSingleAction(source)
            }

            override fun newArray(size: Int): Array<CloseTabSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
