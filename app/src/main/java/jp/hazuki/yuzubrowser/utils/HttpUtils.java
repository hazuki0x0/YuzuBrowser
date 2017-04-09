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

import java.io.File;
import java.util.Map;

import jp.hazuki.yuzubrowser.settings.data.AppData;

public final class HttpUtils {

    public static File getFileName(String url, String defaultExt, Map header) {
        if (header.get("Content-Disposition") != null) {
            String raw = header.get("Content-Disposition").toString();
            // raw = "attachment; filename=abc.jpg"
            if (raw != null && raw.contains("=")) {
                // getting value after '='
                String fileName = raw.split("=")[1];
                fileName = fileName.replaceAll("\"", "").replaceAll("]", "");
                return FileUtils.createUniqueFile(AppData.download_folder.get(), fileName);
            }
        }

        return WebDownloadUtils.guessDownloadFile(AppData.download_folder.get(), url, null, null, defaultExt);
    }
}
