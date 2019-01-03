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

package jp.hazuki.yuzubrowser.legacy.resblock;

import android.content.Context;
import android.webkit.WebResourceResponse;

import com.squareup.moshi.JsonReader;

import java.io.IOException;

import jp.hazuki.yuzubrowser.legacy.resblock.data.EmptyImageData;
import jp.hazuki.yuzubrowser.legacy.resblock.data.EmptyStringData;
import jp.hazuki.yuzubrowser.legacy.utils.matcher.AbstractPatternAction;

public abstract class ResourceData extends AbstractPatternAction {
    protected static final int EMPTY_STRING_DATA = 0;
    protected static final int EMPTY_IMAGE_DATA = 1;

    public abstract int getTypeId();

    public abstract WebResourceResponse getResource(Context context);

    public static ResourceData newInstance(JsonReader reader) throws IOException {
        if (reader.peek() != JsonReader.Token.NUMBER) return null;
        switch (reader.nextInt()) {
            case EMPTY_STRING_DATA:
                return new EmptyStringData(reader);
            case EMPTY_IMAGE_DATA:
                return new EmptyImageData(reader);
        }
        return null;
    }
}
