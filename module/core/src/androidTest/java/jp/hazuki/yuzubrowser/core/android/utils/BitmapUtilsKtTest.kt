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
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class BitmapUtilsKtTest {

    @Test
    fun testCalcImageHash() {
        val smallImage = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        assertThat(smallImage.calcImageHash()).isEqualTo(6342350844925100937)

        val mediumImage = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        assertThat(mediumImage.calcImageHash()).isEqualTo(8383010643843876377)

        val bigImage = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        assertThat(mediumImage.calcImageHash()).isEqualTo(8383010643843876377)
    }
}