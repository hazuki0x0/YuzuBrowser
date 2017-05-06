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
import android.text.TextUtils;

import java.net.IDN;

public class UrlUtils {

    public static String decodeUrl(String url) {
        if (url == null) return null;
        return decodeUrl(Uri.parse(url)).toString();
    }

    public static Uri decodeUrl(Uri uri) {
        Uri.Builder decode = uri.buildUpon();
        if (isValid(uri.getQuery()))
            decode.query(uri.getQuery());
        if (isValid(uri.getFragment()))
            decode.fragment(uri.getFragment());
        decode.authority(decodeAuthority(uri));
        return decode.build();
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

    private static String decodeAuthority(Uri uri) {
        String host = uri.getHost();
        if (TextUtils.isEmpty(host)) {
            return uri.getEncodedAuthority();
        } else {
            host = decodePunyCode(host);
        }

        String userInfo = uri.getUserInfo();
        boolean noUserInfo = TextUtils.isEmpty(userInfo);
        int port = uri.getPort();

        if (noUserInfo && port == -1)
            return host;

        StringBuilder builder = new StringBuilder();

        if (!noUserInfo) {
            if (isValid(userInfo)) {
                builder.append(userInfo).append("@");
            } else {
                builder.append(uri.getEncodedUserInfo()).append("@");
            }
        }

        builder.append(host);

        if (port > -1) {
            builder.append(":").append(port);
        }

        return builder.toString();
    }

    private static final char INVALID_CHAR = '\uFFFD';

    private static boolean isValid(String str) {
        return str != null && !(str.indexOf(INVALID_CHAR) > -1);
    }
}
