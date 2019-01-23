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

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import jp.hazuki.yuzubrowser.ui.dialog.SeekBarDialog
import java.io.IOException

class VibrationSingleAction : SingleAction, Parcelable {
    var time = 100
        private set

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {
        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_NAME_TIME -> {
                        if (reader.peek() != JsonReader.Token.NUMBER) return
                        time = reader.nextInt()
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
        writer.name(FIELD_NAME_TIME)
        writer.value(time)
        writer.endObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(time)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        time = source.readInt()
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        SeekBarDialog(context)
                .setTitle(R.string.action_vibration_setting)
                .setPositiveButton(android.R.string.ok) { _, _, value -> time = value }
                .setSeekMin(1)
                .setSeekMax(3000)
                .setValue(time)
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return null
    }

    companion object {
        private const val FIELD_NAME_TIME = "0"

        @JvmField
        val CREATOR: Parcelable.Creator<VibrationSingleAction> = object : Parcelable.Creator<VibrationSingleAction> {
            override fun createFromParcel(source: Parcel): VibrationSingleAction {
                return VibrationSingleAction(source)
            }

            override fun newArray(size: Int): Array<VibrationSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
