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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import java.io.IOException

class TranslatePageSingleAction : SingleAction, Parcelable {
    var translateFrom: String? = null
    var translateTo: String? = null
        private set

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_NAME_FROM -> {
                        if (reader.peek() == JsonReader.Token.NULL) {
                            reader.nextNull<String>()
                        } else {
                            if (reader.peek() != JsonReader.Token.STRING) return
                            translateFrom = reader.nextString()
                        }
                    }
                    FIELD_NAME_TO -> {
                        if (reader.peek() == JsonReader.Token.NULL) {
                            reader.nextNull<String>()
                        } else {
                            if (reader.peek() != JsonReader.Token.STRING) return
                            translateTo = reader.nextString()
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
        writer.name(FIELD_NAME_FROM)
        writer.value(translateFrom)
        writer.name(FIELD_NAME_TO)
        writer.value(translateTo)
        writer.endObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(translateFrom)
        dest.writeString(translateTo)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        translateFrom = source.readString()
        translateTo = source.readString()
    }

    override fun showMainPreference(context: ActionActivity): StartActivityInfo? {
        return showSubPreference(context)
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_translate_page_setting, null)
        val fromSpinner = view.findViewById<Spinner>(R.id.fromSpinner)
        val toSpinner = view.findViewById<Spinner>(R.id.toSpinner)

        val list = context.resources.getStringArray(R.array.translate_language_list)
        val values = context.resources.getStringArray(R.array.translate_language_values)
        val length = list.size

        val fromList = arrayOfNulls<String>(length + 1)
        fromList[0] = context.getString(R.string.action_translate_auto_select)
        System.arraycopy(list, 0, fromList, 1, length)
        val fromAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, fromList)
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fromSpinner.adapter = fromAdapter

        val toList = arrayOfNulls<String>(length + 1)
        toList[0] = context.getString(R.string.action_translate_select_each)
        System.arraycopy(list, 0, toList, 1, length)
        val toAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, toList)
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toSpinner.adapter = toAdapter

        var index: Int
        index = ArrayUtils.findIndexOfValue<String>(translateFrom, values) + 1
        fromSpinner.setSelection(index)
        index = ArrayUtils.findIndexOfValue<String>(translateTo, values) + 1
        toSpinner.setSelection(index)

        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    var position = fromSpinner.selectedItemPosition
                    translateFrom = if (position == 0) "auto" else values[position - 1]
                    position = toSpinner.selectedItemPosition
                    translateTo = if (position == 0) null else values[position - 1]
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return null
    }

    companion object {
        private const val FIELD_NAME_FROM = "0"
        private const val FIELD_NAME_TO = "1"

        @JvmField
        val CREATOR: Parcelable.Creator<TranslatePageSingleAction> = object : Parcelable.Creator<TranslatePageSingleAction> {
            override fun createFromParcel(source: Parcel): TranslatePageSingleAction {
                return TranslatePageSingleAction(source)
            }

            override fun newArray(size: Int): Array<TranslatePageSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
