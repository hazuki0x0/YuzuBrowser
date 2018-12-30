package jp.hazuki.yuzubrowser.ui.preference

import android.content.Context
import android.content.DialogInterface
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import androidx.preference.Preference
import jp.hazuki.yuzubrowser.ui.R

class IntListPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    private val mEntryValues: IntArray
    var value: Int = 0
        set(value) {
            field = value
            persistInt(value)
        }

    private val valueIndex: Int
        get() = findIndexOfValue(value)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.IntListPreference)
        val resources = context.resources
        mEntryValues = resources.getIntArray(a.getResourceId(R.styleable.IntListPreference_android_entryValues, 0))
        a.recycle()
    }

    protected fun findIndexOfValue(value: Int): Int {
        for (i in mEntryValues.indices) {
            if (mEntryValues[i] == value) return i
        }
        return -1
    }


    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a!!.getInt(index, -1)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = defaultValue as? Int ?: getPersistedInt(value)
    }

    class PreferenceDialog : YuzuPreferenceDialog() {

        private var mClickedItemIndex = -1

        override fun onPrepareDialogBuilder(builder: androidx.appcompat.app.AlertDialog.Builder?) {
            val pref = getParentPreference<IntListPreference>()
            mClickedItemIndex = pref.valueIndex
            builder!!.setPositiveButton(null, null)

            val length = pref.mEntryValues.size
            val lists = arrayOfNulls<String>(length)
            for (i in 0 until length) {
                lists[i] = pref.mEntryValues[i].toString()
            }

            builder.setSingleChoiceItems(lists, mClickedItemIndex) { dialog, which ->
                mClickedItemIndex = which
                this.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                dialog.dismiss()
            }
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            val pref = getParentPreference<IntListPreference>()
            if (positiveResult && mClickedItemIndex >= 0) {
                val value = pref.mEntryValues[mClickedItemIndex]
                if (pref.callChangeListener(value)) {
                    pref.value = value
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
