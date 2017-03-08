package jp.hazuki.yuzubrowser.utils.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * Created by hazuki on 17/01/25.
 */

public class ProgressDialogFragment extends DialogFragment {

    private ProgressDialog dialog;

    private static final String MESSAGE = "mes";

    public static ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putCharSequence(MESSAGE, message);
        fragment.setArguments(bundle);
        return fragment;
    }

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
