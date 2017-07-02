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

package jp.hazuki.yuzubrowser.speeddial;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import java.io.Serializable;

import jp.hazuki.yuzubrowser.utils.ImageUtils;

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
        return new String(Base64.encode(iconBytes, Base64.DEFAULT));
    }

    public Bitmap getBitmap() {
        return BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
    }

    public static WebIcon createIcon(Bitmap icon) {
        if (icon == null) return null;

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
