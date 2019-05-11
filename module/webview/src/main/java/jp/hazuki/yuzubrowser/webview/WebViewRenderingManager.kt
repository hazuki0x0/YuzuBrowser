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

package jp.hazuki.yuzubrowser.webview

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View
import jp.hazuki.yuzubrowser.core.utility.utils.ColorFilterUtils

class WebViewRenderingManager {

    private var colorTemp: Int = 0
    private var nightBright: Int = 0

    private val negativePaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint().apply { colorFilter = ColorMatrixColorFilter(NEGATIVE_COLOR) }
    }

    private val grayPaint by lazy(LazyThreadSafetyMode.NONE) {
        val grayScale = ColorMatrix()
        grayScale.setSaturation(0f)
        Paint().apply { colorFilter = ColorMatrixColorFilter(grayScale) }
    }

    private val negativeGrayFilter by lazy(LazyThreadSafetyMode.NONE) {
        val negative = ColorMatrix()
        negative.set(NEGATIVE_COLOR)
        val grayScale = ColorMatrix()
        grayScale.setSaturation(0f)
        val matrix = ColorMatrix()
        matrix.setConcat(negative, grayScale)
        Paint().apply { ColorMatrixColorFilter(matrix) }
    }

    private val temperaturePaintDelegate = lazy(LazyThreadSafetyMode.NONE) {
        Paint().apply { colorFilter = ColorMatrixColorFilter(ColorFilterUtils.colorTemperatureToMatrix(colorTemp, nightBright)) }
    }

    var defaultMode = 0

    fun onPreferenceReset(defaultMode: Int, colorTemp: Int, nightBright: Int) {
        this.defaultMode = defaultMode
        this.colorTemp = colorTemp
        this.nightBright = nightBright
        if (temperaturePaintDelegate.isInitialized()) {
            temperaturePaintDelegate.value.colorFilter =
                ColorMatrixColorFilter(ColorFilterUtils.colorTemperatureToMatrix(colorTemp, nightBright))
        }
    }

    fun setWebViewRendering(webView: CustomWebView, mode: Int = defaultMode) {
        if (webView.renderingMode == mode) return
        webView.renderingMode = mode
        when (mode) {
            RENDERING_NORMAL -> webView.setLayerType(View.LAYER_TYPE_NONE, null)
            RENDERING_INVERT -> webView.setLayerType(View.LAYER_TYPE_HARDWARE, negativePaint)
            RENDERING_GRAY -> webView.setLayerType(View.LAYER_TYPE_HARDWARE, grayPaint)
            RENDERING_INVERT_GRAY -> webView.setLayerType(View.LAYER_TYPE_HARDWARE, negativeGrayFilter)
            RENDERING_TEMPERATURE -> webView.setLayerType(View.LAYER_TYPE_HARDWARE, temperaturePaintDelegate.value)
            else -> throw IllegalArgumentException("mode: $mode is not mode value")
        }
    }

    companion object {
        private val NEGATIVE_COLOR = floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f)

        const val RENDERING_NORMAL = 0
        const val RENDERING_INVERT = 1
        const val RENDERING_GRAY = 2
        const val RENDERING_INVERT_GRAY = 3
        const val RENDERING_TEMPERATURE = 4
    }
}
