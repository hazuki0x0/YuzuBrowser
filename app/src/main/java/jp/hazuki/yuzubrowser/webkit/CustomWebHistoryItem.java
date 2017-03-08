package jp.hazuki.yuzubrowser.webkit;

import android.graphics.Bitmap;
import android.webkit.WebHistoryItem;

public class CustomWebHistoryItem {
    private final String url;
    private final String originalUrl;
    private final String title;
    private final Bitmap favicon;

    public CustomWebHistoryItem(WebHistoryItem item) {
        this.url = item.getUrl();
        this.originalUrl = item.getOriginalUrl();
        this.title = item.getTitle();
        this.favicon = item.getFavicon();
    }

    public CustomWebHistoryItem(String url, String originalUrl, String title, Bitmap favicon) {
        this.url = url;
        this.originalUrl = originalUrl;
        this.title = title;
        this.favicon = favicon;
    }

    public Bitmap getFavicon() {
        return favicon;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
