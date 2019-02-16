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

package jp.hazuki.yuzubrowser.core.android.utils

import android.graphics.Bitmap
import jp.hazuki.yuzubrowser.core.utility.hash.getVectorHash
import jp.hazuki.yuzubrowser.core.utility.hash.murmur3Hash64
import java.nio.ByteBuffer


fun Bitmap.calcImageHash(): Long {
    val longSide = Math.max(height, width)
    if (longSide > 64) {
        val newImage = Bitmap.createScaledBitmap(this, 64, 64, true)
        return newImage.calcImageHash()
    }

    val buffer = ByteBuffer.allocate(byteCount)
    copyPixelsToBuffer(buffer)
    return buffer.array().murmur3Hash64()
}

fun Bitmap.calcImageVectorHash(): Long {
    val bmpVector: Bitmap
    if (config === Bitmap.Config.ARGB_8888 || config === Bitmap.Config.RGB_565) {
        bmpVector = Bitmap.createScaledBitmap(this, 9, 8, true)
    } else {
        val cache = copy(Bitmap.Config.ARGB_8888, false)
        bmpVector = Bitmap.createScaledBitmap(cache, 9, 8, true)
        cache.recycle()
    }

    val buffer = ByteBuffer.allocate(byteCount)
    copyPixelsToBuffer(buffer)

    bmpVector.recycle()
    return buffer.array().getVectorHash()
}