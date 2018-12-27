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

package jp.hazuki.yuzubrowser.legacy.tab.manager;

import java.util.LinkedHashMap;
import java.util.Map;

public class TabCache<T extends TabData> extends LinkedHashMap<Long, T> {

    private final OnCacheOverFlowListener<T> mListener;
    private int mSize;

    public TabCache(int cacheSize, OnCacheOverFlowListener<T> listener) {
        super(cacheSize, 0.75f, true);
        mSize = cacheSize;
        mListener = listener;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Long, T> eldest) {
        boolean result = size() > mSize;
        if (result) {
            mListener.onCacheOverflow(eldest.getValue());
        }
        return result;
    }

    @Override
    public T put(Long key, T value) {
        if (value != null)
            return super.put(key, value);
        return null;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public interface OnCacheOverFlowListener<T> {
        void onCacheOverflow(T tabData);
    }
}
