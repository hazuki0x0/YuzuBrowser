package jp.hazuki.yuzubrowser.settings.preference.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import java.util.Arrays;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;

public class MultiListIntPreference extends DialogPreference {
    private final int mEntriesId;
    private final int mMax;
    private boolean mValue[];

    public MultiListIntPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiListIntPreference);
        mEntriesId = a.getResourceId(R.styleable.MultiListIntPreference_android_entries, 0);
        mMax = a.getInt(R.styleable.MultiListIntPreference_android_max, -1);
        a.recycle();
    }

    private void setValue(int value) {
        mValue = ArrayUtils.getBits(value, mMax);
        persistInt(value);
    }

    private void setValue(boolean value[]) {
        mValue = value;
        persistInt(ArrayUtils.getBitsInt(value));
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        if (mValue == null) {
            mValue = new boolean[mMax];
            Arrays.fill(mValue, false);
        }

        builder.setMultiChoiceItems(mEntriesId, mValue, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                mValue[which] = isChecked;
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            if (callChangeListener(mValue)) {
                setValue(mValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, -1);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(ArrayUtils.getBitsInt(mValue)) : (Integer) defaultValue);
    }
}
