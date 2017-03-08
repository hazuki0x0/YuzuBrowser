package jp.hazuki.yuzubrowser.download;

import android.content.Context;
import android.text.format.Formatter;

import java.io.File;

public class DownloadRequestInfo extends DownloadInfo {
    private static final long serialVersionUID = 3601107888371011910L;

    private long default_contentlength;
    private String referer;
    private int max_length;
    private int current_length;

    public DownloadRequestInfo() {
    }

    public DownloadRequestInfo(String url, File file, String referer, long contentlength) {
        super(url, file);
        this.referer = referer;
        this.default_contentlength = contentlength;
    }

    public long getDefaultContentLength() {
        return default_contentlength;
    }

    public void setDefaultContentLength(long default_contentlength) {
        this.default_contentlength = default_contentlength;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public int getMaxLength() {
        return max_length;
    }

    public void setMaxLength(int max_length) {
        this.max_length = max_length;
    }

    public int getCurrentLength() {
        return current_length;
    }

    public void setCurrentLength(int current_length) {
        this.current_length = current_length;
    }

    public String getNotificationString(Context context) {
        if (max_length > 0) {
            return (Formatter.formatFileSize(context, current_length) +
                    " / " +
                    Formatter.formatFileSize(context, max_length) +
                    " (" +
                    100 * current_length / max_length +
                    "%)");
        } else {
            //return (FileUtils.convertReadableByteCount(current_length));
            return Formatter.formatFileSize(context, current_length);
        }
    }
}
