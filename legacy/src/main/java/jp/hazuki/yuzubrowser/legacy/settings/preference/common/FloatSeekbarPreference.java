package jp.hazuki.yuzubrowser.legacy.settings.preference.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.legacy.R;

public class FloatSeekbarPreference extends DialogPreference {
    private final int mSeekMin;
    private final int mSeekMax;
    private final int mDenominator;
    private float mValue;

    public FloatSeekbarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekbarPreference);
        mSeekMin = a.getInt(R.styleable.SeekbarPreference_seekMin, 0);
        mSeekMax = a.getInt(R.styleable.SeekbarPreference_seekMax, 100);
        a.recycle();

        a = context.obtainStyledAttributes(attrs, R.styleable.FloatSeekbarPreference);
        mDenominator = a.getInt(R.styleable.FloatSeekbarPreference_denominator, 0);
        a.recycle();
    }

    public void setValue(float value) {
        mValue = value;
        persistFloat(value);
    }

    public float getValue() {
        return mValue;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getFloat(index, -1);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedFloat(mValue) : (Float) defaultValue);
    }

    public static class PreferenceDialog extends YuzuPreferenceDialog {

        private int mTempValue;

        public static YuzuPreferenceDialog newInstance(Preference preference) {
            return newInstance(new PreferenceDialog(), preference);
        }

        @Override
        protected View onCreateDialogView(Context context) {
            final FloatSeekbarPreference pref = (FloatSeekbarPreference) getPreference();
            final View view = LayoutInflater.from(getContext()).inflate(R.layout.seekbar_preference, null);
            final TextView textView = view.findViewById(R.id.countTextView);
            final SeekBar seekbar = view.findViewById(R.id.seekBar);

            seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mTempValue = seekBar.getProgress();
                    textView.setText(String.valueOf((mTempValue + pref.mSeekMin) / (float) pref.mDenominator));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textView.setText(String.valueOf((progress + pref.mSeekMin) / (float) pref.mDenominator));
                }
            });
            seekbar.setMax(pref.mSeekMax - pref.mSeekMin);
            seekbar.setProgress(mTempValue = (int) (pref.mValue * pref.mDenominator) - pref.mSeekMin);

            view.findViewById(R.id.prevImageButton).setOnClickListener(v -> {
                if (mTempValue > 0)
                    seekbar.setProgress(--mTempValue);
            });
            view.findViewById(R.id.nextImageButton).setOnClickListener(v -> {
                if (mTempValue < pref.mSeekMax - pref.mSeekMin)
                    seekbar.setProgress(++mTempValue);
            });

            textView.setOnClickListener(v -> {
                final EditText edittext = new EditText(getContext());
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                edittext.setText(String.valueOf((mTempValue + pref.mSeekMin) / (float) pref.mDenominator));

                new AlertDialog.Builder(getContext())
                        .setView(edittext)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            try {
                                int value = (int) (Float.parseFloat(edittext.getText().toString()) * pref.mDenominator);
                                if (value >= pref.mSeekMin && value <= pref.mSeekMax)
                                    seekbar.setProgress(mTempValue = value - pref.mSeekMin);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            });
            return view;
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            FloatSeekbarPreference pref = (FloatSeekbarPreference) getPreference();
            if (positiveResult) {
                if (pref.callChangeListener(mTempValue + pref.mSeekMin)) {
                    pref.setValue((mTempValue + pref.mSeekMin) / (float) pref.mDenominator);
                }
            }
        }
    }
}
