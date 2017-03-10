package jp.hazuki.yuzubrowser.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class WebViewUtils {
    private WebViewUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean shouldLoadSameTabAuto(String url) {
        return url.regionMatches(true, 0, "about:", 0, 6);
    }

    public static boolean shouldLoadSameTabUser(String url) {
        return url.regionMatches(true, 0, "javascript:", 0, 11);
    }

    public static boolean setDisplayZoomButtons(WebSettings setting, boolean show) {
        setting.setSupportZoom(true);
        setting.setBuiltInZoomControls(true);
        setting.setDisplayZoomControls(show);
        return true;
    }

    public static boolean setDisplayZoomButtons(WebSettings from_setting, WebSettings to_setting) {
        to_setting.setSupportZoom(true);
        to_setting.setBuiltInZoomControls(true);
        to_setting.setDisplayZoomControls(from_setting.getDisplayZoomControls());
        return true;
    }

    public static void setTextSize(WebSettings setting, int textZoom) {
        setting.setTextZoom(textZoom);
    }

    public static int getTextSize(WebSettings setting) {
        return setting.getTextZoom();
    }

    public static void setTextSize(WebSettings from_setting, WebSettings to_setting) {
        to_setting.setTextZoom(from_setting.getTextZoom());
    }

    public static Bitmap capturePictureOverall(WebView web) {
        Bitmap bitmap = Bitmap.createBitmap(web.getWidth(), web.getContentHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        web.draw(canvas);
        return bitmap;
    }

    public static boolean savePictureOverall(WebView web, File file) throws IOException {
        Bitmap bitmap = null;
        try {
            bitmap = capturePictureOverall(web);
            return ImageUtils.saveBitmap(bitmap, file);
        } finally {
            if (bitmap != null)
                bitmap.recycle();
        }
    }

    public static Bitmap capturePicturePart(WebView web) {
        Bitmap bitmap = Bitmap.createBitmap(web.getWidth(), web.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        web.draw(canvas);
        return bitmap;
    }

    public static boolean savePicturePart(WebView web, File file) throws IOException {
        Bitmap bitmap = null;
        try {
            bitmap = capturePicturePart(web);
            return ImageUtils.saveBitmap(bitmap, file);
        } finally {
            if (bitmap != null)
                bitmap.recycle();
        }
    }

    public static void enablePlatformNotifications() {
        Method method = ReflectionUtils.getMethod(WebView.class, "enablePlatformNotifications");
        ReflectionUtils.invokeMethod(method, null);
    }

    public static void disablePlatformNotifications() {
        Method method = ReflectionUtils.getMethod(WebView.class, "disablePlatformNotifications");
        ReflectionUtils.invokeMethod(method, null);
    }
}
