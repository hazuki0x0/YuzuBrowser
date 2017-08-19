package jp.hazuki.yuzubrowser.settings.preference.common;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

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
