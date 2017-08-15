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

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jp.hazuki.yuzubrowser.utils.IOUtils;
import jp.hazuki.yuzubrowser.utils.image.DiskLruCache;
import jp.hazuki.yuzubrowser.utils.image.Gochiusearch;

public class FaviconManager implements FaviconCache.OnIconCacheOverFlowListener, DiskLruCache.OnTrimCacheListener {

    private static final int DISK_CACHE_SIZE = 10 * 1024 * 1024;
    private static final int RAM_CACHE_SIZE = 1024 * 1024;
    private static final String CACHE_FOLDER = "favicon";
    private static final int DISK_CACHE_INDEX = 0;

    private final DiskLruCache diskCache;
    private final FaviconCacheIndex diskCacheIndex;
    private final FaviconCache ramCache;
    private final Map<String, Long> ramCacheIndex;

    private static FaviconManager faviconManager;

    public static FaviconManager getInstance(Context context) {
        if (faviconManager == null) {
            faviconManager = new FaviconManager(context);
        }

        return faviconManager;
    }

    public static void destroyInstance() {
        if (faviconManager != null) {
            faviconManager.destroy();
            faviconManager = null;
        }
    }


    private FaviconManager(Context context) {
        try {
            diskCache = DiskLruCache.open(context.getDir(CACHE_FOLDER, Context.MODE_PRIVATE), 1, 1, DISK_CACHE_SIZE);
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
        ramCache = new FaviconCache(RAM_CACHE_SIZE, this);

        diskCacheIndex = new FaviconCacheIndex(context.getApplicationContext(), CACHE_FOLDER);
        ramCacheIndex = new HashMap<>();

        diskCache.setOnTrimCacheListener(this);
    }

    public void update(String url, Bitmap icon) {
        if (icon == null || TextUtils.isEmpty(url)) return;

        url = getNormalUrl(url);

        Long vec = Gochiusearch.getVectorHash(icon);
        String hash = Gochiusearch.getHashString(vec);
        if (!ramCache.containsKey(vec)) {
            ramCache.put(vec, icon);
            addToDiskCache(hash, icon);
        }

        diskCacheIndex.add(url, vec);

        synchronized (ramCacheIndex) {
            ramCacheIndex.put(url, vec);
        }
    }

    public Bitmap get(String url) {
        if (TextUtils.isEmpty(url)) return null;

        url = getNormalUrl(url);

        synchronized (ramCacheIndex) {
            Long icon = ramCacheIndex.get(url);
            if (icon != null) {
                return ramCache.get(icon);
            }
        }

        FaviconCacheIndex.Result result = diskCacheIndex.get(url);
        if (result.exists) {
            Bitmap icon = getFromDiskCache(Gochiusearch.getHashString(result.hash));
            if (icon != null) {
                ramCache.put(result.hash, icon);
                synchronized (ramCacheIndex) {
                    ramCacheIndex.put(url, result.hash);
                }
            } else {
                try {
                    diskCacheIndex.remove(result.hash);
                } catch (SQLiteException e) {
                    Crashlytics.logException(e);
                }
            }
            return icon;
        }

        return null;
    }

    public byte[] getFaviconBytes(String url) {
        if (TextUtils.isEmpty(url)) return null;

        url = getNormalUrl(url);

        synchronized (ramCacheIndex) {
            Long icon = ramCacheIndex.get(url);
            if (icon != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ramCache.get(icon).compress(Bitmap.CompressFormat.PNG, 100, os);
                return os.toByteArray();
            }
        }

        FaviconCacheIndex.Result result = diskCacheIndex.get(url);
        if (result.exists) {
            return getFromDiskCacheBytes(Gochiusearch.getHashString(result.hash));
        }

        return null;
    }

    public void destroy() {
        ramCache.clear();
        ramCacheIndex.clear();
        try {
            diskCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        diskCacheIndex.clear();
        diskCache.clear();
        ramCacheIndex.clear();
        ramCache.clear();
    }

    private String getNormalUrl(String url) {
        int index = url.indexOf('?');
        if (index > -1) {
            url = url.substring(0, index);
        }
        index = url.indexOf('#');
        if (index > -1) {
            url = url.substring(0, index);
        }
        return url;
    }

    private void addToDiskCache(String key, Bitmap bitmap) {
        synchronized (diskCache) {
            OutputStream out = null;
            try {
                DiskLruCache.Snapshot snapshot = diskCache.get(key);
                if (snapshot == null) {
                    final DiskLruCache.Editor editor = diskCache.edit(key);
                    if (editor != null) {
                        out = editor.newOutputStream(DISK_CACHE_INDEX);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        editor.commit();
                        out.close();
                    }
                } else {
                    snapshot.getInputStream(DISK_CACHE_INDEX).close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap getFromDiskCache(String key) {
        Bitmap bitmap = null;

        synchronized (diskCache) {
            InputStream inputStream = null;
            try {
                final DiskLruCache.Snapshot snapshot = diskCache.get(key);
                if (snapshot != null) {
                    inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                    if (inputStream != null) {
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }
    }

    private byte[] getFromDiskCacheBytes(String key) {

        synchronized (diskCache) {
            InputStream inputStream = null;
            try {
                final DiskLruCache.Snapshot snapshot = diskCache.get(key);
                if (snapshot != null) {
                    inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                    if (inputStream != null) {
                        return IOUtils.readByte(inputStream);
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void onCacheOverflow(Long hash) {
        long key = hash;
        synchronized (ramCacheIndex) {
            for (Iterator<Map.Entry<String, Long>> iterator = ramCacheIndex.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Long> item = iterator.next();
                if (key == item.getValue()) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void onTrim(String key) {
        diskCacheIndex.remove(Gochiusearch.parseHashString(key));
    }
}
