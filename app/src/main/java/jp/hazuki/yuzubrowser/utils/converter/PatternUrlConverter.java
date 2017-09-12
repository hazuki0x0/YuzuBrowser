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

package jp.hazuki.yuzubrowser.utils.converter;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.regex.PatternSyntaxException;

import jp.hazuki.yuzubrowser.pattern.PatternAction;
import jp.hazuki.yuzubrowser.pattern.PatternChecker;
import jp.hazuki.yuzubrowser.pattern.PatternManager;

public class PatternUrlConverter {

    public void convert(Context context) {
        ConvertManager manager = new ConvertManager(context, "url_1.dat");
        manager.load(context);
        manager.save(context);
    }

    private static class ConvertManager extends PatternManager<ConvertChecker> {
        ConvertManager(Context context, String file) {
            super(context, file);
        }

        @Override
        protected ConvertChecker newInstance(JsonParser parser) throws IOException {
            return new ConvertChecker(parser);
        }
    }

    private static class ConvertChecker extends PatternChecker {
        private String convertedString;

        ConvertChecker(JsonParser parser) throws PatternSyntaxException, IOException {
            super(PatternAction.newInstance(parser));
            //TODO not set mPattern
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if ("0".equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                    setConvertedString(parser.getText());
                    continue;
                }
                parser.skipChildren();
            }
        }

        private void setConvertedString(String s) {
            if (s.startsWith("?")) {
                convertedString = "[" + s.substring(1) + "]";
            } else {
                convertedString = s.replace("?", "\\?").replace("#", "\\#");
            }
        }

        @Override
        public String getTitle(Context context) {
            return convertedString;
        }

        @Override
        public boolean isEnable() {
            return false;
        }

        @Override
        public void setEnable(boolean enable) {

        }

        @Override
        public boolean write(JsonGenerator generator) throws IOException {
            getAction().write(generator);
            generator.writeStartObject();
            generator.writeStringField("0", convertedString);
            generator.writeEndObject();
            return true;
        }
    }
}
