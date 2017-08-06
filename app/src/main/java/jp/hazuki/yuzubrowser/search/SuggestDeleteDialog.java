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

package jp.hazuki.yuzubrowser.search;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import jp.hazuki.yuzubrowser.R;

public class SuggestDeleteDialog extends DialogFragment {
    private static final String ARG_QUERY = "query";

    private OnDeleteQuery deleteQuery;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_history)
                .setMessage(R.string.confirm_delete_history)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteQuery.onDelete(getArguments().getString(ARG_QUERY));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        deleteQuery = (OnDeleteQuery) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        deleteQuery = null;
    }

    public static SuggestDeleteDialog newInstance(String query) {
        SuggestDeleteDialog dialog = new SuggestDeleteDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_QUERY, query);
        dialog.setArguments(bundle);
        return dialog;
    }

    interface OnDeleteQuery {
        void onDelete(String query);
    }
}
