/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.webview.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;

import java.io.File;
import java.io.IOException;

import jp.hazuki.yuzubrowser.core.utility.utils.ImageUtils;
import jp.hazuki.yuzubrowser.webview.CustomWebView;

public class WebViewUtils {
    private WebViewUtils() {
        throw new UnsupportedOperationException();
    }

    public static Bitmap capturePictureOverall(CustomWebView web) {
        Bitmap bitmap = Bitmap.createBitmap(
                Math.max(web.getWebView().getWidth(), web.computeHorizontalScrollRangeMethod()),
                Math.max(web.getWebView().getContentHeight(), web.computeVerticalScrollRangeMethod()),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        web.getWebView().draw(canvas);
        return bitmap;
    }

    public static boolean savePictureOverall(CustomWebView web, File file) throws IOException {
        Bitmap bitmap = null;
        try {
            bitmap = capturePictureOverall(web);
            return bitmap != null && ImageUtils.saveBitmap(bitmap, file);
        } catch (OutOfMemoryError e) {
            System.gc();
            return false;
        } finally {
            if (bitmap != null)
                bitmap.recycle();
        }
    }

    public static Bitmap capturePicturePart(WebView web) {
        int width = web.getWidth();
        int height = web.getHeight();
        if (width == 0 || height == 0)
            return null;

        int scrollY = web.getScrollY();
        int scrollX = web.getScrollX();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(-scrollX, -scrollY);

        canvas.clipRect(scrollX, scrollY, width + scrollX, height + scrollY);
        web.draw(canvas);
        return bitmap;
    }

    public static boolean savePicturePart(WebView web, File file) throws IOException {
        Bitmap bitmap = null;
        try {
            bitmap = capturePicturePart(web);
            return bitmap != null && ImageUtils.saveBitmap(bitmap, file);
        } finally {
            if (bitmap != null)
                bitmap.recycle();
        }
    }

    @SuppressWarnings("deprecation")
    public static String[] getHttpAuthUsernamePassword(Context context, CustomWebView webView, String host, String realm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WebViewDatabase.getInstance(context).getHttpAuthUsernamePassword(host, realm);
        } else {
            return webView.getHttpAuthUsernamePassword(host, realm);
        }
    }

    @SuppressWarnings("deprecation")
    public static void setHttpAuthUsernamePassword(Context context, CustomWebView webView,
                                                   String host, String realm, String username, String password) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WebViewDatabase.getInstance(context).setHttpAuthUsernamePassword(host, realm, username, password);
        } else {
            webView.setHttpAuthUsernamePassword(host, realm, username, password);
        }
    }
}
