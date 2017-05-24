package jp.hazuki.yuzubrowser.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Pattern;

public class ImageUtils {
    private ImageUtils() {
        throw new UnsupportedOperationException();
    }

    public static byte[] convertToByteArray(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, os);
        return os.toByteArray();
    }

    public static Bitmap convertToBitmap(byte[] bin) {
        if (bin == null)
            return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeByteArray(bin, 0, bin.length, options);
    }

    public static byte[] bmp2byteArray(Bitmap bitmap, CompressFormat format, int compressVal) {
        if (bitmap == null)
            return null;
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


    public static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return ImageUtils.getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    public static byte[] getBase64Image(String data) {
        if (!data.startsWith("data:")) return null;
        String[] strings = data.split(Pattern.quote(","));
        if (strings.length > 2) {
            return Base64.decode(strings[1], Base64.DEFAULT);
        }
        return null;
    }
}
