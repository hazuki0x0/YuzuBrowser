/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.hazuki.yuzubrowser.settings.data.AppData;

public final class HttpUtils {

    private static final Pattern NAME_UTF_8 = Pattern.compile("filename\\*=UTF-8''(\\S+)");
    private static final Pattern NAME_NORMAL = Pattern.compile("filename=\"(.*)\"");

    public static File getFileName(String url, String defaultExt, Map<String, List<String>> header) {
        if (header.get("Content-Disposition") != null) {
            List<String> lines = header.get("Content-Disposition");
            for (String raw : lines) {
                if (raw != null) {
                    Matcher utf8 = NAME_UTF_8.matcher(raw);
                    if (utf8.find()) { /* RFC 6266 */
                        return FileUtils.createUniqueFile(AppData.download_folder.get(), utf8.group(1).replace("%20", " "));
                    }
                    Matcher normal = NAME_NORMAL.matcher(raw);
                    if (normal.find()) {
                        try {
                            return FileUtils.createUniqueFile(AppData.download_folder.get(), URLDecoder.decode(normal.group(1), "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            return FileUtils.createUniqueFile(AppData.download_folder.get(), normal.group(1));
                        }
                    }
                }
            }
        }

        if (header.get("Content-Type") != null) {
            List<String> lines = header.get("Content-Type");
            if (lines.size() > 0) {
                String mineType = lines.get(0);
                int index = mineType.indexOf(';');
                if (index > -1) {
                    mineType = mineType.substring(0, index);
                }
                return WebDownloadUtils.guessDownloadFile(AppData.download_folder.get(), url, null, mineType, defaultExt);
            }
        }

        return WebDownloadUtils.guessDownloadFile(AppData.download_folder.get(), url, null, null, defaultExt);
    }

    public static Bitmap getImage(String url, String userAgent, String referrer) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.addRequestProperty("Referer", referrer);
            try (InputStream is = conn.getInputStream()) {
                return BitmapFactory.decodeStream(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
