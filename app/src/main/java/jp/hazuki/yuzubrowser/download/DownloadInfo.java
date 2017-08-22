package jp.hazuki.yuzubrowser.download;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class DownloadInfo implements Serializable {
    private static final long serialVersionUID = 8132188309973604990L;

    public static final int STATE_DOWNLOADING = 0;
    public static final int STATE_DOWNLOADED = 1;
    public static final int STATE_CANCELED = 2;
    public static final int STATE_UNKNOWN_ERROR = 100;

    protected long id = -1;
    protected String url;
    protected File file;
    protected long start_time = System.currentTimeMillis();
    protected int state = STATE_DOWNLOADING;

    public DownloadInfo() {
    }

    public DownloadInfo(String url, File file) {
        this.url = url;
        try {
            this.file = file.getCanonicalFile();
        } catch (IOException e) {
            this.file = file;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        try {
            this.file = file.getCanonicalFile();
        } catch (IOException e) {
            this.file = file;
        }
    }

    public void setFile(String file) {
        this.file = new File(file);
    }

    public long getStartTime() {
        return start_time;
    }

    public void setStartTime(long start_time) {
        this.start_time = start_time;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
