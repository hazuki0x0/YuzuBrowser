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

package jp.hazuki.yuzubrowser.webkit;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;

import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.ColorFilterUtils;

public class WebViewRenderingManager {
    private static final float[] NEGATIVE_COLOR = new float[]{
            -1, 0, 0, 0, 255,
            0, -1, 0, 0, 255,
            0, 0, -1, 0, 255,
            0, 0, 0, 1, 0};

    private int colorTemp;
    private int nightBright;

    private final Paint paint = new Paint();
    private int mode;

    public void onPreferenceReset() {
        colorTemp = AppData.night_mode_color.get();
        nightBright = AppData.night_mode_bright.get();
        setMode(AppData.rendering.get());
    }

    public void setMode(int mode) {
        this.mode = mode;
        switch (mode) {
            default:
            case 0:
                paint.setColorFilter(null);
                break;
            case 1:
                paint.setColorFilter(new ColorMatrixColorFilter(NEGATIVE_COLOR));
                break;
            case 2: {
                ColorMatrix grayScale = new ColorMatrix();
                grayScale.setSaturation(0);
                paint.setColorFilter(new ColorMatrixColorFilter(grayScale));
                break;
            }
            case 3: {
                ColorMatrix negative = new ColorMatrix();
                negative.set(NEGATIVE_COLOR);
                ColorMatrix grayScale = new ColorMatrix();
                grayScale.setSaturation(0);
                ColorMatrix matrix = new ColorMatrix();
                matrix.setConcat(negative, grayScale);
                paint.setColorFilter(new ColorMatrixColorFilter(matrix));
                break;
            }
            case 4:
                paint.setColorFilter(new ColorMatrixColorFilter(ColorFilterUtils.colorTemperatureToMatrix(colorTemp, nightBright)));
                break;
        }
    }

    public int getMode() {
        return mode;
    }

    public void setWebViewRendering(CustomWebView webView) {
        if (mode == 0) {
            webView.setLayerType(View.LAYER_TYPE_NONE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
        }
    }

}
