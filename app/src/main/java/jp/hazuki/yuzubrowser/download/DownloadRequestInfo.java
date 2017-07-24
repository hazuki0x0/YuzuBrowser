/*
 * Copyright (C) 2017 Hazuki
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
    private String userAgent;
    private String mDefaultExt;
    private boolean solvedFileName;

    public DownloadRequestInfo() {
    }

    public DownloadRequestInfo(String url, File file, String referer, String userAgent, long contentlength) {
        super(url, file);
        this.referer = referer;
        this.userAgent = userAgent;
        this.default_contentlength = contentlength;
    }

    public DownloadRequestInfo(String url, File file, String referer, String userAgent, long contentlength, boolean solvedFileName) {
        super(url, file);
        this.referer = referer;
        this.userAgent = userAgent;
        this.default_contentlength = contentlength;
        this.solvedFileName = solvedFileName;
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

    public String getUserAgent() {
        return userAgent;
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

    public String getDefaultExt() {
        return mDefaultExt;
    }

    public void setDefaultExt(String defaultExt) {
        mDefaultExt = defaultExt;
    }

    public boolean isSolvedFileName() {
        return solvedFileName;
    }
}
