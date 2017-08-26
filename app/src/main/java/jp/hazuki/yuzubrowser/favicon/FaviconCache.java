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

package jp.hazuki.yuzubrowser.favicon;

import android.graphics.Bitmap;

import java.util.LinkedHashMap;
import java.util.Map;

class FaviconCache extends LinkedHashMap<Long, Bitmap> {

    private final OnIconCacheOverFlowListener mListener;
    private int mSize;

    FaviconCache(int cacheSize, OnIconCacheOverFlowListener listener) {
        super(cacheSize, 0.75f, true);
        mSize = cacheSize;
        mListener = listener;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Long, Bitmap> eldest) {
        boolean result = size() > mSize;
        if (result) {
            mListener.onCacheOverflow(eldest.getKey());
        }
        return result;
    }

    @Override
    public Bitmap put(Long key, Bitmap value) {
        if (value != null)
            return super.put(key, value);
        return null;
    }

    void setSize(int size) {
        mSize = size;
    }

    interface OnIconCacheOverFlowListener {
        void onCacheOverflow(Long hash);
    }
}
