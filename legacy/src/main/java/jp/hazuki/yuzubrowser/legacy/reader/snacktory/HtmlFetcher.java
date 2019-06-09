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
package jp.hazuki.yuzubrowser.legacy.reader.snacktory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jp.hazuki.yuzubrowser.core.utility.log.Logger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Class to fetch articles. This class is thread safe.
 *
 * @author Peter Karich
 */
public class HtmlFetcher {

    static {
        SHelper.enableCookieMgmt();
        SHelper.enableUserAgentOverwrite();
        SHelper.enableAnySSL();
    }

    private String referrer = "https://github.com/karussell/snacktory";
    private String userAgent = "Mozilla/5.0 (compatible; Snacktory; +" + referrer + ")";
    private String cacheControl = "max-age=0";
    private String language = "en-us";
    private String accept = "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
    private String charset = "UTF-8";
    private SCache cache;
    private AtomicInteger cacheCounter = new AtomicInteger(0);
    private int maxTextLength = -1;
    private ArticleTextExtractor extractor = new ArticleTextExtractor();
    private Set<String> furtherResolveNecessary = new HashSet<String>() {
        {
            add("bit.ly");
            add("cli.gs");
            add("deck.ly");
            add("fb.me");
            add("feedproxy.google.com");
            add("flic.kr");
            add("fur.ly");
            add("goo.gl");
            add("is.gd");
            add("ink.co");
            add("j.mp");
            add("lnkd.in");
            add("on.fb.me");
            add("ow.ly");
            add("plurl.us");
            add("sns.mx");
            add("snurl.com");
            add("su.pr");
            add("t.co");
            add("tcrn.ch");
            add("tl.gd");
            add("tiny.cc");
            add("tinyurl.com");
            add("tmi.me");
            add("tr.im");
            add("twurl.nl");
        }
    };

    public HtmlFetcher() {
    }

    public void setExtractor(ArticleTextExtractor extractor) {
        this.extractor = extractor;
    }

    public ArticleTextExtractor getExtractor() {
        return extractor;
    }

    public HtmlFetcher setCache(SCache cache) {
        this.cache = cache;
        return this;
    }

    public SCache getCache() {
        return cache;
    }

    public int getCacheCounter() {
        return cacheCounter.get();
    }

    public HtmlFetcher clearCacheCounter() {
        cacheCounter.set(0);
        return this;
    }

    public HtmlFetcher setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
        return this;
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getReferrer() {
        return referrer;
    }

