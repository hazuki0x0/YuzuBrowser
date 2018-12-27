package jp.hazuki.yuzubrowser.legacy.settings.preference.common;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.legacy.R;

public class IntListPreference extends DialogPreference {
    private final int[] mEntryValues;
    private int mValue;

    public IntListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IntListPreference);
        Resources resources = context.getResources();
        mEntryValues = resources.getIntArray(a.getResourceId(R.styleable.IntListPreference_android_entryValues, 0));
        a.recycle();
    }

    public void setValue(int value) {
        mValue = value;
        persistInt(value);
    }

    public int getValue() {
        return mValue;
    }

    protected int findIndexOfValue(int value) {
        for (int i = 0; i < mEntryValues.length; ++i) {
            if (mEntryValues[i] == value) return i;
        }
        return -1;
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, -1);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mValue) : (Integer) defaultValue);
    }

    public static class PreferenceDialog extends YuzuPreferenceDialog {

        private int mClickedItemIndex = -1;

        public static YuzuPreferenceDialog newInstance(Preference preference) {
            return newInstance(new PreferenceDialog(), preference);
        }

        @Override
        protected void onPrepareDialogBuilder(android.support.v7.app.AlertDialog.Builder builder) {
            IntListPreference pref = getParentPreference();
            mClickedItemIndex = pref.getValueIndex();
            builder.setPositiveButton(null, null);

            int length = pref.mEntryValues.length;
            String lists[] = new String[length];
            for (int i = 0; i < length; ++i) {
                lists[i] = String.valueOf(pref.mEntryValues[i]);
            }

            builder.setSingleChoiceItems(lists, mClickedItemIndex, (dialog, which) -> {
                mClickedItemIndex = which;
                this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            });
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            IntListPreference pref = getParentPreference();
            if (positiveResult && mClickedItemIndex >= 0) {
                int value = pref.mEntryValues[mClickedItemIndex];
                if (pref.callChangeListener(value)) {
                    pref.setValue(value);
                }
            }
        }
    }
}
