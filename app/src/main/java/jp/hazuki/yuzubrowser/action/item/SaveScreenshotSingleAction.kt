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

import android.app.AlertDialog
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.BrowserApplication
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.SingleAction
import jp.hazuki.yuzubrowser.action.view.ActionActivity
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo
import java.io.File
import java.io.IOException

class SaveScreenshotSingleAction : SingleAction, Parcelable {
    private var mSsType = SS_TYPE_PART
    var folder = File(BrowserApplication.externalUserDirectory, "screenshot")
        private set

    val type: Int
        get() = if (AppData.slow_rendering.get()) mSsType else SS_TYPE_PART

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME) return
                if (FIELD_NAME_SS_TYPE == parser.currentName) {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                    mSsType = parser.intValue
                    continue
                }
                if (FIELD_NAME_SAVE_FOLDER == parser.currentName) {
                    if (parser.nextToken() != JsonToken.VALUE_STRING) return
                    folder = File(parser.text)
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
        generator.writeNumberField(FIELD_NAME_SS_TYPE, mSsType)
        generator.writeStringField(FIELD_NAME_SAVE_FOLDER, folder.absolutePath)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(mSsType)
        dest.writeString(folder.absolutePath)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        mSsType = source.readInt()
        folder = File(source.readString())
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_screenshot_settings, null)
        val captureAllCheckBox = view.findViewById<CheckBox>(R.id.captureAllCheckBox)

        captureAllCheckBox.isChecked = mSsType == SS_TYPE_ALL

        if (AppData.slow_rendering.get()) {
            captureAllCheckBox.isEnabled = true
            view.findViewById<View>(R.id.captureAllErrorTextView).visibility = View.GONE
        }

        val folderEditText = view.findViewById<EditText>(R.id.folderEditText)
        folderEditText.setText(folder.absolutePath)

        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    mSsType = if (captureAllCheckBox.isChecked) SS_TYPE_ALL else SS_TYPE_PART

                    //mPath = folderButton.getCurrentFolder();

                    folder = File(folderEditText.text.toString())
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return null
    }

    companion object {
        private const val FIELD_NAME_SS_TYPE = "0"
        private const val FIELD_NAME_SAVE_FOLDER = "1"
        const val SS_TYPE_ALL = 0
        const val SS_TYPE_PART = 1

        @JvmField
        val CREATOR: Parcelable.Creator<SaveScreenshotSingleAction> = object : Parcelable.Creator<SaveScreenshotSingleAction> {
            override fun createFromParcel(source: Parcel): SaveScreenshotSingleAction {
                return SaveScreenshotSingleAction(source)
            }

            override fun newArray(size: Int): Array<SaveScreenshotSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
