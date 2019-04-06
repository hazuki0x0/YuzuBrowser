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

package jp.hazuki.yuzubrowser.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DeleteDialogCompat extends DialogFragment {

    private static final String POS = "pos";
    private static final String TITLE = "title";
    private static final String MES = "mes";

    private boolean canOkPress = true;

    public static DeleteDialogCompat newInstance(Context context, int title, int message, int pos) {
        return newInstance(context.getString(title), context.getString(message), pos);
    }

    public static DeleteDialogCompat newInstance(String title, String message, int pos) {
        DeleteDialogCompat deleteWebTextEncodeDialog = new DeleteDialogCompat();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE, title);
        bundle.putString(MES, message);
        bundle.putInt(POS, pos);
        deleteWebTextEncodeDialog.setArguments(bundle);
        return deleteWebTextEncodeDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(TITLE))
                .setMessage(getArguments().getString(MES))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (canOkPress) {
                            canOkPress = false;
                            if (getParentFragment() instanceof OnDelete) {
                                ((OnDelete) getParentFragment()).onDelete(getArguments().getInt(POS));
                            }
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
