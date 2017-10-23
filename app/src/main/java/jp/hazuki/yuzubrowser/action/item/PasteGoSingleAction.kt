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
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.action.SingleAction
import jp.hazuki.yuzubrowser.action.view.ActionActivity
import jp.hazuki.yuzubrowser.browser.BrowserManager
import jp.hazuki.yuzubrowser.utils.ArrayUtils
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo
import java.io.IOException

class PasteGoSingleAction : SingleAction, Parcelable {
    var targetTab: Int = 0
        private set

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME) return
                if (FIELD_TARGET_TAB != parser.currentName) {
                    parser.skipChildren()
                    continue
                }
                if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                targetTab = parser.intValue
            }
        } else {
            targetTab = BrowserManager.LOAD_URL_TAB_CURRENT
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(generator: JsonGenerator) {
        generator.writeNumber(id)
        generator.writeStartObject()
        generator.writeNumberField(FIELD_TARGET_TAB, targetTab)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(targetTab)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        targetTab = source.readInt()
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val valueArray = context.resources.getIntArray(R.array.pref_newtab_values)
        val defValue = ArrayUtils.findIndexOfValue(targetTab, valueArray)
        AlertDialog.Builder(context)
                .setTitle(R.string.action_target_tab)
                .setSingleChoiceItems(R.array.pref_newtab_list, defValue) { dialog, which ->
                    targetTab = valueArray[which]
                    dialog.dismiss()
                }
                .show()

        return null
    }

    companion object {
        //private static final String TAG = "PasteGoSingleAction";
        private const val FIELD_TARGET_TAB = "0"

        @JvmField
        val CREATOR: Parcelable.Creator<PasteGoSingleAction> = object : Parcelable.Creator<PasteGoSingleAction> {
            override fun createFromParcel(source: Parcel): PasteGoSingleAction {
                return PasteGoSingleAction(source)
            }

            override fun newArray(size: Int): Array<PasteGoSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
