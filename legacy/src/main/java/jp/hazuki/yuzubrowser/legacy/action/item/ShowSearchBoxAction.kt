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
import android.widget.CheckBox
import android.widget.Spinner
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import java.io.IOException

class ShowSearchBoxAction : SingleAction, Parcelable {

    var openNewTabMode = 0
        private set
    var isReverse: Boolean = false
        private set

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_OPEN_NEW_TAB -> {
                        if (reader.peek() != JsonReader.Token.BOOLEAN) return
                        openNewTabMode =
                            if (reader.nextBoolean()) TAB_TYPE_NEW_TAB else TAB_TYPE_CURRENT
                    }
                    FIELD_REVERSE -> {
                        if (reader.peek() != JsonReader.Token.BOOLEAN) return
                        isReverse = reader.nextBoolean()
                    }
                    FIELD_TAB_TYPE -> {
                        if (reader.peek() != JsonReader.Token.NUMBER) return
                        openNewTabMode = reader.nextInt()
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
        writer.name(FIELD_TAB_TYPE)
        writer.value(openNewTabMode)
        writer.name(FIELD_REVERSE)
        writer.value(isReverse)
        writer.endObject()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(openNewTabMode)
        dest.writeInt(if (isReverse) 1 else 0)
    }

    private constructor(source: Parcel) : super(source) {
        openNewTabMode = source.readInt()
        isReverse = source.readInt() != 0
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_show_search_box, null)
        val spinner: Spinner = view.findViewById(R.id.tabTypeSpinner)
        val bottom = view.findViewById<CheckBox>(R.id.checkBox2)
        ArrayAdapter.createFromResource(
            context,
            R.array.action_show_search_list,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(openNewTabMode)
        }

        bottom.isChecked = isReverse
        AlertDialog.Builder(context)
            .setTitle(R.string.action_settings)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                openNewTabMode = spinner.selectedItemPosition
                isReverse = bottom.isChecked
            }
            .setNegativeButton(android.R.string.cancel, null)
                .show()
        return null
    }

    companion object {
        private const val FIELD_OPEN_NEW_TAB = "0"
        private const val FIELD_REVERSE = "1"
        private const val FIELD_TAB_TYPE = "2"

        const val TAB_TYPE_CURRENT = 0
        const val TAB_TYPE_NEW_TAB = 1
        const val TAB_TYPE_NEW_RIGHT_TAB = 2

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
