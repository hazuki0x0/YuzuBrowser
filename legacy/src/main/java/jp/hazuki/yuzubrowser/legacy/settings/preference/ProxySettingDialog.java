package jp.hazuki.yuzubrowser.legacy.settings.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData;
import jp.hazuki.yuzubrowser.legacy.settings.preference.common.CustomDialogPreference;
import jp.hazuki.yuzubrowser.legacy.webkit.WebViewProxy;

public class ProxySettingDialog extends CustomDialogPreference {
    private boolean mSaveSettings = true;

    public ProxySettingDialog(Context context) {
        super(context);
    }

    public ProxySettingDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProxySettingDialog setSaveSettings(boolean save) {
        mSaveSettings = save;
        return this;
    }

    @NonNull
    @Override
    protected CustomDialogFragment crateCustomDialog() {
        return SettingDialog.newInstance(mSaveSettings);
    }

    public static class SettingDialog extends CustomDialogFragment {
        private static final String SAVE = "save";

        public static SettingDialog newInstance(boolean save) {
            SettingDialog dialog = new SettingDialog();
            Bundle bundle = new Bundle();
            bundle.putBoolean(SAVE, save);
            dialog.setArguments(bundle);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.proxy_dialog, null);
            final CheckBox checkBox = view.findViewById(R.id.checkBox);
            final EditText editText = view.findViewById(R.id.editText);

            checkBox.setChecked(AppData.proxy_set.get());
            editText.setText(AppData.proxy_address.get());

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder
                    .setView(view)
                    .setTitle(R.string.pref_proxy_settings)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        Context context = getContext();
                        boolean enable = checkBox.isChecked();
                        String proxy_address = editText.getText().toString();

                        WebViewProxy.setProxy(context, enable, proxy_address);

                        if (getArguments().getBoolean(SAVE)) {
                            AppData.proxy_set.set(enable);
                            AppData.proxy_address.set(proxy_address);
                            AppData.commit(context, AppData.proxy_set, AppData.proxy_address);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);

            return builder.create();
        }
    }
}
