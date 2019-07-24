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
import android.os.Bundle;
import android.os.Parcel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.core.utility.utils.IOUtils;
import jp.hazuki.yuzubrowser.webview.WebViewFactory;

public class BundleDataBaseConverter {
    private final File mFile;
    private final WebViewFactory webViewFactory;

    public BundleDataBaseConverter(File file, WebViewFactory factory) {
        mFile = file;
        webViewFactory = factory;
    }

    public void clear() {
        mFile.delete();
    }

    public boolean readList(Context context) {
        if (!mFile.exists() || mFile.isDirectory())
            return true;

        try (InputStream is = new BufferedInputStream(new FileInputStream(mFile))) {
            Parcel parcel = Parcel.obtain();
            byte[] b = IOUtils.readByte(is);
            parcel.unmarshall(b, 0, b.length);
            parcel.setDataPosition(0);
            Bundle bundle = parcel.readBundle(Bundle.class.getClassLoader());
            if (bundle == null)
                throw new NullPointerException();
            restoreInstanceState(context, bundle);
            parcel.recycle();

            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return false;
    }

    private static final String EXTRA_CURRENT_NO = "TAB.E0";
    private static final String EXTRA_LIST_COUNT = "TAB.E1";
    private static final String EXTRA_TAB_TYPE = "TAB.E2";
    private static final String EXTRA_TAB_ID = "TAB.E3";
    private static final String EXTRA_TAB_PARENT = "TAB.E4";

    private void restoreInstanceState(Context context, Bundle bundle) {
        int currentNo = bundle.getInt(EXTRA_CURRENT_NO);
        int list_count = bundle.getInt(EXTRA_LIST_COUNT);
        int[] tabType = (int[]) bundle.getSerializable(EXTRA_TAB_TYPE);
        long[] ids = (long[]) bundle.getSerializable(EXTRA_TAB_ID);
        long[] parents = (long[]) bundle.getSerializable(EXTRA_TAB_PARENT);
        if (tabType == null) tabType = new int[list_count];
        if (ids == null) ids = new long[list_count];
        if (parents == null) parents = new long[list_count];

        TabStorage fileData = new TabStorage(context, webViewFactory);
        for (int i = 0; i < list_count; ++i) {
            Bundle webBundle = bundle.getBundle("TAB.W" + i);
            fileData.addIndexData(new TabIndexData("", "", tabType[i], ids[i], parents[i]));
            fileData.saveBundle(ids[i], webBundle);
        }
        fileData.saveIndexData();
        fileData.saveCurrentTab(currentNo);
    }
}
