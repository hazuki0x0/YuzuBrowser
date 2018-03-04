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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.io.File;
import java.util.regex.Pattern;

public class WebDownloadUtils {
    private WebDownloadUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean shouldOpen(String contentDisposition) {
        return (contentDisposition == null || !contentDisposition.regionMatches(true, 0, "attachment", 0, 10));
    }

    public static boolean openFile(Context context, String url, String mimetype) {
        if (url.startsWith("data:")) return false;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), mimetype);

        if (context.getPackageManager().queryIntentActivities(intent, 0).isEmpty())
            return false;

        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    public static File guessDownloadFile(String folder_path, String url, String contentDisposition, String mimetype) {
        return guessDownloadFile(folder_path, url, contentDisposition, mimetype, null);
    }

    public static File guessDownloadFile(String folder_path, String url, String contentDisposition, String mimetype, String defaultExt) {
        if (url != null && url.startsWith("data:")) {
            String[] data = url.split(Pattern.quote(","));
            if (data.length > 1) {
                mimetype = data[0].split(Pattern.quote(";"))[0].substring(5);
            }
        }
        if ("application/octet-stream".equals(mimetype))
            mimetype = null;

        String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
        if (TextUtils.isEmpty(filename)) {
            filename = "index.html";
        }

        if (filename.endsWith(".htm")) {
            filename += "l";
        }

        if (filename.endsWith(".bin") && mimetype != null && defaultExt == null) {
            switch (mimetype) {
                case "multipart/related":
                case "message/rfc822":
                case "application/x-mimearchive":
                    defaultExt = ".mhtml";
                    break;
                case "application/javascript":
                case "application/x-javascript":
                case "text/javascript":
                    defaultExt = ".js";
                    break;
            }
        }

        if (filename.endsWith(".bin") && defaultExt != null) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null) {
                int queryIndex = decodedUrl.indexOf('?');
                // If there is a query string strip it, same as desktop browsers
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex);
                }
                if (!decodedUrl.endsWith("/")) {
                    int index = decodedUrl.lastIndexOf('/') + 1;
                    if (index > 0) {
                        filename = decodedUrl.substring(index);
                        if (filename.indexOf('.') < 0) {
                            filename = filename + defaultExt;
                        }
                    }
                }
            }
        }

        return FileUtils.createUniqueFile(folder_path, filename);
    }


}
