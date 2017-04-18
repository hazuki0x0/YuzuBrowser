package jp.hazuki.yuzubrowser.utils.net;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface HttpResponseData {
    InputStream getInputStream();

    long getContentLength();

    Map<String, List<String>> getHeaderFields();
}
