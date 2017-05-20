package jp.hazuki.yuzubrowser.speeddial;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import java.io.Serializable;

import jp.hazuki.yuzubrowser.utils.ImageUtils;

/**
 * Created by hazuki on 17/02/19.
 */

public class WebIcon implements Serializable {
    private final byte[] iconBytes;

    public WebIcon(byte[] icon) {
        iconBytes = icon;
    }

    private WebIcon(Bitmap bitmap) {
        iconBytes = ImageUtils.bmp2byteArray(bitmap, Bitmap.CompressFormat.PNG, 100);
    }

    public byte[] getIconBytes() {
        return iconBytes;
    }

    public String getIconBase64() {
        return "data:image/webp;base64," + new String(Base64.encode(iconBytes, Base64.DEFAULT));
    }

    public Bitmap getBitmap() {
        return BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
    }

    public static WebIcon createIcon(Bitmap icon) {
        int x = icon.getWidth();
        int y = icon.getHeight();

        if (x == y && x <= 200) {
            return new WebIcon(icon);
        }

        float scale;
        if (Math.max(x, y) <= 200) {
            scale = 1f;
        } else {
            scale = Math.max((float) 200 / x, (float) 200 / y);
        }
        int size = Math.min(x, y);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bmp = Bitmap.createBitmap(icon, (x - size) / 2, (y - size) / 2, size, size, matrix, true);

        return new WebIcon(bmp);
    }
}
