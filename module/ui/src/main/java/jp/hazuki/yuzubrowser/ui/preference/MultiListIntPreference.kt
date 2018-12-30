package jp.hazuki.yuzubrowser.ui.preference

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.preference.DialogPreference
import android.support.v7.preference.Preference
import android.util.AttributeSet
import jp.hazuki.utility.utils.ArrayUtils
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
        setValue(defaultValue as? Int ?: ArrayUtils.getBitsInt(mValue))
    }

    class PrefernceDialog : YuzuPreferenceDialog() {

        override fun onPrepareDialogBuilder(builder: android.support.v7.app.AlertDialog.Builder?) {
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
