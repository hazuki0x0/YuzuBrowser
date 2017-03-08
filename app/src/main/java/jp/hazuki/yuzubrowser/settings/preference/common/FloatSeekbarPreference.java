package jp.hazuki.yuzubrowser.settings.preference.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;

public class FloatSeekbarPreference extends DialogPreference {
    private final int mSeekMin;
    private final int mSeekMax;
    private final int mDenominator;
    private float mValue;
    private int mTempValue;

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
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        final View view = LayoutInflater.from(getContext()).inflate(R.layout.seekbar_preference, null);
        final TextView textView = (TextView) view.findViewById(R.id.countTextView);
        final SeekBar seekbar = (SeekBar) view.findViewById(R.id.seekBar);

        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mTempValue = seekBar.getProgress();
                textView.setText(String.valueOf((mTempValue + mSeekMin) / (float) mDenominator));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf((progress + mSeekMin) / (float) mDenominator));
            }
        });
        seekbar.setMax(mSeekMax - mSeekMin);
        seekbar.setProgress(mTempValue = (int) (mValue * mDenominator) - mSeekMin);

        view.findViewById(R.id.prevImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTempValue > 0)
                    seekbar.setProgress(--mTempValue);
            }
        });
        view.findViewById(R.id.nextImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTempValue < mSeekMax - mSeekMin)
                    seekbar.setProgress(++mTempValue);
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edittext = new EditText(getContext());
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                edittext.setText(String.valueOf((mTempValue + mSeekMin) / (float) mDenominator));

                new AlertDialog.Builder(getContext())
                        .setView(edittext)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    int value = (int) (Float.parseFloat(edittext.getText().toString()) * mDenominator);
                                    if (value >= mSeekMin && value <= mSeekMax)
                                        seekbar.setProgress(mTempValue = value - mSeekMin);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        builder.setView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            if (callChangeListener(mTempValue + mSeekMin)) {
                setValue((mTempValue + mSeekMin) / (float) mDenominator);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getFloat(index, -1);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedFloat(mValue) : (Float) defaultValue);
    }
}
