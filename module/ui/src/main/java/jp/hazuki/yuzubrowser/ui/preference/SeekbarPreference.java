package jp.hazuki.yuzubrowser.ui.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import jp.hazuki.yuzubrowser.ui.R;

public class SeekbarPreference extends DialogPreference {
    private final SeekBarPreferenceController mController;

    public SeekbarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mController = new SeekBarPreferenceController(getContext());
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekbarPreference);
        mController.setSeekMin(a.getInt(R.styleable.SeekbarPreference_seekMin, 0));
        mController.setSeekMax(a.getInt(R.styleable.SeekbarPreference_seekMax, 100));
        mController.setComment(a.getString(R.styleable.SeekbarPreference_comment));
        a.recycle();
    }

    public void setValue(int value) {
        mController.setValue(value);
        persistInt(value);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, -1);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mController.getValue()) : (Integer) defaultValue);
    }

    public static class PreferenceDialog extends YuzuPreferenceDialog {

        public static YuzuPreferenceDialog newInstance(Preference preference) {
            return newInstance(new PreferenceDialog(), preference);
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            ((SeekbarPreference) getParentPreference()).mController.onPrepareDialogBuilder(builder);
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            SeekbarPreference pref = getParentPreference();
            if (positiveResult) {
                int value = pref.mController.getCurrentValue();
                if (pref.callChangeListener(value)) {
                    pref.setValue(value);
                }
            }
        }
    }
}
