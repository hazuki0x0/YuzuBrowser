package jp.hazuki.yuzubrowser.webkit.handler;

import android.os.Handler;
import android.os.Message;

public abstract class WebSrcImageHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        final String url = msg.getData().getString("url");
        if (url == null || url.length() == 0) return;
        handleUrl(url);
    }

    public abstract void handleUrl(String url);
}
