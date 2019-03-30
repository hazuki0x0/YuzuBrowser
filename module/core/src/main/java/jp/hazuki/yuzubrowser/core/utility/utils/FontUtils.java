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

public final class FontUtils {
    private static final int SIZE_XXLARGE = 28;
    private static final int SIZE_XLARGE = 22;
    private static final int SIZE_LARGE = 20;
    private static final int SIZE_MEDIUM = 16;
    private static final int SIZE_SMALL = 14;
    private static final int SIZE_XSMALL = 10;
    private static final int SIZE_XXSMALL = 8;

    public static int getTextSize(int size) {
        if (size < 0) {
            return -1;
        }

        switch (size) {
            case 0:
                return SIZE_XXSMALL;
            case 1:
                return SIZE_XSMALL;
            case 2:
                return SIZE_SMALL;
            case 3:
                return SIZE_MEDIUM;
            case 4:
                return SIZE_LARGE;
            case 5:
                return SIZE_XLARGE;
            case 6:
                return SIZE_XXLARGE;
            default:
                return SIZE_MEDIUM;
        }
    }

    public static int getSmallerTextSize(int size) {
        if (size <= 0) {
            return getTextSize(size);
        } else {
            return getTextSize(size - 1);
        }
    }
}
