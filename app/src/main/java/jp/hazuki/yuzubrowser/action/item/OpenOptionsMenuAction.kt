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

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Parcel
import android.os.Parcelable
import android.view.Gravity
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.SingleAction
import jp.hazuki.yuzubrowser.action.view.ActionActivity
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo
import java.io.IOException

class OpenOptionsMenuAction : SingleAction {
    private var showMode = 1

    val gravity: Int
        get() = getGravity(showMode)

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME) return
                if (FIELD_NAME_SHOW_MODE == parser.currentName) {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                    showMode = parser.intValue
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
        generator.writeNumberField(FIELD_NAME_SHOW_MODE, showMode)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(showMode)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        showMode = source.readInt()
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        AlertDialog.Builder(context)
                .setTitle(R.string.action_open_menu)
                .setSingleChoiceItems(R.array.action_open_menu_list, showMode) { dialog, which ->
                    showMode = which
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return null
    }

    companion object {
        private const val FIELD_NAME_SHOW_MODE = "0"

        @JvmField
        val CREATOR: Parcelable.Creator<OpenOptionsMenuAction> = object : Parcelable.Creator<OpenOptionsMenuAction> {
            override fun createFromParcel(source: Parcel): OpenOptionsMenuAction {
                return OpenOptionsMenuAction(source)
            }

            override fun newArray(size: Int): Array<OpenOptionsMenuAction?> {
                return arrayOfNulls(size)
            }
        }

        @SuppressLint("RtlHardcoded")
        fun getGravity(mode: Int): Int {
            return when (mode) {
                0 -> Gravity.BOTTOM or Gravity.LEFT
                1 -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                2 -> Gravity.BOTTOM or Gravity.RIGHT
                else -> throw RuntimeException("Unknown mode : " + mode)
            }
        }
    }
}
