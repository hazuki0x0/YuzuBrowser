package jp.hazuki.yuzubrowser.webencode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import jp.hazuki.yuzubrowser.R;

/**
 * Created by hazuki on 17/01/19.
 */

public class EditWebTextEncodeDialog extends DialogFragment {

    private static final String POS = "pos";
    private static final String ENCODING = "enc";

    public static EditWebTextEncodeDialog newInstance() {
        return newInstance(-1, "");
    }

    public static EditWebTextEncodeDialog newInstance(int pos, WebTextEncode encode) {
        return newInstance(pos, encode.encoding);
    }

    private static EditWebTextEncodeDialog newInstance(int pos, String encoding) {
        EditWebTextEncodeDialog dialog = new EditWebTextEncodeDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(POS, pos);
        bundle.putString(ENCODING, encoding);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.webencode_add, null);
        final EditText encodeEditText = (EditText) view.findViewById(R.id.encodeEditText);

        encodeEditText.setText(getArguments().getString(ENCODING));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(encodeEditText.getText()))
                            return;

                        if (getParentFragment() instanceof OnEditedWebTextEncode) {
                            ((OnEditedWebTextEncode) getParentFragment())
                                    .onEdited(getArguments().getInt(POS), encodeEditText.getText().toString());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }


    public interface OnEditedWebTextEncode {
        void onEdited(int position, String name);
    }
}
