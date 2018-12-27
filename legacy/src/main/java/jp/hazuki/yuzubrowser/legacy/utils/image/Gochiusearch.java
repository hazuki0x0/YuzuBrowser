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

package jp.hazuki.yuzubrowser.legacy.utils.image;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;

public class Gochiusearch {

    /**
     * 画像ファイルから類似画像が近い値を持つようなハッシュ値を計算します
     *
     * @param bitmap 画像ファイル
     * @return ハッシュ値
     */
    public static long getVectorHash(Bitmap bitmap) {
        Bitmap bmpVector;
        if (bitmap.getConfig() == Bitmap.Config.ARGB_8888 || bitmap.getConfig() == Bitmap.Config.RGB_565) {
            bmpVector = Bitmap.createScaledBitmap(bitmap, 9, 8, true);
        } else {
            Bitmap cache = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            bmpVector = Bitmap.createScaledBitmap(cache, 9, 8, true);
            cache.recycle();
        }

        byte[] data = bitmapToByteArray(bmpVector);
        bmpVector.recycle();

        int[] mono = new int[data.length / 4];
        for (int i = 0; i < mono.length; i++) {
            mono[i] = (150 * data[i * 4 + 1] + 77 * data[i * 4 + 2] + 29 * data[i * 4 + 3]) >> 8;
        }

        long result = 0;
        int p = 0;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                result = (result << 1) | (mono[p] > mono[p + 1] ? 1 : 0);
                p++;
            }
            p++;
        }

        return result;
    }

    @NonNull
    public static String getHashString(long hash) {
        String str = "000000000000000" + Long.toHexString(hash);
        return str.substring(str.length() - 16);
    }

    public static long parseHashString(String hex) {
        int len = hex.length();
        if (len <= 15 || hex.charAt(0) == '0') {
            return Long.parseLong(hex, 16);
        } else {
            long first = Long.parseLong(hex.substring(0, len - 1), 16);
            int second = Character.digit(hex.charAt(len - 1), 16);
            return (first << 4) + second;
        }
    }

    /**
     * Bitmapをbyte[]に変換する
     *
     * @param bitmap 変換元のBitmap
     * @return 1 pixel = 4 byte (+0:A, +1:R, +2:G, +3:B) に変換したbyte配列
     */
    private static byte[] bitmapToByteArray(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        byte[] argbPixels = new byte[height * width * 4];
        ByteBuffer buffer = ByteBuffer.wrap(argbPixels);
        buffer.position(0);
        bitmap.copyPixelsToBuffer(buffer);

        return argbPixels;
    }
}
