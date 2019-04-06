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

package jp.hazuki.yuzubrowser.legacy.search.settings;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

class ColorGenerator {

    static final int[] COLORS = {
            0xfff44336, 0xffe91e63, 0xff9c27b0, 0xff673ab7, 0xff3f51b5,
            0xff2196f3, 0xff03a9f4, 0xff00bcd4, 0xff009688, 0xff4caf50,
            0xff8bc34a, 0xffcddc39, 0xffffeb3b, 0xffffc107, 0xffff9800,
            0xffff5722, 0xff795548, 0xff9e9e9e, 0xff607d8b, 0xff212121
    };

    @ColorInt
    static int getColor(@NonNull String str) {
        return COLORS[Math.abs(str.hashCode() % COLORS.length)];
    }

    @ColorInt
    static int getRandomColor() {
        return COLORS[(int) (Math.random() * COLORS.length)];
    }
}
