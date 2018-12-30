package jp.hazuki.yuzubrowser.ui.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;

public class AlertDialogPreference extends DialogPreference {
    private OnButtonClickListener mPositiveButtonListener;

    public AlertDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    public AlertDialogPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    public interface OnButtonClickListener {
        void onPositiveButtonClick();
    }

    public void setOnPositiveButtonListener(OnButtonClickListener l) {
        mPositiveButtonListener = l;
    }

    public static class PreferenceDialog extends YuzuPreferenceDialog {

        public static YuzuPreferenceDialog newInstance(Preference preference) {
            return newInstance(new PreferenceDialog(), preference);
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            OnButtonClickListener listener = ((AlertDialogPreference) getPreference()).mPositiveButtonListener;
            if (listener != null) {
                if (positiveResult)
                    listener.onPositiveButtonClick();
            }
        }
    }
}
