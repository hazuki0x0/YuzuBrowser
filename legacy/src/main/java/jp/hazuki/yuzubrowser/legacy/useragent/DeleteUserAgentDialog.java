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

package jp.hazuki.yuzubrowser.legacy.useragent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import jp.hazuki.yuzubrowser.legacy.R;

public class DeleteUserAgentDialog extends DialogFragment {

    private static final String POS = "pos";

    public static DeleteUserAgentDialog newInstance(int pos) {
        DeleteUserAgentDialog deleteUserAgentDialog = new DeleteUserAgentDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(POS, pos);
        deleteUserAgentDialog.setArguments(bundle);
        return deleteUserAgentDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_ua)
                .setMessage(R.string.delete_ua_confirm)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getParentFragment() instanceof OnDelete) {
                            ((OnDelete) getParentFragment()).onDelete(getArguments().getInt(POS));
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    public interface OnDelete {
        void onDelete(int position);
    }
}