    public HtmlFetcher setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAccept() {
        return accept;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public String getCharset() {
        return charset;
    }

    public JResult fetchAndExtract(OkHttpClient client, String url, int timeout, boolean resolve) throws Exception {
        OkHttpClient newClient = client.newBuilder()
            .callTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .build();

        String originalUrl = url;
        url = SHelper.removeHashbang(url);
        String gUrl = SHelper.getUrlFromUglyGoogleRedirect(url);
        if (gUrl != null)
            url = gUrl;
        else {
            gUrl = SHelper.getUrlFromUglyFacebookRedirect(url);
            if (gUrl != null)
                url = gUrl;
        }

        if (resolve) {
            // check if we can avoid resolving the URL (which hits the website!)
            JResult res = getFromCache(url, originalUrl);
            if (res != null)
                return res;

            String resUrl = getResolvedUrl(newClient, url);
            if (resUrl.isEmpty()) {
                Logger.w("snacktory", "resolved url is empty. Url is: ", url);

                JResult result = new JResult();
                if (cache != null)
                    cache.put(url, result);
                return result.setUrl(url);
            }

            // if resolved url is longer then use it!
            if (resUrl.trim().length() > url.length()) {
                // this is necessary e.g. for some homebaken url resolvers which return
                // the resolved url relative to url!
                url = SHelper.useDomainOfFirstArg4Second(url, resUrl);
            }
        }

        // check if we have the (resolved) URL in cache
        JResult res = getFromCache(url, originalUrl);
        if (res != null)
            return res;

        JResult result = new JResult();
        // or should we use? <link rel="canonical" href="http://www.N24.de/news/newsitem_6797232.html"/>
        result.setUrl(url);
        result.setOriginalUrl(originalUrl);
        result.setDate(SHelper.estimateDate(url));

        // Immediately put the url into the cache as extracting content takes time.
        if (cache != null) {
            cache.put(originalUrl, result);
            cache.put(url, result);
        }

        String lowerUrl = url.toLowerCase();
        if (SHelper.isDoc(lowerUrl) || SHelper.isApp(lowerUrl) || SHelper.isPackage(lowerUrl)) {
            // skip
        } else if (SHelper.isVideo(lowerUrl) || SHelper.isAudio(lowerUrl)) {
            result.setVideoUrl(url);
        } else if (SHelper.isImage(lowerUrl)) {
            result.setImageUrl(url);
        } else {
            extractor.extractContent(result, fetchAsString(newClient, url, timeout));
            if (result.getFaviconUrl().isEmpty())
                result.setFaviconUrl(SHelper.getDefaultFavicon(url));

            // some links are relative to root and do not include the domain of the url :(
            result.setFaviconUrl(fixUrl(url, result.getFaviconUrl()));
            result.setImageUrl(fixUrl(url, result.getImageUrl()));
            result.setVideoUrl(fixUrl(url, result.getVideoUrl()));
            result.setRssUrl(fixUrl(url, result.getRssUrl()));
        }
        result.setText(lessText(result.getText()));
        synchronized (result) {
            result.notifyAll();
        }
        return result;
    }

    public String lessText(String text) {
        if (text == null)
            return "";

        if (maxTextLength >= 0 && text.length() > maxTextLength)
            return text.substring(0, maxTextLength);

        return text;
    }

    private static String fixUrl(String url, String urlOrPath) {
        return SHelper.useDomainOfFirstArg4Second(url, urlOrPath);
    }

    public String fetchAsString(OkHttpClient client, String urlAsString, int timeout) throws IOException {
        return fetchAsString(client, urlAsString, timeout, true);
    }

    public String fetchAsString(OkHttpClient client, String urlAsString, int timeout, boolean includeSomeGooseOptions)
        throws IOException {
        Request request = createRequest(urlAsString, includeSomeGooseOptions, false);

        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();
        if (body == null) throw new IOException("Connection failed");

        InputStream is = body.byteStream();

        MediaType type = body.contentType();

        String enc = type != null ? type.charset(Charset.defaultCharset()).toString() : "UTF-8";
        String res = createConverter(urlAsString).streamToString(is, enc);
        Logger.d("snacktory", res.length(), " FetchAsString:", urlAsString);
        return res;
    }

    public Converter createConverter(String url) {
        return new Converter(url);
    }

    /**
     * On some devices we have to hack:
     * http://developers.sun.com/mobility/reference/techart/design_guidelines/http_redirection.html
     *
     * @return the resolved url if any. Or null if it couldn't resolve the url
     * (within the specified time) or the same url if response code is OK
     */
    public String getResolvedUrl(OkHttpClient client, String urlAsString) {
        // force no follow
        OkHttpClient newClient = client.newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build();
        return getResolvedUrlInternal(newClient, urlAsString);
    }

    private String getResolvedUrlInternal(OkHttpClient client, String urlAsString) {
        String newUrl = null;
        int responseCode = -1;
        try {
            Request request = createRequest(urlAsString, true, true);

            Response response = client.newCall(request).execute();

            responseCode = response.code();
            if (response.body() != null) response.close();
            if (responseCode == HttpURLConnection.HTTP_OK)
                return urlAsString;

            newUrl = response.header("Location");
            if (responseCode / 100 == 3 && newUrl != null) {
                newUrl = newUrl.replaceAll(" ", "+");
                // some services use (none-standard) utf8 in their location header
                if (urlAsString.startsWith("http://bit.ly") || urlAsString.startsWith("http://is.gd"))
                    newUrl = encodeUriFromHeader(newUrl);

                // fix problems if shortened twice. as it is often the case after twitters' t.co bullshit
                if (furtherResolveNecessary.contains(SHelper.extractDomain(newUrl, true)))
                    newUrl = getResolvedUrlInternal(client, newUrl);

                return newUrl;
            } else
                return urlAsString;

        } catch (Exception ex) {
            Logger.w("snacktory", ex, "getResolvedUrl:", urlAsString, " Error:", ex.getMessage());
            return "";
        } finally {
            Logger.d("snacktory", responseCode, " url:", urlAsString, " resolved:", newUrl);
        }
    }

    /**
     * Takes a URI that was decoded as ISO-8859-1 and applies percent-encoding
     * to non-ASCII characters. Workaround for broken origin servers that send
     * UTF-8 in the Location: header.
     */
    static String encodeUriFromHeader(String badLocation) {
        StringBuilder sb = new StringBuilder();

        for (char ch : badLocation.toCharArray()) {
            if (ch < (char) 128) {
                sb.append(ch);
            } else {
                // this is ONLY valid if the uri was decoded using ISO-8859-1
                sb.append(String.format("%%%02X", (int) ch));
            }
        }

        return sb.toString();
    }

    protected Request createRequest(String urlAsStr, boolean includeSomeGooseOptions, boolean head) {
        Request.Builder builder = new Request.Builder();
        builder.url(urlAsStr)
            .addHeader("User-Agent", userAgent)
            .addHeader("Accept", accept);

        if (head) {
            builder.head();
        } else {
            builder.get();
        }

        if (includeSomeGooseOptions) {
            builder.addHeader("Accept-Language", language)
                .addHeader("content-charset", charset)
                .addHeader("Referer", referrer)
                .addHeader("Cache-Control", cacheControl);
        }
        return builder.build();
    }

    private JResult getFromCache(String url, String originalUrl) throws Exception {
        if (cache != null) {
            JResult res = cache.get(url);
            if (res != null) {
                // e.g. the cache returned a shortened url as original url now we want to store the
                // current original url! Also it can be that the cache response to url but the JResult
                // does not contain it so overwrite it:
                res.setUrl(url);
                res.setOriginalUrl(originalUrl);
                cacheCounter.addAndGet(1);
                return res;
            }
        }
        return null;
    }
}
