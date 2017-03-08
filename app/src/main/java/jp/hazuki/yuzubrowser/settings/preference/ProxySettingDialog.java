package jp.hazuki.yuzubrowser.settings.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.preference.common.CustomDialogPreference;
import jp.hazuki.yuzubrowser.webkit.WebViewProxy;

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

    @Override
    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.proxy_dialog, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        final EditText editText = (EditText) view.findViewById(R.id.editText);

        checkBox.setChecked(AppData.proxy_set.get());
        editText.setText(AppData.proxy_address.get());

        builder
                .setView(view)
                .setTitle(R.string.pref_proxy_settings)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Context context = getContext();
                        boolean enable = checkBox.isChecked();
                        String proxy_address = editText.getText().toString();

                        WebViewProxy.setProxy(context, enable, proxy_address);

                        if (mSaveSettings) {
                            AppData.proxy_set.set(enable);
                            AppData.proxy_address.set(proxy_address);
                            AppData.commit(context, AppData.proxy_set, AppData.proxy_address);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
    }
}
