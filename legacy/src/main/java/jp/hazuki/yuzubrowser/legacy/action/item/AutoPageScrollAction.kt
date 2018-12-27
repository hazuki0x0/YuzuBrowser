/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.legacy.action.item

import android.app.AlertDialog
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.utils.app.StartActivityInfo
import java.io.IOException

class AutoPageScrollAction : SingleAction, Parcelable {
    var scrollSpeed = 40
        private set

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME) return
                if (FIELD_NAME_SPEED == parser.currentName) {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                    scrollSpeed = parser.intValue
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
        generator.writeNumberField(FIELD_NAME_SPEED, scrollSpeed)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(scrollSpeed)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        scrollSpeed = source.readInt()
    }

    override fun showMainPreference(context: ActionActivity): StartActivityInfo? {
        return showSubPreference(context)
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_auto_scroll, null)
        val editText = view.findViewById<EditText>(R.id.editText)
        editText.setText(scrollSpeed.toString())
        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    var y = 0

                    try {
                        y = Integer.parseInt(editText.text.toString())
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }

                    if (y == 0) {
                        Toast.makeText(context.applicationContext, R.string.action_auto_scroll_speed_zero, Toast.LENGTH_SHORT).show()
                        showSubPreference(context)
                        return@setPositiveButton
                    }

                    scrollSpeed = y
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        return null
    }

    companion object {
        private const val FIELD_NAME_SPEED = "0"

        @JvmField
        val CREATOR: Parcelable.Creator<AutoPageScrollAction> = object : Parcelable.Creator<AutoPageScrollAction> {
            override fun createFromParcel(source: Parcel): AutoPageScrollAction {
                return AutoPageScrollAction(source)
            }

            override fun newArray(size: Int): Array<AutoPageScrollAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
