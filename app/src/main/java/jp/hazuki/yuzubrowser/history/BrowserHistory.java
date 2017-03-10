package jp.hazuki.yuzubrowser.history;

/**
 * Created by hazuki on 17/03/10.
 */

public class BrowserHistory {
    private long mId;
    private String mTitle;
    private String mUrl;
    private long mTime;

    public BrowserHistory() {
    }

    public BrowserHistory(long id, String title, String url, long time) {
        this.mId = id;
        this.mTitle = title;
        this.mUrl = url;
        this.mTime = time;
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public long getTime() {
        return mTime;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public void setTime(long mTime) {
        this.mTime = mTime;
    }
}
