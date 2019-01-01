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

import android.app.AlertDialog
import android.os.Parcel
import android.os.Parcelable
import android.widget.CheckBox
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.utils.app.StartActivityInfo
import java.io.IOException

class LeftRightTabSingleAction : SingleAction {
    var isTabLoop = false
        private set

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME) return
                if (FIELD_NAME_TAB_LOOP != parser.currentName) {
                    parser.skipChildren()
                    continue
                }
                when (parser.nextToken()) {
                    JsonToken.VALUE_TRUE -> isTabLoop = true
                    JsonToken.VALUE_FALSE -> isTabLoop = false
                    else -> Logger.w(TAG, "current token is not boolean value : " + parser.currentToken.toString())
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(generator: JsonGenerator) {
        generator.writeNumber(id)
        generator.writeStartObject()
        generator.writeBooleanField(FIELD_NAME_TAB_LOOP, isTabLoop)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(if (isTabLoop) 1 else 0)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        isTabLoop = source.readInt() == 1
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = CheckBox(context)
        view.setText(R.string.action_tab_loop)
        view.isChecked = isTabLoop

        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ -> isTabLoop = view.isChecked }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return null
    }

    companion object {
        private const val TAG = "LeftRightTabSingleAction"
        private const val FIELD_NAME_TAB_LOOP = "0"

        @JvmField
        val CREATOR: Parcelable.Creator<LeftRightTabSingleAction> = object : Parcelable.Creator<LeftRightTabSingleAction> {
            override fun createFromParcel(source: Parcel): LeftRightTabSingleAction {
                return LeftRightTabSingleAction(source)
            }

            override fun newArray(size: Int): Array<LeftRightTabSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }

}
