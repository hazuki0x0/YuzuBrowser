package jp.hazuki.yuzubrowser.settings.preference.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

public abstract class CustomDialogPreference extends DialogPreference {
    public CustomDialogPreference(Context context) {
        this(context, null);
    }

    public CustomDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void show(FragmentManager manager) {
        crateCustomDialog().show(manager, getKey());
    }

    @NonNull
    protected abstract CustomDialogFragment crateCustomDialog();

    public static class CustomDialogFragment extends DialogFragment implements TargetFragment {

        @Override
        public Preference findPreference(CharSequence key) {
            DialogPreference.TargetFragment fragment =
                    (DialogPreference.TargetFragment) getTargetFragment();
            return fragment.findPreference(key);
        }
    }
}
