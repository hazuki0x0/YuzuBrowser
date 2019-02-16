package jp.hazuki.yuzubrowser.legacy.utils.net;

public abstract class HttpClientBuilder {
    protected HttpClientBuilder() {
    }

    public static HttpClientBuilder createInstance(String url) {
        return HttpURLConnectionWrapper.createInstance(url);
    }

    public abstract void setHeader(String name, String value);

    public abstract HttpResponseData connect();

    public abstract void destroy();
}
