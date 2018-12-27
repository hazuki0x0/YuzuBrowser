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

package jp.hazuki.yuzubrowser.legacy.action.item

import android.app.Activity
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.utils.app.StartActivityInfo
import java.io.IOException

class CustomSingleAction : SingleAction {
    var action: Action
        private set
    private var mName: String? = null

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {

        action = Action()
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME) return
                if (FIELD_NAME_ACTION == parser.currentName) {
                    action.loadAction(parser)
                    continue
                }
                if (FIELD_NAME_ACTION_NAME == parser.currentName) {
                    if (parser.nextToken() != JsonToken.VALUE_STRING) return
                    mName = parser.text
                    continue
                }
                parser.skipChildren()
            }
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(generator: JsonGenerator) {
        generator.writeNumber(id)
        generator.writeStartObject()
        action.writeAction(FIELD_NAME_ACTION, generator)
        generator.writeStringField(FIELD_NAME_ACTION_NAME, mName)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeParcelable(action, flags)
        dest.writeString(mName)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        action = source.readParcelable(Action::class.java.classLoader)!!
        mName = source.readString()
    }

    override fun showMainPreference(context: ActionActivity): StartActivityInfo? {
        return showSubPreference(context)
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val intent = Intent(context, CustomSingleActionActivity::class.java)
        intent.putExtra(CustomSingleActionActivity.EXTRA_ACTION, action as Parcelable?)
        intent.putExtra(CustomSingleActionActivity.EXTRA_NAME, mName)
        intent.putExtra(ActionNameArray.INTENT_EXTRA, context.actionNameArray as Parcelable)

        return StartActivityInfo(intent) { _, resultCode, data ->
            if (resultCode != Activity.RESULT_OK || data == null)
                return@StartActivityInfo
            action = data.getParcelableExtra(CustomSingleActionActivity.EXTRA_ACTION)
            mName = data.getStringExtra(CustomSingleActionActivity.EXTRA_NAME)
        }
    }

    override fun toString(nameArray: ActionNameArray): String? {
        return if (mName != null) mName else super.toString(nameArray)
    }

    companion object {
        private const val FIELD_NAME_ACTION = "0"
        private const val FIELD_NAME_ACTION_NAME = "1"

        @JvmField
        val CREATOR: Parcelable.Creator<CustomSingleAction> = object : Parcelable.Creator<CustomSingleAction> {
            override fun createFromParcel(source: Parcel): CustomSingleAction {
                return CustomSingleAction(source)
            }

            override fun newArray(size: Int): Array<CustomSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
