package jp.hazuki.yuzubrowser.utils.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragmentCompat extends DialogFragment {

    private ProgressDialog dialog;

    private static final String MESSAGE = "mes";

    public static ProgressDialogFragmentCompat newInstance(String message) {
        ProgressDialogFragmentCompat fragment = new ProgressDialogFragmentCompat();
        Bundle bundle = new Bundle();
        bundle.putCharSequence(MESSAGE, message);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getArguments().getCharSequence(MESSAGE));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setCancelable(false);

        return dialog;
    }

    @Override
    public void dismiss() {
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public Dialog getDialog() {
        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dialog = null;
    }
}
