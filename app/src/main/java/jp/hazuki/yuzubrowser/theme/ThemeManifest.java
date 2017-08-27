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

package jp.hazuki.yuzubrowser.theme;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.hazuki.yuzubrowser.utils.JsonUtils;

public class ThemeManifest {
    public static final String MANIFEST = "manifest.json";

    private static final int FORMAT_VERSION = 1;

    private static final String FIELD_FORMAT_VERSION = "format_version";
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_ID = "id";

    private String version;
    private String name;
    private String id;

    public ThemeManifest(JsonParser parser) throws IllegalManifestException, IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT)
            throw new IllegalManifestException("broken manifest file", 1);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentToken() != JsonToken.FIELD_NAME)
                throw new IllegalManifestException("broken manifest file", 1);
            String field = parser.getText();
            parser.nextToken();

            if (FIELD_FORMAT_VERSION.equalsIgnoreCase(field)) {
                if (parser.getIntValue() > FORMAT_VERSION)
                    throw new IllegalManifestException("unknown version of format", 2);
                continue;
            }

            if (FIELD_VERSION.equalsIgnoreCase(field)) {
                version = parser.getText().trim();
                continue;
            }

            if (FIELD_NAME.equalsIgnoreCase(field)) {
                name = parser.getText().trim();
                continue;
            }

            if (FIELD_ID.equalsIgnoreCase(field)) {
                id = parser.getText().trim();
                continue;
            }

            if (parser.getCurrentToken() != JsonToken.START_OBJECT
                    && parser.getCurrentToken() != JsonToken.START_ARRAY) {
                parser.nextValue();
            } else {
                parser.skipChildren();
            }
        }

        if (version == null || name == null || id == null)
            throw new IllegalManifestException("broken manifest file", 1);
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public static ThemeManifest getManifest(File themeFolder) {
        try {
            return decodeManifest(new File(themeFolder, MANIFEST));
        } catch (IllegalManifestException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ThemeManifest decodeManifest(File manifestFile) throws ThemeManifest.IllegalManifestException {
        if (manifestFile.exists() && manifestFile.isFile()) {
            try (InputStream is = new BufferedInputStream(new FileInputStream(manifestFile));
                 JsonParser parser = JsonUtils.getFactory().createParser(is)) {
                return new ThemeManifest(parser);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ThemeManifest.IllegalManifestException("unknown error", 0);
            }
        } else {
            return null;
        }
    }

    public static class IllegalManifestException extends Exception {

        private final int errorType;

        IllegalManifestException(String message, int type) {
            super(message);
            errorType = type;
        }

        public int getErrorType() {
            return errorType;
        }
    }
}
