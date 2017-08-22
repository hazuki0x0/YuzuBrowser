package jp.hazuki.yuzubrowser.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Region;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class WebViewUtils {
    private WebViewUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean shouldLoadSameTabAuto(String url) {
        return url.regionMatches(true, 0, "about:", 0, 6);
    }

    public static boolean shouldLoadSameTabScheme(String url) {
        return url.regionMatches(true, 0, "intent:", 0, 7)
                || url.regionMatches(true, 0, "yuzu:", 0, 5) && !UrlUtils.isSpeedDial(url);
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

        canvas.clipRect(scrollX, scrollY, width + scrollX, height + scrollY, Region.Op.REPLACE);
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

    public static void enablePlatformNotifications() {
        Method method = ReflectionUtils.getMethod(WebView.class, "enablePlatformNotifications");
        ReflectionUtils.invokeMethod(method, null);
    }

    public static void disablePlatformNotifications() {
        Method method = ReflectionUtils.getMethod(WebView.class, "disablePlatformNotifications");
        ReflectionUtils.invokeMethod(method, null);
    }

    public static boolean isRedirect(CustomWebView webView) {
        return webView.getHitTestResult().getType() <= 0;
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
