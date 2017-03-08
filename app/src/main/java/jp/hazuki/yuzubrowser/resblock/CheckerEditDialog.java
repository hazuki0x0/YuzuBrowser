package jp.hazuki.yuzubrowser.resblock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.resblock.checker.NormalChecker;
import jp.hazuki.yuzubrowser.resblock.data.EmptyImageData;
import jp.hazuki.yuzubrowser.resblock.data.EmptyStringData;

/**
 * Created by hazuki on 17/02/28.
 */

public class CheckerEditDialog extends DialogFragment {
    private static final String INDEX = "index";
    private static final String CHECKER = "checker";

    public static DialogFragment newInstance(int index, NormalChecker checker) {
        DialogFragment dialog = new CheckerEditDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);
        bundle.putSerializable(CHECKER, checker);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.resource_block_add_dialog, null);
        final Spinner resTypeSpinner = (Spinner) view.findViewById(R.id.resTypeSpinner);
        final EditText urlEditText = (EditText) view.findViewById(R.id.urlEditText);
        final CheckBox whiteCheckBox = (CheckBox) view.findViewById(R.id.whiteCheckBox);

        NormalChecker checker = (NormalChecker) getArguments().getSerializable(CHECKER);
        if (checker != null) {
            resTypeSpinner.setSelection(checker.getAction().getTypeId());
            urlEditText.setText(checker.getUrl());
            whiteCheckBox.setChecked(checker.isWhite());
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.resblock_edit)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ResourceData data;
                        ResourceChecker new_checker;
                        switch (resTypeSpinner.getSelectedItemPosition()) {
                            case ResourceData.EMPTY_STRING_DATA:
                                data = new EmptyStringData();
                                break;
                            case ResourceData.EMPTY_IMAGE_DATA:
                                data = new EmptyImageData();
                                break;
                            default:
                                throw new IllegalStateException("unknown selection:" + resTypeSpinner.getSelectedItemPosition());
                        }
                        int index = getArguments().getInt(INDEX, -1);
                        new_checker = new NormalChecker(data, urlEditText.getText().toString(), whiteCheckBox.isChecked());
                        if (getParentFragment() instanceof OnCheckerEdit) {
                            ((OnCheckerEdit) getParentFragment()).onCheckerEdited(index, new_checker);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    interface OnCheckerEdit {
        void onCheckerEdited(int index, ResourceChecker checker);
    }
}
