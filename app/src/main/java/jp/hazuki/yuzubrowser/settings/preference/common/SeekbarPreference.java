package jp.hazuki.yuzubrowser.settings.preference.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.R;

public class SeekbarPreference extends DialogPreference {
    private final SeekBarPreferenceController mController;

    public SeekbarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mController = new SeekBarPreferenceController(getContext());
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekbarPreference);
        mController.setSeekMin(a.getInt(R.styleable.SeekbarPreference_seekMin, 0));
        mController.setSeekMax(a.getInt(R.styleable.SeekbarPreference_seekMax, 100));
        a.recycle();
    }

    public void setValue(int value) {
        mController.setValue(value);
        persistInt(value);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        mController.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            int value = mController.getCurrentValue();
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
        setValue(restoreValue ? getPersistedInt(mController.getValue()) : (Integer) defaultValue);
    }
}
