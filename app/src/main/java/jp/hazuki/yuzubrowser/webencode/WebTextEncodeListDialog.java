package jp.hazuki.yuzubrowser.webencode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import jp.hazuki.yuzubrowser.R;

import static android.app.Activity.RESULT_OK;

/**
 * Created by hazuki on 17/01/19.
 */

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
        getActivity().finish();
    }
}
