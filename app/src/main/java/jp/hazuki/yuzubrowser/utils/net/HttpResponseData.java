package jp.hazuki.yuzubrowser.utils.net;

import java.io.InputStream;

public interface HttpResponseData {
    InputStream getInputStream();

    long getContentLength();
}
