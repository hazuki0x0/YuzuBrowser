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

package jp.hazuki.yuzubrowser.tab.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.LongSparseArray;

import java.util.concurrent.LinkedBlockingQueue;

import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.utils.ImageUtils;
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

class ThumbnailManager {
    private final int height;
    private final int width;

    private final LongSparseArray<Bitmap> thumbnails;
    private final LongSparseArray<Boolean> shotTab;

    private OnThumbnailListener thumbnailListener;
    private ThumbThread thread;

    ThumbnailManager(Context context, OnThumbnailListener listener) {
        float density = DisplayUtils.getDensity(context);
        height = (int) (density * 82 + 0.5f);
        width = (int) (density * 104 + 0.5f);

        thumbnails = new LongSparseArray<>();
        shotTab = new LongSparseArray<>();

        /* save image */
        thumbnailListener = listener;
        thread = new ThumbThread(listener);
        thread.start();
    }

    public void takeThumbnailIfNeeded(CustomWebView webView) {
        if (!shotTab.get(webView.getIdentityId(), Boolean.FALSE)) {
            create(webView);
        }
    }

    public void create(final CustomWebView webView) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                shotTab.put(webView.getIdentityId(), true);
                Bitmap bitmap = createThumbnailImage(webView);
                if (bitmap != null) {
                    thumbnails.put(webView.getIdentityId(), bitmap);
                    thread.addQueue(new ThumbItem(webView.getIdentityId(), bitmap));
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                webView.getWebView().post(runnable);
            }
        }).start();
    }

    protected Bitmap createThumbnailImage(CustomWebView webView) {
        int x = webView.getWebView().getWidth();
        int y = (int) ((float) x / width * height + 0.5f);
        if (x <= 0 || y <= 0) return null;
        float scale = (float) width / x;
        int scroll = webView.getWebView().getScrollY();

        Bitmap bitmap = Bitmap.createBitmap(x, y + scroll, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(bitmap);
        webView.getWebView().draw(localCanvas);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, scroll, x, y, matrix, true);
    }

    public Bitmap getThumbnail(long id) {
        /* from cache */
        Bitmap bitmap = thumbnails.get(id);

        /* from file */
        if (bitmap == null) {
            byte[] image = thumbnailListener.onLoadThumbnail(id);
            if (image != null) {
                bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                thumbnails.put(id, bitmap);
            }
        }

        return bitmap;
    }

    public void removeThumbnailCache(long id) {
        thumbnails.remove(id);
    }

    public void onStartPage(long id) {
        shotTab.put(id, Boolean.FALSE);
    }

    public void destroy() {
        thread.interrupt();
    }

    private static final class ThumbItem {
        private final long id;
        private final Bitmap thumbnail;

        ThumbItem(long id, Bitmap icon) {
            this.id = id;
            thumbnail = icon;
        }
    }

    private static class ThumbThread extends Thread {
        private final OnThumbnailListener thumbnailListener;
        private final LinkedBlockingQueue<ThumbItem> queue = new LinkedBlockingQueue<>();

        ThumbThread(OnThumbnailListener listener) {
            thumbnailListener = listener;
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            setPriority(MIN_PRIORITY);
            try {
                while (true) {
                    save(queue.take());
                }
            } catch (InterruptedException e) {
                Logger.d("Thumbnail", "thread stop");
            }
        }

        private void save(ThumbItem item) {
            byte[] image = ImageUtils.bmp2byteArray(item.thumbnail, Bitmap.CompressFormat.WEBP, 70);
            thumbnailListener.onSaveThumbnail(item.id, image);
        }

        void addQueue(ThumbItem item) {
            queue.add(item);
        }
    }

    public interface OnThumbnailListener {
        void onSaveThumbnail(long id, byte[] image);

        byte[] onLoadThumbnail(long id);
    }
}
