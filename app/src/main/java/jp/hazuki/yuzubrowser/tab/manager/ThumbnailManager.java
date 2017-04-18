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
import android.graphics.Canvas;
import android.graphics.Matrix;

import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

class ThumbnailManager {
    private final int height;
    private final int width;

    ThumbnailManager(Context context) {
        float density = DisplayUtils.getDensity(context);
        height = (int) (density * 82 + 0.5f);
        width = (int) (density * 104 + 0.5f);
    }

    void takeThumbnailIfNeeded(MainTabData data) {
        if (!data.isShotThumbnail()) {
            create(data);
        }
    }

    private void create(final MainTabData data) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = createThumbnailImage(data.mWebView);
                if (bitmap != null) {
                    data.shotThumbnail(bitmap);
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

                data.mWebView.getWebView().post(runnable);
            }
        }).start();
    }

    void forceTakeThumbnail(MainTabData data) {
        Bitmap bitmap = createThumbnailImage(data.mWebView);
        if (bitmap != null) {
            data.shotThumbnail(bitmap);
        }
    }

    private Bitmap createThumbnailImage(CustomWebView webView) {
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
}
