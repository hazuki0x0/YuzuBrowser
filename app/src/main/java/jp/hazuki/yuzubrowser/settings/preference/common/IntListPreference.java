package jp.hazuki.yuzubrowser.settings.preference.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.R;

public class IntListPreference extends DialogPreference {
    private final int[] mEntryValues;
    private int mClickedItemIndex = -1;
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
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        mClickedItemIndex = getValueIndex();
        builder.setPositiveButton(null, null);

        int length = mEntryValues.length;
        String lists[] = new String[length];
        for (int i = 0; i < length; ++i) {
            lists[i] = String.valueOf(mEntryValues[i]);
        }

        builder.setSingleChoiceItems(lists, mClickedItemIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mClickedItemIndex = which;
                IntListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult && mClickedItemIndex >= 0) {
            int value = mEntryValues[mClickedItemIndex];
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, -1);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mValue) : (Integer) defaultValue);
    }
}
