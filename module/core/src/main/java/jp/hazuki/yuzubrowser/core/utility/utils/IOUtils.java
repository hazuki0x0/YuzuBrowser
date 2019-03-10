/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.core.utility.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtils {
    private IOUtils() {
        throw new UnsupportedOperationException();
    }

    private static final int BUFFER_SIZE = 1024 * 8;

    public static byte[] readByte(InputStream is) throws IOException {
        byte buffer[] = new byte[BUFFER_SIZE];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int n;
        while ((n = is.read(buffer)) >= 0) {
            bout.write(buffer, 0, n);
        }
        return bout.toByteArray();
    }

    public static String readString(InputStream is) {
        return readString(is, "UTF-8");
    }

    public static String readString(InputStream is, String encode) {

        final char[] bytes = new char[BUFFER_SIZE];
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, encode), BUFFER_SIZE)) {
            int len;
            while ((len = reader.read(bytes, 0, BUFFER_SIZE)) > 0) {
                builder.append(bytes, 0, len);
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String readFile(File file, String encode) throws IOException {
        if (!file.exists() || !file.isFile())
            return null;

        return readString(new FileInputStream(file), encode);
    }
}
