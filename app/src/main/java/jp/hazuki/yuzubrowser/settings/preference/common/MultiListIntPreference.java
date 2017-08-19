package jp.hazuki.yuzubrowser.settings.preference.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
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
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, -1);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(ArrayUtils.getBitsInt(mValue)) : (Integer) defaultValue);
    }

    public static class PrefernceDialog extends YuzuPreferenceDialog {

        public static YuzuPreferenceDialog newInstance(Preference preference) {
            return newInstance(new PrefernceDialog(), preference);
        }

        @Override
        protected void onPrepareDialogBuilder(android.support.v7.app.AlertDialog.Builder builder) {
            MultiListIntPreference pref = getParentPreference();
            if (pref.mValue == null) {
                pref.mValue = new boolean[pref.mMax];
                Arrays.fill(pref.mValue, false);
            }

            builder.setMultiChoiceItems(pref.mEntriesId, pref.mValue, (dialog, which, isChecked) -> pref.mValue[which] = isChecked);
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            if (positiveResult) {
                MultiListIntPreference pref = getParentPreference();
                if (pref.callChangeListener(pref.mValue)) {
                    pref.setValue(pref.mValue);
                }
            }
        }
    }
}
