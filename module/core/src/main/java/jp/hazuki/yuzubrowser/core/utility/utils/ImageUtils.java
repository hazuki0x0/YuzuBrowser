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

package jp.hazuki.yuzubrowser.core.utility.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.core.content.res.ResourcesCompat;

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
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), drawableId, context.getTheme());

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
            if (!parent.exists() && !parent.mkdirs())
                return false;

        try (OutputStream os = new FileOutputStream(file)) {
            bitmap.compress(CompressFormat.PNG, 100, os);
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

    public static Drawable getDrawable(Context context, Bitmap bitmap) {
        if (bitmap == null) return null;

        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        return drawable;
    }

    public static Bitmap trimSquare(Bitmap icon, int maxSize) {
        if (icon == null) return null;

        int x = icon.getWidth();
        int y = icon.getHeight();

        if (x == y && x <= maxSize) {
            return icon;
        }

        float scale;
        if (Math.max(x, y) <= maxSize) {
            scale = 1f;
        } else {
            scale = Math.max((float) maxSize / x, (float) maxSize / y);
        }
        int size = Math.min(x, y);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(icon, (x - size) / 2, (y - size) / 2, size, size, matrix, true);
    }
}
