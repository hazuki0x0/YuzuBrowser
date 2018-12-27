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

import android.app.AlertDialog
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.CheckBox
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.utils.Logger
import jp.hazuki.yuzubrowser.legacy.utils.app.StartActivityInfo
import java.io.IOException

class FinishSingleAction : SingleAction, Parcelable {
    var isShowAlert = true
        private set
    var isCloseTab = false
        private set

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME) return
                if (FIELD_NAME_ALERT == parser.currentName) {
                    when (parser.nextToken()) {
                        JsonToken.VALUE_TRUE -> isShowAlert = true
                        JsonToken.VALUE_FALSE -> isShowAlert = false
                        else -> Logger.w(TAG, "current token is not boolean value : " + parser.currentToken.toString())
                    }
                    continue
                }
                if (FIELD_NAME_CLOSE_TAB == parser.currentName) {
                    when (parser.nextToken()) {
                        JsonToken.VALUE_TRUE -> isCloseTab = true
                        JsonToken.VALUE_FALSE -> isCloseTab = false
                        else -> Logger.w(TAG, "current token is not boolean value : " + parser.currentToken.toString())
                    }
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
        generator.writeBooleanField(FIELD_NAME_ALERT, isShowAlert)
        generator.writeBooleanField(FIELD_NAME_CLOSE_TAB, isCloseTab)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(if (isShowAlert) 1 else 0)
        dest.writeInt(if (isCloseTab) 1 else 0)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        isShowAlert = source.readInt() == 1
        isCloseTab = source.readInt() == 1
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_finish_setting, null)
        val finishAlertCheckBox = view.findViewById<CheckBox>(R.id.finishAlertCheckBox)
        val closeTabCheckBox = view.findViewById<CheckBox>(R.id.closetabCheckBox)

        finishAlertCheckBox.isChecked = isShowAlert
        closeTabCheckBox.isChecked = isCloseTab

        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    isShowAlert = finishAlertCheckBox.isChecked
                    isCloseTab = closeTabCheckBox.isChecked
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return null
    }

    companion object {
        private const val TAG = "FinishSingleAction"
        private const val FIELD_NAME_ALERT = "0"
        private const val FIELD_NAME_CLOSE_TAB = "1"

        @JvmField
        val CREATOR: Parcelable.Creator<FinishSingleAction> = object : Parcelable.Creator<FinishSingleAction> {
            override fun createFromParcel(source: Parcel): FinishSingleAction {
                return FinishSingleAction(source)
            }

            override fun newArray(size: Int): Array<FinishSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }

}
