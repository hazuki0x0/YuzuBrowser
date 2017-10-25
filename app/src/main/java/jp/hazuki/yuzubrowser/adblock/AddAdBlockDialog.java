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

package jp.hazuki.yuzubrowser.adblock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.extensions.ContextExtensionsKt;

public class AddAdBlockDialog extends DialogFragment {
    private static final String ARG_TYPE = "type";
    private static final String ARG_TITLE = "title";
    private static final String ARG_URL = "url";

    private OnAdBlockListUpdateListener listener;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText editText = new EditText(getActivity());
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        float density = ContextExtensionsKt.getDensity(getActivity());
        int marginWidth = (int) (8 * density + 0.5f);
        int marginHeight = (int) (16 * density + 0.5f);
        layoutParams.setMargins(marginWidth, marginHeight, marginWidth, marginHeight);
        editText.setLayoutParams(layoutParams);
        editText.setId(android.R.id.edit);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        editText.setText(getArguments().getString(ARG_URL));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getInt(ARG_TITLE));
        builder.setView(editText);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AdBlockManager.AdBlockItemProvider provider = AdBlockManager.getProvider(getActivity(), getArguments().getInt(ARG_TYPE));
                provider.update(new AdBlock(editText.getText().toString()));
                listener.onAdBlockListUpdate();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }


    public static AddAdBlockDialog addBackListInstance(String url) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_TYPE, 1);
        bundle.putInt(ARG_TITLE, R.string.pref_ad_block_black);
        bundle.putString(ARG_URL, trimUrl(url));
        return newInstance(bundle);
    }

    public static AddAdBlockDialog addWhiteListInstance(String url) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_TYPE, 2);
        bundle.putInt(ARG_TITLE, R.string.pref_ad_block_white);
        bundle.putString(ARG_URL, trimUrl(url));
        return newInstance(bundle);
    }

    public static AddAdBlockDialog addWhitePageListInstance(String url) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_TYPE, 3);
        bundle.putInt(ARG_TITLE, R.string.pref_ad_block_white_page);
        bundle.putString(ARG_URL, trimUrl(url));
        return newInstance(bundle);
    }

    private static String trimUrl(String url) {
        if (url != null) {
            int index = url.indexOf("://");
            if (index > -1) {
                return url.substring(index + 3);
            }
        }
        return url;
    }

    private static AddAdBlockDialog newInstance(Bundle bundle) {
        AddAdBlockDialog dialog = new AddAdBlockDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnAdBlockListUpdateListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnAdBlockListUpdateListener {
        void onAdBlockListUpdate();
    }
}
