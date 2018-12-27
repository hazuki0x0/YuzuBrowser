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
import android.widget.Spinner
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.utils.Logger
import jp.hazuki.yuzubrowser.legacy.utils.app.StartActivityInfo
import java.io.IOException

class TabListSingleAction : SingleAction, Parcelable {
    var mode = MODE_HORIZONTAL
        private set
    var isLeftButton: Boolean = false
        private set
    var lastTabMode = 0
        private set

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                when (parser.currentName) {
                    FIELD_NAME_REVERSE -> {
                        when (parser.nextToken()) {
                            JsonToken.VALUE_TRUE -> mode = MODE_REVERSE
                            JsonToken.VALUE_FALSE -> mode = MODE_NORMAL
                            else -> Logger.w(TAG, "current token is not boolean value : " + parser.currentToken.toString())
                        }
                    }
                    FIELD_NAME_MODE -> {
                        if (parser.nextValue().isNumeric)
                            mode = parser.intValue
                    }
                    FIELD_NAME_LEFT_BUTTON -> {
                        if (parser.nextValue().isBoolean)
                            isLeftButton = parser.booleanValue
                    }
                    FIELD_NAME_LAST_TAB_MODE -> {
                        if (parser.nextValue().isNumeric)
                            lastTabMode = parser.intValue
                    }
                    else -> parser.skipChildren()
                }

            }
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(generator: JsonGenerator) {
        generator.writeNumber(id)
        generator.writeStartObject()
        generator.writeNumberField(FIELD_NAME_MODE, mode)
        generator.writeBooleanField(FIELD_NAME_LEFT_BUTTON, isLeftButton)
        generator.writeNumberField(FIELD_NAME_LAST_TAB_MODE, lastTabMode)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(mode)
        dest.writeInt(lastTabMode)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        mode = source.readInt()
        lastTabMode = source.readInt()
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_tab_list, null)
        val modeSpinner = view.findViewById<Spinner>(R.id.modeSpinner)
        val buttonSpinner = view.findViewById<Spinner>(R.id.buttonSpinner)
        val lastTabSpinner = view.findViewById<Spinner>(R.id.lastTabSpinner)

        modeSpinner.setSelection(mode)
        buttonSpinner.setSelection(if (isLeftButton) 1 else 0)
        lastTabSpinner.setSelection(lastTabMode)
        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    mode = modeSpinner.selectedItemPosition
                    isLeftButton = buttonSpinner.selectedItemPosition > 0
                    lastTabMode = lastTabSpinner.selectedItemPosition
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return null
    }

    companion object {
        const val MODE_NORMAL = 0
        const val MODE_REVERSE = 1
        const val MODE_HORIZONTAL = 2

        const val LAST_TAB_MODE_NONE = 0
        const val LAST_TAB_MODE_NEW_TAB = 1
        const val LAST_TAB_MODE_FINISH = 2
        const val LAST_TAB_MODE_FINISH_WITH_ALERT = 3

        private const val TAG = "TabListSingleAction"
        private const val FIELD_NAME_REVERSE = "0"
        private const val FIELD_NAME_MODE = "1"
        private const val FIELD_NAME_LEFT_BUTTON = "2"
        private const val FIELD_NAME_LAST_TAB_MODE = "3"

        @JvmField
        val CREATOR: Parcelable.Creator<TabListSingleAction> = object : Parcelable.Creator<TabListSingleAction> {
            override fun createFromParcel(source: Parcel): TabListSingleAction {
                return TabListSingleAction(source)
            }

            override fun newArray(size: Int): Array<TabListSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
