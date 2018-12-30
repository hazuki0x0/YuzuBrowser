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

package jp.hazuki.yuzubrowser.legacy.webencode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import jp.hazuki.yuzubrowser.legacy.R;

import static android.app.Activity.RESULT_OK;

public class WebTextEncodeListDialog extends DialogFragment {

    private static final String ENCODING = "enc";

    public static WebTextEncodeListDialog newInstance(String webTextEncode) {
        WebTextEncodeListDialog dialog = new WebTextEncodeListDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ENCODING, webTextEncode);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        WebTextEncodeList encodes = new WebTextEncodeList();
        encodes.read(getActivity());

        final String[] entries = new String[encodes.size()];

        String now = getArguments().getString(ENCODING);

        if (now == null) now = "";

        int pos = -1;

        WebTextEncode encode;
        for (int i = 0; encodes.size() > i; i++) {
            encode = encodes.get(i);
            entries[i] = encode.encoding;
            if (now.equals(encode.encoding)) {
                pos = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.web_encode)
                .setSingleChoiceItems(entries, pos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra(Intent.EXTRA_TEXT, entries[which]);
                        getActivity().setResult(RESULT_OK, intent);
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity != null)
            activity.finish();
    }
}
