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
import android.text.InputType;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.adblock.AdBlock;
import jp.hazuki.yuzubrowser.utils.extensions.ContextExtensionsKt;

public class AdBlockEditDialog extends DialogFragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_INDEX = "index";
    private static final String ARG_ID = "id";
    private static final String ARG_TEXT = "text";

    private AdBlockEditDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText editText = new EditText(getActivity());
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        float density = ContextExtensionsKt.getDensity(getActivity());
        int marginWidth = (int) (4 * density + 0.5f);
        int marginHeight = (int) (16 * density + 0.5f);
        layoutParams.setMargins(marginWidth, marginHeight, marginWidth, marginHeight);
        editText.setLayoutParams(layoutParams);
        editText.setId(android.R.id.edit);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        String text = getArguments().getString(ARG_TEXT);
        if (!TextUtils.isEmpty(text))
            editText.setText(text);

        return new AlertDialog.Builder(getActivity())
                .setView(editText)
                .setTitle(getArguments().getString(ARG_TITLE))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onEdited(
                                getArguments().getInt(ARG_INDEX, -1),
                                getArguments().getInt(ARG_ID, -1),
                                editText.getText().toString());

                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    public static AdBlockEditDialog newInstance(String title) {
        return newInstance(title, -1, null);
    }

    public static AdBlockEditDialog newInstance(String title, int index, AdBlock adBlock) {
        AdBlockEditDialog dialog = new AdBlockEditDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, title);
        bundle.putInt(ARG_INDEX, index);
        if (adBlock != null) {
            bundle.putInt(ARG_ID, adBlock.getId());
            bundle.putString(ARG_TEXT, adBlock.getMatch());
        }
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof AdBlockEditDialogListener) {
            listener = (AdBlockEditDialogListener) getParentFragment();
        } else {
            listener = (AdBlockEditDialogListener) getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    interface AdBlockEditDialogListener {
        void onEdited(int index, int id, String text);
    }
}
