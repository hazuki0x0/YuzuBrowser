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

package jp.hazuki.yuzubrowser.utils;

import android.net.Uri;

import java.net.IDN;

public class UrlUtils {

    public static String decodeUrl(String url) {
        if (url == null) return null;
        return Uri.decode(decodeUrl(Uri.parse(url)).toString());
    }

    public static Uri decodeUrl(Uri uri) {
        String host = uri.getAuthority();
        if (host != null) {
            return uri.buildUpon()
                    .authority(decodePunyCode(host))
                    .build();
        } else {
            return uri;
        }
    }

    public static String decodeUrlHost(String url) {
        String host = Uri.parse(url).getHost();
        if (host != null)
            return decodePunyCode(host);
        return null;
    }

    private static String decodePunyCode(String domain) {
        return IDN.toUnicode(domain);
    }
}
