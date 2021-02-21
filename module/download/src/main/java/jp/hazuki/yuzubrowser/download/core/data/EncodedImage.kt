/*
 * Copyright (C) 2017-2021 Hazuki
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

package jp.hazuki.yuzubrowser.download.core.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import jp.hazuki.yuzubrowser.download.core.utils.Base64Image

class EncodedImage(
    val mimeType: String,
    val image: Bitmap,
) {
    companion object {
        operator fun invoke(url: String): EncodedImage? {
            val imageData = Base64Image(url)
            if (!imageData.isValid) return null
            val buffer = Base64.decode(imageData.getData(), Base64.DEFAULT)
            val image = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
            return EncodedImage(imageData.mimeType, image)
        }
    }
}
