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

import com.squareup.moshi.JsonReader;

import java.io.File;
import java.io.IOException;

import jp.hazuki.yuzubrowser.legacy.utils.matcher.AbstractPatternManager;

public class ResourceBlockManager extends AbstractPatternManager<ResourceChecker> {
    private static final String FOLDER_NAME = "resblock1";

    public ResourceBlockManager(Context context) {
        super(context, new File(context.getDir(FOLDER_NAME, Context.MODE_PRIVATE), "1.dat"));
    }

    @Override
    protected ResourceChecker newInstance(JsonReader reader) throws IOException {
        return ResourceChecker.newInstance(reader);
    }
}
