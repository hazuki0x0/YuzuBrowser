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

package jp.hazuki.yuzubrowser.legacy.action.manager;

import android.content.Context;

import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.ActionFile;

public class SingleActionFile extends ActionFile {
    private static final long serialVersionUID = 3216383296384940721L;

    private final String FOLDER_NAME;

    private final int id;
    public Action action = new Action();

    public SingleActionFile(String folder_name, int id) {
        this.id = id;
        FOLDER_NAME = folder_name;
    }

    @Override
    @NonNull
    public File getFile(@NonNull Context context) {
        return new File(context.getDir(FOLDER_NAME, Context.MODE_PRIVATE), id + ".dat");
    }

    @Override
    public void reset() {
        action.clear();
    }

    @Override
    public boolean load(@NonNull JsonReader reader) throws IOException {
        return action.loadAction(reader);
    }

    @Override
    public boolean write(@NonNull JsonWriter writer) throws IOException {
        action.writeAction(writer);
        return true;
    }

}
