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

package jp.hazuki.yuzubrowser.legacy.tab.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;

import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

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
import java.util.Iterator;
import java.util.List;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils;
import jp.hazuki.yuzubrowser.core.utility.utils.IOUtils;
import jp.hazuki.yuzubrowser.core.utility.utils.ImageUtils;
import jp.hazuki.yuzubrowser.webview.CustomWebView;
import jp.hazuki.yuzubrowser.webview.WebViewFactory;
import okio.Okio;

public class TabStorage {
    private static final String FILE_TAB_INDEX = "index";
    private static final String FILE_TAB_CURRENT = "current";
    private static final String FILE_TAB_THUMBNAIL_SUFFIX = "_thumb";
    private List<TabIndexData> mTabIndexDataList;
    private final File tabPath;
    private final WebViewFactory webViewFactory;
    private OnWebViewCreatedListener listener;

    public TabStorage(Context context, WebViewFactory factory) {
        tabPath = context.getDir("tabs", Context.MODE_PRIVATE);
        mTabIndexDataList = loadIndexJson(new File(tabPath, FILE_TAB_INDEX));
        webViewFactory = factory;
        loadThumbnails();
    }

    public void addIndexData(TabIndexData data) {
        mTabIndexDataList.add(data);
    }

    public void addIndexData(int index, TabIndexData data) {
        mTabIndexDataList.add(index, data);
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

    public void clearExceptPinnedTab(OnClearExceptPinnedTabListener listener) {
        int index = 0;
        Iterator<TabIndexData> itr = mTabIndexDataList.iterator();
        while (itr.hasNext()) {
            TabIndexData data = itr.next();
            if (!data.isPinning()) {
                itr.remove();
                deleteWebView(data);
                listener.onRemove(index, data.getId());
            }
            index++;
        }
        saveIndexData();
    }

    public interface OnClearExceptPinnedTabListener {
        void onRemove(int index, long id);
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

    public MainTabData loadWebView(WebViewProvider provider, TabIndexData data, View tabView) {
        if (data == null) return null;
        Bundle bundle = loadBundle(new File(tabPath, Long.toString(data.getId())));
        CustomWebView webView = provider.makeWebView(webViewFactory.getMode(bundle));
        webView.setIdentityId(data.getId());
        MainTabData tab = data.getMainTabData(webView, tabView);
        if (listener != null) {
            listener.onWebViewCreated(tab);
        }
        webView.restoreState(bundle);
        return tab;
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
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    data.setThumbnail(bitmap);
                } catch (OutOfMemoryError e) {
                    System.gc();
                }
            }
        }
    }

    private void saveThumbnails() {
        for (TabIndexData data : mTabIndexDataList) {
            if (data.isThumbnailUpdated()) {
                byte[] image = ImageUtils.bmp2byteArray(data.getThumbnail(), Bitmap.CompressFormat.JPEG, 75);
                if (image != null)
                    saveThumbnail(data.getId(), image);
                data.setThumbnailUpdated(false);
            }
        }
    }

    private void saveThumbnail(long id, byte[] image) {
        try (FileOutputStream stream = new FileOutputStream(
            new File(tabPath, id + FILE_TAB_THUMBNAIL_SUFFIX))) {
            stream.write(image);
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
    }

    private byte[] getThumbnail(long id) {
        File file = new File(tabPath, id + FILE_TAB_THUMBNAIL_SUFFIX);

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
                byte[] b = IOUtils.readByte(is);
                parcel.unmarshall(b, 0, b.length);
                parcel.setDataPosition(0);
                Bundle bundle = parcel.readBundle(Bundle.class.getClassLoader());
                parcel.recycle();

                return bundle != null ? bundle : new Bundle();
            } catch (IOException | IllegalArgumentException e) {
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
        FileUtils.deleteDirectoryContents(tabPath);
    }

    public void setOnWebViewCreatedListener(OnWebViewCreatedListener listener) {
        this.listener = listener;
    }

    private static final String JSON_NAME_ID = "id";
    private static final String JSON_NAME_URL = "url";
    private static final String JSON_NAME_TITLE = "title";
    private static final String JSON_NAME_TAB_TYPE = "type";
    private static final String JSON_NAME_PARENT = "parent";
    private static final String JSON_NAME_NAV_LOCK = "nav";
    private static final String JSON_NAME_PINNING = "pin";

    private List<TabIndexData> loadIndexJson(File file) {
        List<TabIndexData> tabIndexDataList = new ArrayList<>();
        try (JsonReader reader = JsonReader.of(Okio.buffer(Okio.source(file)))) {
            // 配列の処理
            if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                reader.beginArray();
                while (reader.hasNext()) {
                    // 各オブジェクトの処理
                    if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                        reader.beginObject();
                        TabIndexData tabIndexData = new TabIndexData();
                        while (reader.hasNext()) {
                            switch (reader.nextName()) {
                                case JSON_NAME_ID:
                                    tabIndexData.setId(reader.nextLong());
                                    break;
                                case JSON_NAME_URL:
                                    if (reader.peek() == JsonReader.Token.NULL) {
                                        reader.nextNull();
                                    } else {
                                        tabIndexData.setUrl(reader.nextString());
                                    }
                                    break;
                                case JSON_NAME_TITLE:
                                    if (reader.peek() == JsonReader.Token.NULL) {
                                        reader.nextNull();
                                    } else {
                                        tabIndexData.setTitle(reader.nextString());
                                    }
                                    break;
                                case JSON_NAME_TAB_TYPE:
                                    tabIndexData.setTabType(reader.nextInt());
                                    break;
                                case JSON_NAME_PARENT:
                                    tabIndexData.setParent(reader.nextLong());
                                    break;
                                case JSON_NAME_NAV_LOCK:
                                    tabIndexData.setNavLock(reader.nextBoolean());
                                    break;
                                case JSON_NAME_PINNING:
                                    tabIndexData.setPinning(reader.nextBoolean());
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                        }
                        tabIndexDataList.add(tabIndexData);
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tabIndexDataList;
    }

    private void saveIndexJson(File file, List<TabIndexData> tabIndexDataList) {
        try (JsonWriter writer = JsonWriter.of(Okio.buffer(Okio.sink(file)))) {
            writer.beginArray();
            for (TabIndexData data : tabIndexDataList) {
                writer.beginObject();
                writer.name(JSON_NAME_ID);
                writer.value(data.getId());
                writer.name(JSON_NAME_URL);
                writer.value(data.getUrl());
                writer.name(JSON_NAME_TITLE);
                writer.value(data.getTitle());
                writer.name(JSON_NAME_TAB_TYPE);
                writer.value(data.getTabType());
                writer.name(JSON_NAME_PARENT);
                writer.value(data.getParent());
                writer.name(JSON_NAME_NAV_LOCK);
                writer.value(data.isNavLock());
                writer.name(JSON_NAME_PINNING);
                writer.value(data.isPinning());
                writer.endObject();
            }
            writer.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String JSON_NAME_CURRENT_TAB = "current";

    public int loadCurrentTab() {
        int tab = 0;
        try (JsonReader reader = JsonReader.of(Okio.buffer(Okio.source(new File(tabPath, FILE_TAB_CURRENT))))) {
            if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                reader.beginObject();
                while (reader.hasNext()) {
                    switch (reader.nextName()) {
                        case JSON_NAME_CURRENT_TAB:
                            tab = reader.nextInt();
                            break;
                    }
                }
                reader.endObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tab;
    }

    public void saveCurrentTab(int currentTab) {
        try (JsonWriter writer = JsonWriter.of(Okio.buffer(Okio.sink(new File(tabPath, FILE_TAB_CURRENT))))) {
            writer.beginObject();
            writer.name(JSON_NAME_CURRENT_TAB);
            writer.value(currentTab);
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
