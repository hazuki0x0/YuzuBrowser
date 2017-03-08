package jp.hazuki.yuzubrowser.useragent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import jp.hazuki.yuzubrowser.R;

/**
 * Created by hazuki on 17/01/19.
 */

public class DeleteUserAgentDialog extends DialogFragment {

    private static final String POS = "pos";

    public static DeleteUserAgentDialog newInstance(int pos) {
        DeleteUserAgentDialog deleteUserAgentDialog = new DeleteUserAgentDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(POS, pos);
        deleteUserAgentDialog.setArguments(bundle);
        return deleteUserAgentDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_ua)
                .setMessage(R.string.delete_ua_confirm)
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
