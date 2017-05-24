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
        Base64Image image = decodeBase64Image(data);
        if (image.isValid())
            return saveBase64Image(image, getFile(image));
        return null;
    }

    static File saveBase64Image(Base64Image imageData, File file) {
        if (imageData.isValid()) {
            byte[] image = Base64.decode(imageData.getData(), Base64.DEFAULT);

            if (file == null)
                file = getFile(imageData);

            if (file == null)
                return null;

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

    static File getFile(Base64Image image) {
        if (!image.isValid()) return null;

        String mimeType = image.getHeader().split(";")[0].substring(5);
        String name = System.currentTimeMillis() + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        return FileUtils.createUniqueFile(AppData.download_folder.get(), name);
    }

    static Base64Image decodeBase64Image(String url) {
        return new Base64Image(url);
    }

    static class Base64Image {
        private final String[] data;

        private Base64Image(String url) {
            data = url.split(Pattern.quote(","));
        }

        boolean isValid() {
            return data.length >= 2;
        }

        private String getHeader() {
            return data[0];
        }

        private String getData() {
            return data[1];
        }
    }
}
