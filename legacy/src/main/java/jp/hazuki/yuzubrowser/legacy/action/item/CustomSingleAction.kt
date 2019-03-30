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

import android.app.Activity
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import java.io.IOException

class CustomSingleAction : SingleAction {
    var action: Action
        private set
    private var mName: String? = null

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {
        action = Action()
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_NAME_ACTION -> {
                        action.loadAction(reader)
                    }
                    FIELD_NAME_ACTION_NAME -> {
                        if (reader.peek() == JsonReader.Token.NULL) {
                            reader.nextNull<String>()
                        } else {
                            if (reader.peek() == JsonReader.Token.STRING) {
                                mName = reader.nextString()
                            } else {
                                reader.skipValue()
                            }
                        }
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
        writer.name(FIELD_NAME_ACTION)
        action.writeAction(writer)
        writer.name(FIELD_NAME_ACTION_NAME)
        writer.value(mName)
        writer.endObject()
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
