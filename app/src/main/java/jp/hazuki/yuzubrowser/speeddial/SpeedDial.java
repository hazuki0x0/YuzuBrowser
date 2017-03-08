package jp.hazuki.yuzubrowser.speeddial;

import java.io.Serializable;

/**
 * Created by hazuki on 17/02/19.
 */

public class SpeedDial implements Serializable {
    private int id;
    private String url;
    private String title;
    private WebIcon icon;
    private boolean favicon;

    public SpeedDial() {
        this("", "");
    }

    public SpeedDial(String url, String title) {
        this(url, title, null, false);
    }

    public SpeedDial(String url, String title, WebIcon icon, boolean isFavicon) {
        this(-1, url, title, icon, isFavicon);
    }

    public SpeedDial(int id, String url, String title, WebIcon icon, boolean isFavicon) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.icon = icon;
        favicon = isFavicon;
    }

    public WebIcon getIcon() {
        return icon;
    }

    public void setIcon(WebIcon icon) {
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFavicon() {
        return favicon;
    }

    public void setFaviconMode(boolean favicon) {
        this.favicon = favicon;
    }
}
