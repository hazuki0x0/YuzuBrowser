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

package jp.hazuki.yuzubrowser.legacy.utils.matcher;

import android.content.Context;
import android.widget.Toast;

import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import okio.Okio;

public abstract class AbstractPatternManager<T extends AbstractPatternChecker<?>> {
    private final File mFile;
    private final ArrayList<T> mList = new ArrayList<>();

    public AbstractPatternManager(Context context, File file) {
        mFile = file;
        load(context);
    }

    public ArrayList<T> getList() {
        return mList;
    }

    public T remove(int index) {
        return mList.remove(index);
    }

    public T get(int index) {
        return mList.get(index);
    }

    public int getIndex(T object) {
        return mList.indexOf(object);
    }

    public void add(T object) {
        mList.add(object);
    }

    public void add(int index, T object) {
        mList.add(index, object);
    }

    public void set(T from, T to) {
        mList.set(mList.indexOf(from), to);
    }

    public void set(int id, T to) {
        mList.set(id, to);
    }

    protected abstract T newInstance(JsonReader reader) throws IOException;

    public boolean load(Context context) {
        mList.clear();

        if (!mFile.exists() || !mFile.isFile())
            return true;

        try (JsonReader reader = JsonReader.of(Okio.buffer(Okio.source(mFile)))) {
            if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) return false;
            reader.beginArray();
            while (reader.hasNext()) {
                if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) return false;
                reader.beginArray();
                mList.add(newInstance(reader));
                if (reader.peek() != JsonReader.Token.END_ARRAY) return false;
                reader.endArray();
            }
            reader.endArray();
        } catch (PatternSyntaxException | IOException e) {
            ErrorReport.printAndWriteLog(e);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public boolean save(Context context) {
        try (JsonWriter writer = JsonWriter.of(Okio.buffer(Okio.sink(mFile)))) {
            writer.beginArray();
            for (T item : mList) {
                if (item != null) {
                    writer.beginArray();
                    item.write(writer);
                    writer.endArray();
                }
            }
            writer.endArray();
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
