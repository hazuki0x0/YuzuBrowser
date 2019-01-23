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
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.browser.BrowserManager
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import java.io.IOException

class PasteGoSingleAction : SingleAction, Parcelable {
    var targetTab: Int = BrowserManager.LOAD_URL_TAB_CURRENT
        private set

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_TARGET_TAB -> {
                        if (reader.peek() != JsonReader.Token.NUMBER) return
                        targetTab = reader.nextInt()
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
        writer.name(FIELD_TARGET_TAB)
        writer.value(targetTab)
        writer.endObject()
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
