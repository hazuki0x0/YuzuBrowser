package jp.hazuki.yuzubrowser.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatDrawableManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageUtils {
    private ImageUtils() {
        throw new UnsupportedOperationException();
    }

    public static byte[] convertToByteArray(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.WEBP, 100, os);
        return os.toByteArray();
    }

    public static Bitmap convertToBitmap(byte[] bin) {
        if (bin == null)
            return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeByteArray(bin, 0, bin.length, options);
    }

    /**
     * bitmapをバイト配列に変換します。
     *
     * @param bitmap      ビットマップ
     * @param format      圧縮フォーマット
     * @param compressVal 圧縮率
     * @return
     */
    public static byte[] bmp2byteArray(Bitmap bitmap, CompressFormat format, int compressVal) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, compressVal, baos);
        return baos.toByteArray();

    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static boolean saveBitmap(Bitmap bitmap, File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null)
            parent.mkdirs();

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(CompressFormat.PNG, 100, os);
        } finally {
            if (os != null)
                os.close();
        }
        return true;
    }

    private static final int NO_COLOR = 0x00000001;
    private static final int TRANSPARENT_COLOR = 0x00000000;

    public static byte[] makeNinePatchChunk(Rect expandArea, Rect paddingArea) {
        ByteBuffer buffer = ByteBuffer.allocate(56).order(ByteOrder.nativeOrder());
        // was not serialized
        buffer.put((byte) 0x01);
        // divX array length
        buffer.put((byte) 0x02);
        // divY array length
        buffer.put((byte) 0x02);
        // color array length
        buffer.put((byte) 0x02);

        // skip 8 bytes
        buffer.putInt(0);
        buffer.putInt(0);

        // padding
        buffer.putInt(paddingArea.left);
        buffer.putInt(paddingArea.right);
        buffer.putInt(paddingArea.top);
        buffer.putInt(paddingArea.bottom);

        // skip 4 bytes
        buffer.putInt(0);

        // divX array
        buffer.putInt(expandArea.left);
        buffer.putInt(expandArea.right);
        // divY array
        buffer.putInt(expandArea.top);
        buffer.putInt(expandArea.bottom);

        // color array
        buffer.putInt(TRANSPARENT_COLOR);
        buffer.putInt(TRANSPARENT_COLOR);

        return buffer.array();
    }
}
