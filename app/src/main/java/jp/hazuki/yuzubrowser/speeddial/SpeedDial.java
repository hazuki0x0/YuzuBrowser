/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.speeddial;

import java.io.Serializable;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpeedDial) {
            if (((SpeedDial) obj).getId() <= 0) return false;

            return ((SpeedDial) obj).getId() == id;
        }
        return false;
    }
}
