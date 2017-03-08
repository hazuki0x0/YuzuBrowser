package jp.hazuki.yuzubrowser.settings.preference.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;

public class StrToIntListPreference extends DialogPreference {
    private final int mEntriesId;
    private final int[] mEntryValues;
    private int mClickedItemIndex = -1;
    private int mValue;

    public StrToIntListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrToIntListPreference);
        Resources resources = context.getResources();
        mEntriesId = a.getResourceId(R.styleable.StrToIntListPreference_android_entries, 0);
        mEntryValues = resources.getIntArray(a.getResourceId(R.styleable.StrToIntListPreference_android_entryValues, 0));
        a.recycle();
    }

    private void setValue(int value) {
        mValue = value;
        persistInt(value);
    }

    @SuppressWarnings("unused")
    private int getValue() {
        return mValue;
    }

    private int findIndexOfValue(int value) {
        return ArrayUtils.findIndexOfValue(value, mEntryValues);
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        mClickedItemIndex = getValueIndex();
        builder.setPositiveButton(null, null);
        builder.setSingleChoiceItems(mEntriesId, mClickedItemIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mClickedItemIndex = which;
                StrToIntListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
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
