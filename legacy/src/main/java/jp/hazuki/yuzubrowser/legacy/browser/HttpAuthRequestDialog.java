/*
 * Copyright (C) 2017-2019 Hazuki
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
import jp.hazuki.yuzubrowser.webview.CustomWebView;
import jp.hazuki.yuzubrowser.webview.utility.WebViewUtils;

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
