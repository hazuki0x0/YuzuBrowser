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

package jp.hazuki.yuzubrowser.legacy.pattern.url;

import android.content.Context;

import com.squareup.moshi.JsonReader;

import java.io.IOException;

import jp.hazuki.yuzubrowser.adblock.filter.fastmatch.FastMatcherFactory;
import jp.hazuki.yuzubrowser.legacy.pattern.PatternManager;

public class PatternUrlManager extends PatternManager<PatternUrlChecker> {

    private FastMatcherFactory factory;

    public PatternUrlManager(Context context) {
        super(context, "url_1.dat");
        factory = new FastMatcherFactory();
    }

    @Override
    protected PatternUrlChecker newInstance(JsonReader reader) throws IOException {
        if (factory == null) factory = new FastMatcherFactory();
        return new PatternUrlChecker(reader, factory);
    }

    @Override
    public boolean load(Context context) {
        boolean result = super.load(context);
        if (factory != null)
            factory.release();
        return result;
    }
}
