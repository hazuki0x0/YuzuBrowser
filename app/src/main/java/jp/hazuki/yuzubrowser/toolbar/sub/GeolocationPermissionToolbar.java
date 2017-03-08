package jp.hazuki.yuzubrowser.toolbar.sub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.widget.CheckBox;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.toolbar.SubToolbar;

public class GeolocationPermissionToolbar extends SubToolbar implements View.OnClickListener {
    private String mOrigin;
    private GeolocationPermissions.Callback mCallback;

    public GeolocationPermissionToolbar(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.geolocation_alert, this);
    }

    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
        mOrigin = origin;
        mCallback = callback;
        ((TextView) findViewById(R.id.urlTextView)).setText(origin);
        findViewById(R.id.okButton).setOnClickListener(this);
        findViewById(R.id.cancelButton).setOnClickListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mOrigin = null;
        mCallback = null;
    }

    @Override
    public void onClick(View v) {
        mCallback.invoke(mOrigin, v.getId() == R.id.okButton, ((CheckBox) findViewById(R.id.rememberCheckBox)).isChecked());
        onHideToolbar();
    }

    public void onHideToolbar() {
    }
}
