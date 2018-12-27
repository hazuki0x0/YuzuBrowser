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

package jp.hazuki.yuzubrowser.legacy.utils.image;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;

public class ImageCache {
    private LruCache<Integer, Bitmap> imageCache;

    public ImageCache(int cacheSize) {
        imageCache = new ImageLruCache(cacheSize);
    }

    public Bitmap getBitmap(String url) {
        if (TextUtils.isEmpty(url)) return null;
        return imageCache.get(getKey(url));
    }

    public void putBitmap(String url, Bitmap image) {
        if (!TextUtils.isEmpty(url)) {
            imageCache.put(getKey(url), image);
        }
    }

    public void remove(String url) {
        if (!TextUtils.isEmpty(url))
            imageCache.remove(getKey(url));
    }

    public void dispose() {
        imageCache.evictAll();
    }

    private int getKey(String url) {
        return url.hashCode();
    }

    private static class ImageLruCache extends LruCache<Integer, Bitmap> {
        ImageLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(Integer key, Bitmap value) {
            return value.getByteCount();
        }
    }
}
