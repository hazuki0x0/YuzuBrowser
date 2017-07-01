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

package jp.hazuki.yuzubrowser.adblock.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import jp.hazuki.yuzubrowser.R;

public class AdBlockMenuDialog extends DialogFragment {
    private static final String ARG_INDEX = "index";
    private static final String ARG_ID = "id";

    private OnAdBlockMenuListener listener;

    static AdBlockMenuDialog newInstance(int index, int id) {
        AdBlockMenuDialog dialog = new AdBlockMenuDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_INDEX, index);
        bundle.putInt(ARG_ID, id);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.pref_ad_block_menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int index = getArguments().getInt(ARG_INDEX);
                int id = getArguments().getInt(ARG_ID);
                switch (which) {
                    case 0:
                        listener.onEdit(index, id);
                        break;
                    case 1:
                        listener.onAskDelete(index, id);
                        break;
                    case 2:
                        listener.onResetCount(index, id);
                        break;
                    case 3:
                        listener.startMultiSelect(index);
                        break;
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnAdBlockMenuListener) getParentFragment();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    interface OnAdBlockMenuListener {
        void onAskDelete(int index, int id);

        void onEdit(int index, int id);

        void onResetCount(int index, int id);

        void startMultiSelect(int index);
    }
}
