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
import android.text.TextPaint;
import android.text.TextUtils;

import java.net.IDN;

public class UrlUtils {

    public static String decodeUrl(String url) {
        if (url == null) return null;
        return decodeUrl(Uri.parse(url));
    }

    public static String decodeUrl(Uri uri) {
        if (uri.isOpaque()) {
            StringBuilder builder = new StringBuilder(uri.getScheme()).append(":");
            if (isValid(uri.getSchemeSpecificPart())) {
                builder.append(uri.getSchemeSpecificPart());
            } else {
                builder.append(uri.getEncodedSchemeSpecificPart());
            }
            String fragment = uri.getFragment();
            if (!TextUtils.isEmpty(fragment)) {
                builder.append("#");
                if (isValid(fragment)) {
                    builder.append(fragment);
                } else {
                    builder.append(uri.getEncodedFragment());
                }
            }
            return builder.toString();
        } else {
            Uri.Builder decode = uri.buildUpon();
            if (isValid(uri.getQuery()))
                decode.encodedQuery(uri.getQuery());
            if (isValid(uri.getFragment()))
                decode.encodedFragment(uri.getFragment());
            if (isValid(uri.getPath()))
                decode.encodedPath(uri.getPath());
            decode.encodedAuthority(decodeAuthority(uri));
            return decode.build().toString();
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

    public static CharSequence ellipsizeUrl(CharSequence text, TextPaint p, float avail) {
        int len = text.length();

        float wid = p.measureText(text, 0, len);
        if (wid <= avail) {
            return text;
        }

        int fit = p.breakText(text, 0, len, true, avail, null);

        return text.toString().substring(0, fit);
    }

    private static final char INVALID_CHAR = '\uFFFD';

    private static boolean isValid(String str) {
        return str != null && !(str.indexOf(INVALID_CHAR) > -1);
    }

    public static boolean isSpeedDial(String url) {
        return "yuzu:speeddial".equalsIgnoreCase(url);
    }
}
