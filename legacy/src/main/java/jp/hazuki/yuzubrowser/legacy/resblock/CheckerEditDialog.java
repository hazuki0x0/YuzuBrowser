/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.legacy.resblock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.resblock.checker.NormalChecker;
import jp.hazuki.yuzubrowser.legacy.resblock.data.EmptyImageData;
import jp.hazuki.yuzubrowser.legacy.resblock.data.EmptyStringData;

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
        final Spinner resTypeSpinner = view.findViewById(R.id.resTypeSpinner);
        final EditText urlEditText = view.findViewById(R.id.urlEditText);
        final CheckBox whiteCheckBox = view.findViewById(R.id.whiteCheckBox);

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
