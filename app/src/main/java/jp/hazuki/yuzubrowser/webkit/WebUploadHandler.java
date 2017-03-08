package jp.hazuki.yuzubrowser.webkit;

import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

public class WebUploadHandler {
    private ValueCallback<Uri[]> mUploadMsg;

    public void onActivityResult(int resultCode, Intent intent) {
        if (mUploadMsg == null) return;

        mUploadMsg.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
        mUploadMsg = null;
    }

    public void destroy() {
        if (mUploadMsg != null) {
            mUploadMsg.onReceiveValue(null);
            mUploadMsg = null;
        }
    }


    public Intent onShowFileChooser(ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams params) {
        mUploadMsg = uploadMsg;
        return params.createIntent();
    }
}
