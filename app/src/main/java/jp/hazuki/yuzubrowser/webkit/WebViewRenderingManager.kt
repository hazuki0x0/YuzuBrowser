/*
 * Copyright (C) 2017-2018 Hazuki
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

package jp.hazuki.yuzubrowser.webkit

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View

import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.ColorFilterUtils

class WebViewRenderingManager {

    private var colorTemp: Int = 0
    private var nightBright: Int = 0

    private val paint = Paint()
    var mode: Int = 0
        set(mode) {
            field = mode
            isInverted = isInvertMode

            paint.colorFilter = when (mode) {
                0 -> null
                1 -> ColorMatrixColorFilter(NEGATIVE_COLOR)
                2 -> {
                    val grayScale = ColorMatrix()
                    grayScale.setSaturation(0f)
                    ColorMatrixColorFilter(grayScale)
                }
                3 -> {
                    val negative = ColorMatrix()
                    negative.set(NEGATIVE_COLOR)
                    val grayScale = ColorMatrix()
                    grayScale.setSaturation(0f)
                    val matrix = ColorMatrix()
                    matrix.setConcat(negative, grayScale)
                    ColorMatrixColorFilter(matrix)
                }
                4 -> ColorMatrixColorFilter(ColorFilterUtils.colorTemperatureToMatrix(colorTemp, nightBright))
                else -> null
            }
        }

    val isInvertMode: Boolean
        get() = mode == 1 || mode == 3

    var isInverted = false
        private set

    fun onPreferenceReset() {
        colorTemp = AppData.night_mode_color.get()
        nightBright = AppData.night_mode_bright.get()
        mode = AppData.rendering.get()
    }

    fun setWebViewRendering(webView: CustomWebView) {
        if (this.mode == 0) {
            webView.setLayerType(View.LAYER_TYPE_NONE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        }
    }

    companion object {
        private val NEGATIVE_COLOR = floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f)
    }
}
