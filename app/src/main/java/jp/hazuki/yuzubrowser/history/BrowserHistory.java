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

package jp.hazuki.yuzubrowser.history;

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
