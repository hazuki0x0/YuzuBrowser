package jp.hazuki.yuzubrowser.settings.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.takisoft.fix.support.v7.preference.EditTextPreference;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.preference.common.YuzuPreferenceDialog;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;

public class SearchUrlPreference extends EditTextPreference {

    private final CharSequence errorText;
    private AppCompatEditText mEditText;

    public SearchUrlPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        errorText = context.getText(R.string.pref_search_url_error);
        mEditText = new AppCompatEditText(context, attrs);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("%s")) {
                    mEditText.setError(null);
                } else {
                    mEditText.setError(errorText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static class PreferenceDialog extends YuzuPreferenceDialog {
        private AppCompatEditText mEditText;

        public static YuzuPreferenceDialog newInstance(Preference preference) {
            return newInstance(new PreferenceDialog(), preference);
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            SearchUrlPreference pref = getParentPreference();
            mEditText = pref.mEditText;

            // we can be reusing the EditText, so remove it from parent, if any
            ViewParent parent = mEditText.getParent();
            if (parent != null)
                ((ViewGroup) parent).removeView(this.mEditText);

            // set text and put cursor on the end
            String value = pref.getText();
            if (value != null) {
                mEditText.setText(value);
                mEditText.setSelection(value.length(), value.length());
            }

            // set padding
            int padding = DisplayUtils.convertDpToPx(getContext(), 16);
            mEditText.setPadding(padding, padding, padding, padding);
            builder.setView(mEditText);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog dialog = (AlertDialog) super.onCreateDialog(savedInstanceState);

            dialog.setOnShowListener(dialogInterface -> {
                AlertDialog alertDialog = (AlertDialog) dialogInterface;

                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (mEditText.getText().toString().contains("%s")) {
                        getDialog().dismiss();
                        onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
                    }
                });
            });

            return dialog;
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            SearchUrlPreference pref = getParentPreference();
            if (positiveResult) {
                String value = mEditText.getText().toString();
                if (pref.callChangeListener(value)) {
                    pref.setText(value);
                }
            }
        }
    }
}
