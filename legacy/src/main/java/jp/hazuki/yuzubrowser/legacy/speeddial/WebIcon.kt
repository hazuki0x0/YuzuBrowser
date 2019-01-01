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

package jp.hazuki.yuzubrowser.legacy.speeddial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import jp.hazuki.yuzubrowser.core.utility.utils.ImageUtils
import java.io.Serializable

class WebIcon(val iconBytes: ByteArray) : Serializable {

    val iconBase64: String
        get() = String(Base64.encode(iconBytes, Base64.DEFAULT))

    val bitmap: Bitmap
        get() = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.size)

    private constructor(bitmap: Bitmap) : this(ImageUtils.bmp2byteArray(bitmap, Bitmap.CompressFormat.PNG, 100))

    companion object {

        @JvmStatic
        fun createIconOrNull(icon: Bitmap?): WebIcon? {
            return if (icon == null) null else WebIcon(ImageUtils.trimSquare(icon, 200))
        }

        fun createIcon(icon: Bitmap): WebIcon {
            return WebIcon(ImageUtils.trimSquare(icon, 200))
        }
    }
}
