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

package jp.hazuki.yuzubrowser.download;

import android.util.Base64;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.FileUtils;

class DownloadUtils {

    static File saveBase64Image(String data) {
        return saveBase64Image(data, null);
    }

    static File saveBase64Image(String data, File file) {
        String[] raw = data.split(Pattern.quote(","));
        if (raw.length > 2) {
            String mimeType = raw[0].split(Pattern.quote(";"))[0].substring(5);

            byte[] image = Base64.decode(raw[1], Base64.DEFAULT);

            if (file == null) {
                String name = System.currentTimeMillis() + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                file = FileUtils.createUniqueFile(AppData.download_folder.get(), name);
            }

            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            try (OutputStream outputStream = new FileOutputStream(file)) {

                outputStream.write(image);
                outputStream.flush();

                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
