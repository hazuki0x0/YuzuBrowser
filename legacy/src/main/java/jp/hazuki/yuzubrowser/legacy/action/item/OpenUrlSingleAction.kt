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
import android.widget.EditText
import android.widget.Spinner
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.legacy.browser.BrowserManager
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import java.io.IOException

class OpenUrlSingleAction : SingleAction, Parcelable {
    var url = ""
        private set
    var targetTab = BrowserManager.LOAD_URL_TAB_CURRENT
        private set

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_NAME_URL -> {
                        if (reader.peek() != JsonReader.Token.STRING) return
                        url = reader.nextString()
                    }
                    FIELD_NAME_TARGET_TAB -> {
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
        writer.name(FIELD_NAME_URL)
        writer.value(url)
        writer.name(FIELD_NAME_TARGET_TAB)
        writer.value(targetTab)
        writer.endObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(url)
        dest.writeInt(targetTab)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        url = source.readString()!!
        targetTab = source.readInt()
    }

    override fun showMainPreference(context: ActionActivity): StartActivityInfo? {
        return showSubPreference(context)
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_open_url_setting, null)
        val urlEditText = view.findViewById<EditText>(R.id.urlEditText)
        val tabSpinner = view.findViewById<Spinner>(R.id.tabSpinner)

        urlEditText.setText(url)

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, context.resources.getStringArray(R.array.pref_newtab_list))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tabSpinner.adapter = adapter
        val targetValues = context.resources.getIntArray(R.array.pref_newtab_values)
        tabSpinner.setSelection(ArrayUtils.findIndexOfValue(targetTab, targetValues))

        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    url = urlEditText.text.toString()
                    targetTab = targetValues[tabSpinner.selectedItemPosition]
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return null
    }

    companion object {
        private const val FIELD_NAME_URL = "0"
        private const val FIELD_NAME_TARGET_TAB = "1"

        @JvmField
        val CREATOR: Parcelable.Creator<OpenUrlSingleAction> = object : Parcelable.Creator<OpenUrlSingleAction> {
            override fun createFromParcel(source: Parcel): OpenUrlSingleAction {
                return OpenUrlSingleAction(source)
            }

            override fun newArray(size: Int): Array<OpenUrlSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
