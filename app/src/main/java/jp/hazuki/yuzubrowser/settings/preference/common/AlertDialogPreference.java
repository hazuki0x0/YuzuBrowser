package jp.hazuki.yuzubrowser.settings.preference.common;

import android.content.Context;
import android.preference.DialogPreference;
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

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (mPositiveButtonListener != null) {
            if (positiveResult)
                mPositiveButtonListener.onPositiveButtonClick();
        }
        super.onDialogClosed(positiveResult);
    }
}
