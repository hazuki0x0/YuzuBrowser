package jp.hazuki.yuzubrowser.ui.preference

import android.app.AlertDialog
import android.content.Context
import android.content.res.TypedArray
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.preference.DialogPreference
import androidx.preference.Preference
import jp.hazuki.yuzubrowser.ui.R

class FloatSeekbarPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    private val mSeekMin: Int
    private val mSeekMax: Int
    private val mDenominator: Int
    var value: Float = 0.toFloat()
        set(value) {
            field = value
            persistFloat(value)
        }

    init {
        var a = context.obtainStyledAttributes(attrs, R.styleable.SeekbarPreference)
        mSeekMin = a.getInt(R.styleable.SeekbarPreference_seekMin, 0)
        mSeekMax = a.getInt(R.styleable.SeekbarPreference_seekMax, 100)
        a.recycle()

        a = context.obtainStyledAttributes(attrs, R.styleable.FloatSeekbarPreference)
        mDenominator = a.getInt(R.styleable.FloatSeekbarPreference_denominator, 0)
        a.recycle()
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a!!.getFloat(index, -1f)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = defaultValue as? Float ?: getPersistedFloat(value)
    }

    class PreferenceDialog : YuzuPreferenceDialog() {

        private var mTempValue: Int = 0

        override fun onCreateDialogView(context: Context): View {
            val pref = preference as FloatSeekbarPreference
            val view = LayoutInflater.from(getContext()).inflate(R.layout.seekbar_preference, null)
            val textView = view.findViewById<TextView>(R.id.countTextView)
            val seekbar = view.findViewById<SeekBar>(R.id.seekBar)

            seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    mTempValue = seekBar.progress
                    textView.text = ((mTempValue + pref.mSeekMin) / pref.mDenominator.toFloat()).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    textView.text = ((progress + pref.mSeekMin) / pref.mDenominator.toFloat()).toString()
                }
            })
            seekbar.max = pref.mSeekMax - pref.mSeekMin
            mTempValue = (pref.value * pref.mDenominator).toInt() - pref.mSeekMin
            seekbar.progress = mTempValue

            view.findViewById<View>(R.id.prevImageButton).setOnClickListener {
                if (mTempValue > 0)
                    seekbar.progress = --mTempValue
            }
            view.findViewById<View>(R.id.nextImageButton).setOnClickListener {
                if (mTempValue < pref.mSeekMax - pref.mSeekMin)
                    seekbar.progress = ++mTempValue
            }

            textView.setOnClickListener {
                val edittext = EditText(getContext())
                edittext.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                edittext.setText(((mTempValue + pref.mSeekMin) / pref.mDenominator.toFloat()).toString())

                AlertDialog.Builder(getContext())
                        .setView(edittext)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            try {
                                val value = (java.lang.Float.parseFloat(edittext.text.toString()) * pref.mDenominator).toInt()
                                if (value >= pref.mSeekMin && value <= pref.mSeekMax)
                                    mTempValue = value - pref.mSeekMin
                                seekbar.progress = mTempValue
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
            }
            return view
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            val pref = preference as FloatSeekbarPreference
            if (positiveResult) {
                if (pref.callChangeListener(mTempValue + pref.mSeekMin)) {
                    pref.value = (mTempValue + pref.mSeekMin) / pref.mDenominator.toFloat()
                }
            }
        }

        companion object {

            fun newInstance(preference: Preference): YuzuPreferenceDialog {
                return YuzuPreferenceDialog.newInstance(PreferenceDialog(), preference)
            }
        }
    }
}
