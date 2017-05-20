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

package jp.hazuki.yuzubrowser.tab.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcel;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
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
import java.util.Collections;
import java.util.List;

import jp.hazuki.yuzubrowser.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.FileUtils;
import jp.hazuki.yuzubrowser.utils.IOUtils;
import jp.hazuki.yuzubrowser.utils.ImageUtils;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;
import jp.hazuki.yuzubrowser.webkit.WebBrowser;
import jp.hazuki.yuzubrowser.webkit.WebViewFactory;

class TabStorage {
    private static final String FILE_TAB_INDEX = "index";
    private static final String FILE_TAB_CURRENT = "current";
    private static final String FILE_TAB_THUMBNAIL_SUFFIX = "_thumb";
    private List<TabIndexData> mTabIndexDataList;
    private final File tabPath;

    public TabStorage(Context context) {
        tabPath = context.getDir("tabs", Context.MODE_PRIVATE);
        mTabIndexDataList = loadIndexJson(new File(tabPath, FILE_TAB_INDEX));
        loadThumbnails();
    }

    public void addIndexData(TabIndexData data) {
        mTabIndexDataList.add(data);
    }

    public TabIndexData getIndexData(int index) {
        return mTabIndexDataList.get(index);
    }

    public int indexOf(long id) {
        for (int i = 0; i < mTabIndexDataList.size(); i++)
            if (mTabIndexDataList.get(i).getId() == id)
                return i;

        return -1;
    }

    public int size() {
        return mTabIndexDataList.size();
    }

    public TabIndexData removeAndDelete(int index) {
        TabIndexData data = mTabIndexDataList.remove(index);
        saveIndexData();
        deleteWebView(data);
        return data;
    }

    public void move(int from, int to) {
        ArrayUtils.move(mTabIndexDataList, from, to);
    }

    public void swap(int i, int j) {
        Collections.swap(mTabIndexDataList, i, j);
    }

    TabIndexData remove(int index) {
        return mTabIndexDataList.remove(index);
    }

    void add(int index, TabIndexData tabIndexData) {
        mTabIndexDataList.add(index, tabIndexData);
    }

    public List<TabIndexData> getTabIndexDataList() {
        return new ArrayList<>(mTabIndexDataList);
    }

