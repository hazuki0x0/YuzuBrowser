package jp.hazuki.yuzubrowser.utils.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by hazuki on 17/01/19.
 */

public class DeleteDialog extends DialogFragment {

    private static final String POS = "pos";
    private static final String TITLE = "title";
    private static final String MES = "mes";

    public static DeleteDialog newInstance(Context context, int title, int message, int pos) {
        return newInstance(context.getString(title), context.getString(message), pos);
    }

    public static DeleteDialog newInstance(String title, String message, int pos) {
        DeleteDialog deleteWebTextEncodeDialog = new DeleteDialog();
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
                        if (getParentFragment() instanceof OnDelete) {
                            ((OnDelete) getParentFragment()).onDelete(getArguments().getInt(POS));
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
