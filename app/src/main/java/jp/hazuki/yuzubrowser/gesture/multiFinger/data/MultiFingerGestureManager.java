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

package jp.hazuki.yuzubrowser.gesture.multiFinger.data;

import android.content.Context;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;

public class MultiFingerGestureManager {
    private static final String FILENAME = "multiFingerGes_1.dat";
    private final File JSON_FILE;
    private List<MultiFingerGestureItem> gestureItems;

    public MultiFingerGestureManager(Context context) {
        gestureItems = new ArrayList<>();
        JSON_FILE = context.getFileStreamPath(FILENAME);
        load();
    }

    public void add(MultiFingerGestureItem item) {
        gestureItems.add(item);
        save();
    }

    public void add(int index, MultiFingerGestureItem item) {
        gestureItems.add(index, item);
        save();
    }

    public void set(int index, MultiFingerGestureItem item) {
        gestureItems.set(index, item);
        save();
    }

    public MultiFingerGestureItem remove(int index) {
        MultiFingerGestureItem item = gestureItems.remove(index);
        save();
        return item;
    }

    public void remove(MultiFingerGestureItem item) {
        if (gestureItems.remove(item))
            save();
    }

    public void move(int from, int to) {
        ArrayUtils.move(gestureItems, from, to);
        save();
    }

    public int indexOf(MultiFingerGestureItem item) {
        return gestureItems.indexOf(item);
    }

    public List<MultiFingerGestureItem> getGestureItems() {
        return gestureItems;
    }

    private static final String JSON_FINGERS = "0";
    private static final String JSON_TRACES = "1";
    private static final String JSON_ACTION = "2";

    private void load() {
        gestureItems.clear();
        if (JSON_FILE == null || !JSON_FILE.exists() || JSON_FILE.isDirectory()) return;
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(JSON_FILE)) {
            if (parser.nextToken() == JsonToken.START_ARRAY) {
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                        MultiFingerGestureItem item = new MultiFingerGestureItem();
                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                            String name = parser.getCurrentName();
                            if (name != null) {
                                switch (name) {
                                    case JSON_FINGERS:
                                        parser.nextToken();
                                        item.setFingers(parser.getIntValue());
                                        break;
                                    case JSON_TRACES:
                                        if (parser.nextToken() == JsonToken.START_ARRAY) {
                                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                                item.addTrace(parser.getIntValue());
                                            }
                                        } else {
                                            parser.skipChildren();
                                        }
                                        break;
                                    case JSON_ACTION:
                                        Action action = new Action();
                                        action.loadAction(parser);
                                        item.setAction(action);
                                        break;
                                    default:
                                        parser.skipChildren();
                                        break;
                                }
                            }
                        }
                        gestureItems.add(item);
                    } else {
                        parser.skipChildren();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonGenerator generator = jsonFactory.createGenerator(JSON_FILE, JsonEncoding.UTF8)) {
            generator.writeStartArray();
            for (MultiFingerGestureItem item : gestureItems) {
                generator.writeStartObject();

                generator.writeNumberField(JSON_FINGERS, item.getFingers());

                // finger actions
                generator.writeFieldName(JSON_TRACES);
                generator.writeStartArray();
                for (Integer i : item.getTraces()) {
                    generator.writeNumber(i);
                }
                generator.writeEndArray();

                // browser action
                generator.writeFieldName(JSON_ACTION);
                item.getAction().writeAction(generator);

                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
