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

package jp.hazuki.yuzubrowser.ui.preference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import androidx.preference.Preference
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils
import jp.hazuki.yuzubrowser.ui.R
import java.util.*

class MultiListIntPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    private val mEntriesId: Int
    private val mMax: Int
    private var mValue: BooleanArray? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MultiListIntPreference)
        mEntriesId = a.getResourceId(R.styleable.MultiListIntPreference_android_entries, 0)
        mMax = a.getInt(R.styleable.MultiListIntPreference_android_max, -1)
        a.recycle()
    }

    private fun setValue(value: Int) {
        mValue = ArrayUtils.getBits(value, mMax)
        persistInt(value)
    }

    private fun setValue(value: BooleanArray?) {
        mValue = value
        persistInt(ArrayUtils.getBitsInt(value))
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a!!.getInt(index, -1)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        setValue(getPersistedInt(defaultValue as? Int ?: ArrayUtils.getBitsInt(mValue)))
    }

    class PrefernceDialog : YuzuPreferenceDialog() {

        override fun onPrepareDialogBuilder(builder: androidx.appcompat.app.AlertDialog.Builder?) {
            val pref = getParentPreference<MultiListIntPreference>()
            if (pref.mValue == null) {
                pref.mValue = BooleanArray(pref.mMax)
                Arrays.fill(pref.mValue!!, false)
            }

            builder!!.setMultiChoiceItems(pref.mEntriesId, pref.mValue) { _, which, isChecked -> pref.mValue!![which] = isChecked }
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            if (positiveResult) {
                val pref = getParentPreference<MultiListIntPreference>()
                if (pref.callChangeListener(pref.mValue)) {
                    pref.setValue(pref.mValue)
                }
            }
        }

        companion object {

            fun newInstance(preference: Preference): YuzuPreferenceDialog {
                return newInstance(PrefernceDialog(), preference)
            }
        }
    }
}
