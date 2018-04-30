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

package jp.hazuki.yuzubrowser.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HttpUtils {

    public static Bitmap getImage(String url, String userAgent, String referrer) {
        return getImage(url, userAgent, referrer, null);
    }

    public static Bitmap getImage(String url, String userAgent, String referrer, String cookie) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            if (userAgent != null) {
                conn.setRequestProperty("User-Agent", userAgent);
            }
            if (referrer != null) {
                conn.setRequestProperty("Referer", referrer);
            }
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }
            try (InputStream is = conn.getInputStream()) {
                return BitmapFactory.decodeStream(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
