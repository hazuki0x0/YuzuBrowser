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
import android.widget.Switch
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo

class WithToastAction private constructor(id: Int) : SingleAction(id), Parcelable {

    companion object {
        private const val FIELD_SHOW_TOAST = "0"

        @JvmField
        val CREATOR: Parcelable.Creator<WithToastAction> = object : Parcelable.Creator<WithToastAction> {
            override fun createFromParcel(source: Parcel): WithToastAction {
                return WithToastAction(source)
            }

            override fun newArray(size: Int): Array<WithToastAction?> {
                return arrayOfNulls(size)
            }
        }
    }

    var showToast = false
        private set

    constructor(id: Int, reader: JsonReader?) : this(id) {
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_SHOW_TOAST -> {
                        if (reader.peek() != JsonReader.Token.BOOLEAN) return
                        showToast = reader.nextBoolean()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        }
    }

    override fun writeIdAndData(writer: JsonWriter) {
        writer.value(id)
        writer.beginObject()
        writer.name(FIELD_SHOW_TOAST)
        writer.value(showToast)
        writer.endObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeByte(if (showToast) 1 else 0)
    }

    private constructor(source: Parcel) : this(source.readInt()) {
        showToast = source.readByte() != 0.toByte()
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_with_toast, null)
        val switch: Switch = view.findViewById(R.id.showToastSwitch)

        switch.isChecked = showToast
        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    showToast = switch.isChecked
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        return null
    }
}