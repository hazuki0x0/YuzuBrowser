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

public class ColorFilterUtils {

    public static float[] colorTemperatureToMatrix(int kelvin, int brightness) {
        int[] rgb = colorTemperatureToRGB(kelvin);
        float bright = brightness / 100f / 255f;

        return new float[]{
                rgb[0] * bright, 0, 0, 0, 0,
                0, rgb[1] * bright, 0, 0, 0,
                0, 0, rgb[2] * bright, 0, 0,
                0, 0, 0, 1, 0};
    }

    public static int[] colorTemperatureToRGB(int kelvin) {
        if (kelvin < 1000 || kelvin > 40000) {
            throw new IllegalArgumentException();
        }
        final double temp = kelvin / 100D;

        int red;
        int green;
        int blue;
        if (temp <= 66) {
            red = 255;

            green = (int) (99.4708025861 * Math.log(temp) - 161.1195681661 + 0.5);

            if (temp <= 19) {
                blue = 0;
            } else {
                blue = (int) (138.5177312231 * Math.log(temp - 10) - 305.0447927307 + 0.5);
            }
        } else {
            red = (int) (329.698727446 * Math.pow(temp - 60, -0.1332047592) + 0.5);

            green = (int) (288.1221695283 * Math.pow(temp - 60, -0.0755148492));

            blue = 255;
        }

        return new int[]{
                Math.min(Math.max(red, 0), 255),
                Math.min(Math.max(green, 0), 255),
                Math.min(Math.max(blue, 0), 255)};
    }
}
