/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.ui.preference

import android.content.Context
import android.content.DialogInterface
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import androidx.preference.Preference
import jp.hazuki.yuzubrowser.ui.R

class StrToIntListPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    private val mEntriesId: Int
    private val mEntryValues: IntArray
    private var mClickedItemIndex = -1
    var value: Int = 0
        set(value) {
            field = value
            persistInt(value)
        }

    private val valueIndex: Int
        get() = findIndexOfValue(value)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StrToIntListPreference)
        val resources = context.resources
        mEntriesId = a.getResourceId(R.styleable.StrToIntListPreference_android_entries, 0)
        mEntryValues = resources.getIntArray(a.getResourceId(R.styleable.StrToIntListPreference_android_entryValues, 0))
        a.recycle()
    }

    private fun findIndexOfValue(value: Int): Int {
        return mEntryValues.indexOf(value)
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a!!.getInt(index, -1)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = defaultValue as? Int ?: getPersistedInt(value)
    }

    class PreferenceDialog : YuzuPreferenceDialog() {

        override fun onPrepareDialogBuilder(builder: androidx.appcompat.app.AlertDialog.Builder?) {
            val preference = preference as StrToIntListPreference
            preference.mClickedItemIndex = preference.valueIndex
            builder!!.setPositiveButton(null, null)
            builder.setSingleChoiceItems(preference.mEntriesId, preference.mClickedItemIndex) { dialog, which ->
                preference.mClickedItemIndex = which
                onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                dialog.dismiss()
            }
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            val preference = preference as StrToIntListPreference
            if (positiveResult && preference.mClickedItemIndex >= 0) {
                val value = preference.mEntryValues[preference.mClickedItemIndex]
                if (preference.callChangeListener(value)) {
                    preference.value = value
                }
            }
        }

        companion object {

            fun newInstance(preference: Preference): YuzuPreferenceDialog {
                return newInstance(PreferenceDialog(), preference)
            }
        }
    }
}
