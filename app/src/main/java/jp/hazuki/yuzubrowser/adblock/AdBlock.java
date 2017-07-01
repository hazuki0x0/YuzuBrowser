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

package jp.hazuki.yuzubrowser.adblock;

import java.io.Serializable;

public class AdBlock implements Serializable {
    private int id;
    private String match;
    private boolean enable;
    private int count;
    private long time;

    public AdBlock() {
        id = -1;
        enable = true;
    }

    public AdBlock(String match) {
        this();
        this.match = match;
    }

    public AdBlock(int id, String match, boolean enable, int count, long time) {
        this.id = id;
        this.match = match;
        this.enable = enable;
        this.count = count;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getTime() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof AdBlock && id == ((AdBlock) obj).id;
    }
}
