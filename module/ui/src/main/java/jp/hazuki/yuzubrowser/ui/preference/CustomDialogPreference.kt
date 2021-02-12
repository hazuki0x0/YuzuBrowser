package jp.hazuki.yuzubrowser.ui.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;

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
