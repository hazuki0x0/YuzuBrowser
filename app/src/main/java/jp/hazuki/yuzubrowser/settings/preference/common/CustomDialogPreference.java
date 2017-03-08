package jp.hazuki.yuzubrowser.settings.preference.common;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public abstract class CustomDialogPreference extends DialogPreference {
    public CustomDialogPreference(Context context) {
        this(context, null);
    }

    public CustomDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void show() {
        //showDialog(null);//NullPointerException at getPreferenceManager()

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        onPrepareDialogBuilder(builder);
        builder.show();
    }
}
