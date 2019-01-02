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

public class SoftButtonActionFile extends ActionFile {
    //private static final String TAG = "ButtonActionList";

    private static final long serialVersionUID = 2904009975751614292L;

    //the same in attrs.xml
    public static final int FIELD_SWIPE_TYPE = 0x000F;

    public static final int BUTTON_SWIPE_PRESS = 0x0001;
    public static final int BUTTON_SWIPE_LPRESS = 0x0002;
    public static final int BUTTON_SWIPE_UP = 0x0003;
    public static final int BUTTON_SWIPE_DOWN = 0x0004;
    public static final int BUTTON_SWIPE_LEFT = 0x0005;
    public static final int BUTTON_SWIPE_RIGHT = 0x0006;

    private final String FOLDER_NAME;

    private final int id;
    public final Action press = new Action();
    public final Action lpress = new Action();
    public final Action up = new Action();
    public final Action down = new Action();
    public final Action left = new Action();
    public final Action right = new Action();

    public SoftButtonActionFile() {
        this.id = 0;
        FOLDER_NAME = null;
    }

    public SoftButtonActionFile(String folder_name, int id) {
        this.id = id;
        FOLDER_NAME = folder_name;
    }

    public Action getAction(int search_id) {
        //if((search_id & FIELD_BUTTON_TYPE) != id) return null;
        switch (search_id & FIELD_SWIPE_TYPE) {
            case BUTTON_SWIPE_PRESS:
                return press;
            case BUTTON_SWIPE_LPRESS:
                return lpress;
            case BUTTON_SWIPE_UP:
                return up;
            case BUTTON_SWIPE_DOWN:
                return down;
            case BUTTON_SWIPE_LEFT:
                return left;
            case BUTTON_SWIPE_RIGHT:
                return right;
            default:
                throw new IllegalArgumentException("Unknown id:" + search_id);
        }
    }

    @Override
    @NonNull
    public File getFile(@NonNull Context context) {
        return new File(context.getDir(FOLDER_NAME, Context.MODE_PRIVATE), id + ".dat");
    }

    @Override
    public void reset() {
        press.clear();
        lpress.clear();
        up.clear();
        down.clear();
        left.clear();
        right.clear();
    }

    @Override
    public boolean load(@NonNull JsonReader reader) throws IOException {
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) return false;
        reader.beginArray();
        if (!press.loadAction(reader)) return false;
        if (!lpress.loadAction(reader)) return false;
        if (!up.loadAction(reader)) return false;
        if (!down.loadAction(reader)) return false;
        if (!left.loadAction(reader)) return false;
        if (!right.loadAction(reader)) return false;
        if (reader.peek() == JsonReader.Token.END_ARRAY) {
            reader.endArray();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean write(@NonNull JsonWriter writer) throws IOException {
        writer.beginArray();
        press.writeAction(writer);
        lpress.writeAction(writer);
        up.writeAction(writer);
        down.writeAction(writer);
        left.writeAction(writer);
        right.writeAction(writer);
        writer.endArray();
        return true;
    }
}