    public int searchParentTabNo(long id) {
        for (int i = 0; mTabIndexDataList.size() > i; i++) {
            if (mTabIndexDataList.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public void saveIndexData() {
        saveIndexJson(new File(tabPath, FILE_TAB_INDEX), mTabIndexDataList);
        saveThumbnails();
    }

    public CustomWebView loadWebView(WebBrowser webBrowser, TabIndexData data) {
        if (data == null) return null;
        Bundle bundle = loadBundle(new File(tabPath, Long.toString(data.getId())));
        CustomWebView webView = webBrowser.makeWebView(WebViewFactory.getMode(bundle));
        webView.restoreState(bundle);
        webView.setIdentityId(data.getId());
        return webView;
    }

    public TabIndexData saveWebView(MainTabData tabData) {
        TabIndexData data = tabData.getTabIndexData();
        saveIndexData();

        Bundle bundle = new Bundle();
        tabData.mWebView.saveState(bundle);
        saveBundle(new File(tabPath, Long.toString(data.getId())), bundle);

        return data;
    }

    private void loadThumbnails() {
        for (TabIndexData data : mTabIndexDataList) {
            byte[] image = getThumbnail(data.getId());
            if (image != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                data.setThumbnail(bitmap);
            }
        }
    }

    private void saveThumbnails() {
        for (TabIndexData data : mTabIndexDataList) {
            if (data.isThumbnailUpdated()) {
                byte[] image = ImageUtils.bmp2byteArray(data.getThumbnail(), Bitmap.CompressFormat.WEBP, 60);
                if (image != null)
                    saveThumbnail(data.getId(), image);
                data.setThumbnailUpdated(false);
            }
        }
    }

    private void saveThumbnail(long id, byte[] image) {
        try (FileOutputStream stream = new FileOutputStream(
                new File(tabPath, Long.toString(id) + FILE_TAB_THUMBNAIL_SUFFIX))) {
            stream.write(image);
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
    }

    public byte[] getThumbnail(long id) {
        File file = new File(tabPath, Long.toString(id) + FILE_TAB_THUMBNAIL_SUFFIX);

        if (!file.exists()) return null;

        try (FileInputStream inputStream = new FileInputStream(file);
             BufferedInputStream is = new BufferedInputStream(inputStream)) {
            return IOUtils.readByte(is);
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }

        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteWebView(TabIndexData data) {
        String id = Long.toString(data.getId());
        new File(tabPath, id).delete();
        new File(tabPath, id + FILE_TAB_THUMBNAIL_SUFFIX).delete();
    }

    private Bundle loadBundle(File file) {
        if (file.exists()) {
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                Parcel parcel = Parcel.obtain();
                byte b[] = IOUtils.readByte(is);
                parcel.unmarshall(b, 0, b.length);
                parcel.setDataPosition(0);
                Bundle bundle = parcel.readBundle(Bundle.class.getClassLoader());
                parcel.recycle();

                return bundle != null ? bundle : new Bundle();
            } catch (IOException e) {
                ErrorReport.printAndWriteLog(e);
            }
        }
        return new Bundle();
    }

    void saveBundle(long id, Bundle bundle) {
        saveBundle(new File(tabPath, Long.toString(id)), bundle);
    }

    private void saveBundle(File file, Bundle bundle) {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            Parcel parcel = Parcel.obtain();
            parcel.writeBundle(bundle);
            os.write(parcel.marshall());
            parcel.recycle();
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
    }

    public void clear() {
        FileUtils.deleteFile(tabPath);
    }

    private static final String JSON_NAME_ID = "id";
    private static final String JSON_NAME_URL = "url";
    private static final String JSON_NAME_TITLE = "title";
    private static final String JSON_NAME_TAB_TYPE = "type";
    private static final String JSON_NAME_PARENT = "parent";

    private List<TabIndexData> loadIndexJson(File file) {
        List<TabIndexData> tabIndexDataList = new ArrayList<>();
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(file)) {
            // 配列の処理
            if (parser.nextToken() == JsonToken.START_ARRAY) {
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    // 各オブジェクトの処理
                    if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                        TabIndexData tabIndexData = new TabIndexData();
                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                            String name = parser.getCurrentName();
                            parser.nextToken();
                            if (name != null) {
                                switch (name) {
                                    case JSON_NAME_ID:
                                        tabIndexData.setId(parser.getLongValue());
                                        break;
                                    case JSON_NAME_URL:
                                        tabIndexData.setUrl(parser.getText());
                                        break;
                                    case JSON_NAME_TITLE:
                                        tabIndexData.setTitle(parser.getText());
                                        break;
                                    case JSON_NAME_TAB_TYPE:
                                        tabIndexData.setTabType(parser.getIntValue());
                                        break;
                                    case JSON_NAME_PARENT:
                                        tabIndexData.setParent(parser.getLongValue());
                                        break;
                                    default:
                                        parser.skipChildren();
                                        break;
                                }
                            }
                        }
                        tabIndexDataList.add(tabIndexData);
                    } else {
                        parser.skipChildren();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tabIndexDataList;
    }

    private void saveIndexJson(File file, List<TabIndexData> tabIndexDataList) {
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonGenerator generator = jsonFactory.createGenerator(file, JsonEncoding.UTF8)) {
            generator.writeStartArray();
            for (TabIndexData data : tabIndexDataList) {
                generator.writeStartObject();
                generator.writeNumberField(JSON_NAME_ID, data.getId());
                generator.writeStringField(JSON_NAME_URL, data.getUrl());
                generator.writeStringField(JSON_NAME_TITLE, data.getTitle());
                generator.writeNumberField(JSON_NAME_TAB_TYPE, data.getTabType());
                generator.writeNumberField(JSON_NAME_PARENT, data.getParent());
                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String JSON_NAME_CURRENT_TAB = "current";

    public int loadCurrentTab() {
        int tab = 0;
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(new File(tabPath, FILE_TAB_CURRENT))) {
            if (parser.nextToken() == JsonToken.START_OBJECT) {
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String name = parser.getCurrentName();
                    parser.nextToken();

                    if (JSON_NAME_CURRENT_TAB.equals(name)) {
                        tab = parser.getIntValue();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tab;
    }

    public void saveCurrentTab(int currentTab) {
        JsonFactory jsonFactory = new JsonFactory();
        try (JsonGenerator generator = jsonFactory.createGenerator(new File(tabPath, FILE_TAB_CURRENT), JsonEncoding.UTF8)) {
            generator.writeStartObject();
            generator.writeNumberField(JSON_NAME_CURRENT_TAB, currentTab);
            generator.writeEndObject();
            generator.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
