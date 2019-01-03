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
import android.net.Uri;
import android.webkit.WebResourceResponse;

import com.squareup.moshi.JsonReader;

import java.io.IOException;

import jp.hazuki.yuzubrowser.legacy.resblock.checker.NormalChecker;
import jp.hazuki.yuzubrowser.legacy.utils.matcher.AbstractPatternChecker;

public abstract class ResourceChecker extends AbstractPatternChecker<ResourceData> {
    protected static final int NORMAL_CHECKER = 0;

    public static final int SHOULD_RUN = 0;
    public static final int SHOULD_BREAK = 1;
    public static final int SHOULD_CONTINUE = 2;

    protected ResourceChecker(ResourceData data) {
        super(data);
    }

    protected ResourceChecker(JsonReader reader) throws IOException {
        super(ResourceData.newInstance(reader));
    }

    public abstract int check(Uri url);

    public WebResourceResponse getResource(Context context) {
        return getAction().getResource(context);
    }

    public static ResourceChecker newInstance(JsonReader reader) throws IOException {
        if (reader.peek() != JsonReader.Token.NUMBER) return null;
        switch (reader.nextInt()) {
            case NORMAL_CHECKER:
                return new NormalChecker(reader);
        }
        return null;
    }
}
