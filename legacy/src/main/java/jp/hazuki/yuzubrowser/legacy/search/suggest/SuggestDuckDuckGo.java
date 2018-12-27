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

package jp.hazuki.yuzubrowser.legacy.search.suggest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SuggestDuckDuckGo implements ISuggest {

    private static final String SUGGEST_URL_DUCK = "https://duckduckgo.com/ac/?q=";

    @Override
    public URL getUrl(String query) throws IOException {
        return new URL(SUGGEST_URL_DUCK + URLEncoder.encode(query, "UTF-8"));
    }

    @Override
    public List<Suggestion> getSuggestions(JsonParser parser) throws IOException {
        ArrayList<Suggestion> list = new ArrayList<>();

        if (parser.nextToken() == JsonToken.START_ARRAY) {
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        String name = parser.getCurrentName();
                        parser.nextToken();

                        if ("phrase".equals(name)) {
                            list.add(new Suggestion(parser.getText()));
                        }
                    }
                }
            }
        }

        return list;
    }
}
