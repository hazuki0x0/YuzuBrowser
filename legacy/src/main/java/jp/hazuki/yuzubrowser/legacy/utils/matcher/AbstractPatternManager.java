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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.legacy.utils.JsonUtils;

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

    protected abstract T newInstance(JsonParser parser) throws IOException;

    public boolean load(Context context) {
        mList.clear();

        if (!mFile.exists() || !mFile.isFile())
            return true;

        try (InputStream is = new BufferedInputStream(new FileInputStream(mFile));
             JsonParser parser = JsonUtils.getFactory().createParser(is)) {

            if (parser.nextToken() != JsonToken.START_ARRAY) return false;
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.getCurrentToken() != JsonToken.START_ARRAY) return false;
                mList.add(newInstance(parser));
                if (parser.nextToken() != JsonToken.END_ARRAY) return false;
            }
            return true;
        } catch (PatternSyntaxException | IOException e) {
            ErrorReport.printAndWriteLog(e);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public boolean save(Context context) {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(mFile));
             JsonGenerator generator = JsonUtils.getFactory().createGenerator(os)) {

            generator.writeStartArray();
            for (T item : mList) {
                if (item != null) {
                    generator.writeStartArray();
                    item.write(generator);
                    generator.writeEndArray();
                }
            }
            generator.writeEndArray();

            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
