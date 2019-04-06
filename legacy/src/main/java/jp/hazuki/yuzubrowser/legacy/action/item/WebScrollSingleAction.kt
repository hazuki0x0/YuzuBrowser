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
import android.content.Context
import android.hardware.SensorManager
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewConfiguration
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.core.utility.extensions.density
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import jp.hazuki.yuzubrowser.webview.CustomWebView
import java.io.IOException

class WebScrollSingleAction : SingleAction, Parcelable {
    private var mType = TYPE_FLING
    private var mX: Int = 0
    private var mY: Int = 0

    val iconResourceId: Int
        get() = when {
            mX > 0 -> when {
                mY > 0 -> R.drawable.ic_arrow_down_right_white_24dp
                mY < 0 -> R.drawable.ic_arrow_up_right_white_24px
                else -> R.drawable.ic_arrow_forward_white_24dp
            }
            mX < 0 -> when {
                mY > 0 -> R.drawable.ic_arrow_down_left_24dp
                mY < 0 -> R.drawable.ic_arrow_up_left_white_24px
                else -> R.drawable.ic_arrow_back_white_24dp
            }
            else -> when {
                mY > 0 -> R.drawable.ic_arrow_downward_white_24dp
                mY < 0 -> R.drawable.ic_arrow_upward_white_24dp
                else -> -1
            }
        }

    @Throws(IOException::class)
    constructor(id: Int, reader: JsonReader?) : super(id) {

        if (reader != null) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return
            reader.beginObject()
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.NAME) return
                when (reader.nextName()) {
                    FIELD_NAME_TYPE -> mType = reader.nextInt()
                    FIELD_NAME_X -> mX = reader.nextInt()
                    FIELD_NAME_Y -> mY = reader.nextInt()
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
        writer.name(FIELD_NAME_TYPE)
        writer.value(mType)
        writer.name(FIELD_NAME_X)
        writer.value(mX)
        writer.name(FIELD_NAME_Y)
        writer.value(mY)
        writer.endObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(mType)
        dest.writeInt(mX)
        dest.writeInt(mY)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        mType = source.readInt()
        mX = source.readInt()
        mY = source.readInt()
    }

    override fun showMainPreference(context: ActionActivity): StartActivityInfo? {
        return showSubPreference(context)
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        val view = View.inflate(context, R.layout.action_web_scroll_setting, null)
        val typeSpinner = view.findViewById<Spinner>(R.id.typeSpinner)
        val editTextX = view.findViewById<EditText>(R.id.editTextX)
        val editTextY = view.findViewById<EditText>(R.id.editTextY)

        val res = context.resources
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, res.getStringArray(R.array.action_web_scroll_type_list))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter
        val values = res.getIntArray(R.array.action_web_scroll_type_values)
        var current = ArrayUtils.findIndexOfValue(mType, values)
        if (current < 0)
            current = TYPE_FLING
        typeSpinner.setSelection(current)

        editTextX.setText(mX.toString())
        editTextY.setText(mY.toString())

        AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    mType = values[typeSpinner.selectedItemPosition]

                    var x = 0
                    var y = 0

                    try {
                        x = Integer.parseInt(editTextX.text.toString())
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }

                    try {
                        y = Integer.parseInt(editTextY.text.toString())
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }

                    if (x == 0 && y == 0) {
                        Toast.makeText(context.applicationContext, R.string.action_web_scroll_x_y_zero, Toast.LENGTH_SHORT).show()
                        showSubPreference(context)
                        return@setPositiveButton
                    }

                    mX = x
                    mY = y
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return null
    }

    fun scrollWebView(context: Context, web: CustomWebView) {
        when (mType) {
            TYPE_FAST -> web.scrollBy(mX, mY)
            TYPE_FLING -> web.flingScroll(makeFlingValue(context, mX), makeFlingValue(context, mY))
            else -> throw RuntimeException("Unknown type : " + mType)
        }
    }

    private fun makeFlingValue(context: Context, d: Int): Int {
        val decelerationLate = (Math.log(0.78) / Math.log(0.9)).toFloat()
        val physicalCoef = SensorManager.GRAVITY_EARTH * 39.37f * context.density * 160.0f * 0.84f
        val flingFliction = ViewConfiguration.getScrollFriction()
        return (Integer.signum(d) * Math.round(Math.exp(Math.log((Math.abs(d) / (flingFliction * physicalCoef)).toDouble()) / decelerationLate * (decelerationLate - 1.0)) / 0.35f * (flingFliction * physicalCoef))).toInt()
    }

    companion object {
        private const val FIELD_NAME_TYPE = "0"
        private const val FIELD_NAME_X = "1"
        private const val FIELD_NAME_Y = "2"
        private const val TYPE_FAST = 0
        private const val TYPE_FLING = 1

        @JvmField
        val CREATOR: Parcelable.Creator<WebScrollSingleAction> = object : Parcelable.Creator<WebScrollSingleAction> {
            override fun createFromParcel(source: Parcel): WebScrollSingleAction {
                return WebScrollSingleAction(source)
            }

            override fun newArray(size: Int): Array<WebScrollSingleAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
