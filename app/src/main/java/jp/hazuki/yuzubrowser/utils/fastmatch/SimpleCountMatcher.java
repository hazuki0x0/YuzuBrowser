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

package jp.hazuki.yuzubrowser.utils.fastmatch;

import android.net.Uri;

abstract class SimpleCountMatcher implements FastMatcher {

    private int count;
    private int id;
    private boolean update;
    private long time;

    protected abstract boolean matchItem(Uri uri);

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public final boolean match(Uri uri) {
        if (matchItem(uri)) {
            if (count != Integer.MAX_VALUE)
                count++;
            time = System.currentTimeMillis();
            update = true;
            return true;
        }
        return false;
    }

    @Override
    public final int getFrequency() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean isUpdate() {
        return update;
    }

    @Override
    public void saved() {
        update = false;
    }

    @Override
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
