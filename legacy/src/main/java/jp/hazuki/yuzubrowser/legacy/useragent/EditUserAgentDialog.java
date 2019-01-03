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

package jp.hazuki.yuzubrowser.legacy.useragent;

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

public class EditUserAgentDialog extends DialogFragment {

    private static final String POS = "pos";
    private static final String NAME = "name";
    private static final String UA = "ua";

    public static EditUserAgentDialog newInstance() {
        return newInstance(-1, "", "");
    }

    public static EditUserAgentDialog newInstance(int pos, UserAgent userAgent) {
        return newInstance(pos, userAgent.getName(), userAgent.getUseragent());
    }

    private static EditUserAgentDialog newInstance(int pos, String name, String ua) {
        EditUserAgentDialog dialog = new EditUserAgentDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(POS, pos);
        bundle.putString(NAME, name);
        bundle.putString(UA, ua);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.useragent_add, null);
        final EditText nameEditText = view.findViewById(R.id.nameEditText);
        final EditText uaEditText = view.findViewById(R.id.uaEditText);

        nameEditText.setText(getArguments().getString(NAME));
        uaEditText.setText(getArguments().getString(UA));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(nameEditText.getText()))
                            return;

                        if (getParentFragment() instanceof OnEditedUserAgent) {
                            ((OnEditedUserAgent) getParentFragment())
                                    .onEdited(getArguments().getInt(POS),
                                            nameEditText.getText().toString()
                                                    .trim().replace("\n", "")
                                            , uaEditText.getText().toString());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }


    public interface OnEditedUserAgent {
        void onEdited(int position, String name, String ua);
    }
}
