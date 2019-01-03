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

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import jp.hazuki.yuzubrowser.legacy.R;

public class SelectActionDialog extends DialogFragment {

    private static final String POS = "pos";
    private static final String UA = "ua";

    public static final int EDIT = 0;
    public static final int DELETE = 1;

    @IntDef({EDIT, DELETE})
    public @interface ActionMode {
    }

    public static SelectActionDialog newInstance(int position, UserAgent ua) {
        SelectActionDialog dialog = new SelectActionDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(POS, position);
        bundle.putSerializable(UA, ua);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final UserAgent ua = (UserAgent) getArguments().getSerializable(UA);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(ua.getName())
                .setItems(R.array.edit_user_agent, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getParentFragment() instanceof OnActionSelect) {
                            ((OnActionSelect) getParentFragment())
                                    .onActionSelected(which, getArguments().getInt(POS), ua);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

    public interface OnActionSelect {
        void onActionSelected(@ActionMode int mode, int position, UserAgent userAgent);
    }
}
