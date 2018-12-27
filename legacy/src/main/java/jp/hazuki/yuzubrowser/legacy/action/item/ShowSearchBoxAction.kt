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
import jp.hazuki.yuzubrowser.legacy.utils.app.StartActivityInfo
import java.io.IOException

class ShowSearchBoxAction : SingleAction, Parcelable {

    var isOpenNewTab: Boolean = false
        private set
    var isReverse: Boolean = false
        private set

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (FIELD_OPEN_NEW_TAB == parser.currentName) {
                    if (parser.nextValue().isBoolean)
                        isOpenNewTab = parser.booleanValue
                }
                if (FIELD_REVERSE == parser.currentName) {
                    if (parser.nextValue().isBoolean)
                        isReverse = parser.booleanValue
                }

                if (parser.currentToken == JsonToken.START_OBJECT || parser.currentToken == JsonToken.START_ARRAY) {
                    parser.skipChildren()
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(generator: JsonGenerator) {
        generator.writeNumber(id)
        generator.writeStartObject()
        generator.writeBooleanField(FIELD_OPEN_NEW_TAB, isOpenNewTab)
        generator.writeBooleanField(FIELD_REVERSE, isReverse)
        generator.writeEndObject()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(if (isOpenNewTab) 1 else 0)
        dest.writeInt(if (isReverse) 1 else 0)
    }

    private constructor(source: Parcel) : super(source) {
        isOpenNewTab = source.readInt() != 0
        isReverse = source.readInt() != 0
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_show_search_box, null)
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val bottom = view.findViewById<CheckBox>(R.id.checkBox2)
        checkBox.isChecked = isOpenNewTab
        bottom.isChecked = isReverse
        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    isOpenNewTab = checkBox.isChecked
                    isReverse = bottom.isChecked
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        return null
    }

    companion object {
        private const val FIELD_OPEN_NEW_TAB = "0"
        private const val FIELD_REVERSE = "1"

        @JvmField
        val CREATOR: Parcelable.Creator<ShowSearchBoxAction> = object : Parcelable.Creator<ShowSearchBoxAction> {
            override fun createFromParcel(source: Parcel): ShowSearchBoxAction {
                return ShowSearchBoxAction(source)
            }

            override fun newArray(size: Int): Array<ShowSearchBoxAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
