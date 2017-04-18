package jp.hazuki.yuzubrowser.utils.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import jp.hazuki.yuzubrowser.utils.ErrorReport;

public class HttpURLConnectionWrapper extends HttpClientBuilder {
    private final HttpURLConnection mConnection;

    private HttpURLConnectionWrapper(HttpURLConnection con) {
        mConnection = con;
    }

    public static HttpURLConnectionWrapper createInstance(String url) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(true);
            return new HttpURLConnectionWrapper(con);
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return null;
    }

    @Override
    public void setHeader(String name, String value) {
        mConnection.setRequestProperty(name, value);
    }

    private static class MyHttpResponseData implements HttpResponseData {
        private final InputStream mInputStream;
        private final long mContentLength;
        private final Map<String, List<String>> mHeaderFields;

        public MyHttpResponseData(InputStream is, long cl, Map<String, List<String>> header) {
            mInputStream = is;
            mContentLength = cl;
            mHeaderFields = header;
        }

        @Override
        public InputStream getInputStream() {
            return mInputStream;
        }

        @Override
        public long getContentLength() {
            return mContentLength;
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            return mHeaderFields;
        }
    }

    @Override
    public HttpResponseData connect() {
        try {
            mConnection.connect();
            if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = mConnection.getInputStream();

                String cl_str = mConnection.getHeaderField("Content-Length");
                long cl = -1;
                if (cl_str != null) {
                    try {
                        cl = Long.parseLong(cl_str);
                    } catch (NumberFormatException e) {
                        ErrorReport.printAndWriteLog(e);
                    }
                }

                return new MyHttpResponseData(is, cl, mConnection.getHeaderFields());
            }
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return null;
    }

    @Override
    public void destroy() {
    }

}
