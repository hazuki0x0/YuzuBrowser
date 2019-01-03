/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.webencode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import jp.hazuki.yuzubrowser.legacy.R;

public class EditWebTextEncodeDialog extends DialogFragment {

    private static final String POS = "pos";
    private static final String ENCODING = "enc";

    public static EditWebTextEncodeDialog newInstance() {
        return newInstance(-1, "");
    }

    public static EditWebTextEncodeDialog newInstance(int pos, WebTextEncode encode) {
        return newInstance(pos, encode.getEncoding());
    }

    private static EditWebTextEncodeDialog newInstance(int pos, String encoding) {
        EditWebTextEncodeDialog dialog = new EditWebTextEncodeDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(POS, pos);
        bundle.putString(ENCODING, encoding);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.webencode_add, null);
        final EditText encodeEditText = view.findViewById(R.id.encodeEditText);

        encodeEditText.setText(getArguments().getString(ENCODING));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(encodeEditText.getText()))
                            return;

                        if (getParentFragment() instanceof OnEditedWebTextEncode) {
                            ((OnEditedWebTextEncode) getParentFragment())
                                    .onEdited(getArguments().getInt(POS), encodeEditText.getText().toString());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }


    public interface OnEditedWebTextEncode {
        void onEdited(int position, String name);
    }
}
