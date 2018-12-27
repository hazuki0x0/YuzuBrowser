package jp.hazuki.yuzubrowser.legacy.browser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.utils.WebViewUtils;
import jp.hazuki.yuzubrowser.legacy.webkit.CustomWebView;

public class HttpAuthRequestDialog {
    private final Context mContext;

    public HttpAuthRequestDialog(Context context) {
        mContext = context;
    }

    public void requestHttpAuth(final CustomWebView view, final HttpAuthHandler handler, final String host, final String realm) {
        String username = null;
        String password = null;
        if (handler.useHttpAuthUsernamePassword()) {
            String[] credentials = WebViewUtils.getHttpAuthUsernamePassword(mContext, view, host, realm);
            if (credentials != null && credentials.length == 2) {
                username = credentials[0];
                password = credentials[1];
            }
        }
        if (username == null || password == null) {
            View dialog_view = LayoutInflater.from(mContext).inflate(R.layout.http_auth_dialog, null);
            final EditText nameEditText = dialog_view.findViewById(R.id.nameEditText);
            final EditText passEditText = dialog_view.findViewById(R.id.passEditText);
            CheckBox checkBox = dialog_view.findViewById(R.id.checkBox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        passEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    } else {
                        passEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            });
            new AlertDialog.Builder(mContext)
                    .setTitle(R.string.login)
                    .setView(dialog_view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String username = nameEditText.getText().toString();
                            String password = passEditText.getText().toString();
                            WebViewUtils.setHttpAuthUsernamePassword(mContext, view, host, realm, username, password);
                            handler.proceed(username, password);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.cancel();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            handler.cancel();
                        }
                    })
                    .show();
        } else {
            handler.proceed(username, password);
        }
    }
}
