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

public class AdBlockItemDeleteDialog extends DialogFragment {
    private static final String ARG_INDEX = "index";
    private static final String ARG_ID = "id";
    private static final String ARG_ITEM = "item";

    private OnBlockItemDeleteListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pref_delete);
        builder.setMessage(getString(R.string.pref_ad_block_delete_confirm, getArguments().getString(ARG_ITEM)));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDelete(getArguments().getInt(ARG_INDEX), getArguments().getInt(ARG_ID));
            }
        });
        builder.setNegativeButton(android.R.string.no, null);
        return builder.create();
    }

    static AdBlockItemDeleteDialog newInstance(int index, int id, String item) {
        AdBlockItemDeleteDialog dialog = new AdBlockItemDeleteDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_INDEX, index);
        bundle.putInt(ARG_ID, id);
        bundle.putString(ARG_ITEM, item);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnBlockItemDeleteListener) getParentFragment();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    interface OnBlockItemDeleteListener {
        void onDelete(int index, int id);
    }
}
