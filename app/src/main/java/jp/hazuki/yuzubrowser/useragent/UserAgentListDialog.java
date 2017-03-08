package jp.hazuki.yuzubrowser.useragent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.webkit.WebSettings;

import jp.hazuki.yuzubrowser.R;

import static android.app.Activity.RESULT_OK;

/**
 * Created by hazuki on 17/01/19.
 */

public class UserAgentListDialog extends DialogFragment {

    private static final String UA = "ua";

    public static UserAgentListDialog newInstance(String userAgent) {
        UserAgentListDialog dialog = new UserAgentListDialog();
        Bundle bundle = new Bundle();
        bundle.putString(UA, userAgent);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        UserAgentList mUserAgentList = new UserAgentList();
        mUserAgentList.read(getActivity());

        String[] entries = new String[mUserAgentList.size() + 1];
        final String[] entryValues = new String[mUserAgentList.size() + 1];

        String ua = getArguments().getString(UA);

        if (ua == null) ua = "";

        int pos = WebSettings.getDefaultUserAgent(getActivity()).equals(ua) ? 0 : -1;

        entries[0] = getContext().getString(R.string.default_text);
        entryValues[0] = "";

        UserAgent userAgent;

        for (int i = 1; mUserAgentList.size() > i - 1; i++) {
            userAgent = mUserAgentList.get(i - 1);
            entries[i] = userAgent.name;
            entryValues[i] = userAgent.useragent;
            if (ua.equals(userAgent.useragent)) {
                pos = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.useragent)
                .setSingleChoiceItems(entries, pos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra(Intent.EXTRA_TEXT, entryValues[which]);
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
