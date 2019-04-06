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

package jp.hazuki.yuzubrowser.legacy.utils;

public class HtmlUtils {

    public static String sanitize(String str) {
        int index = indexOfMeta(str, 0);
        if (index < 0) return str;

        StringBuilder sb = new StringBuilder(str.length());
        int offset = 0;

        do {
            sb.append(str, offset, index);
            switch (str.charAt(index)) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
            }
            offset = index + 1;
        } while (0 <= (index = indexOfMeta(str, offset)));

        sb.append(str, offset, str.length());
        return sb.toString();
    }


    private static int indexOfMeta(String str, int offset) {
        for (int i = offset; str.length() > i; i++) {
            switch (str.charAt(i)) {
                case '&':
                case '<':
                case '>':
                case '"':
                case '\'':
                    return i;
            }
        }
        return -1;
    }
}
