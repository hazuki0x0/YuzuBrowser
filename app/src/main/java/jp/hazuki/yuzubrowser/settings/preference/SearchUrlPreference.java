package jp.hazuki.yuzubrowser.settings.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import jp.hazuki.yuzubrowser.R;

public class SearchUrlPreference extends EditTextPreference {

    private final CharSequence errorText;
    private EditText mEditText;

    public SearchUrlPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        errorText = context.getText(R.string.pref_search_url_error);
    }

    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        super.onAddEditTextToDialogView(dialogView, editText);
        mEditText = editText;
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

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.getText().toString().contains("%s")) {
                    getDialog().dismiss();
                    SearchUrlPreference.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
                }
            }
        });
    }
}
