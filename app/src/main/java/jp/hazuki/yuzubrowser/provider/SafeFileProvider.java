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

package jp.hazuki.yuzubrowser.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import androidx.annotation.NonNull;
import jp.hazuki.yuzubrowser.BuildConfig;
import jp.hazuki.yuzubrowser.ErrorReportServer;
import jp.hazuki.yuzubrowser.core.utility.log.Logger;
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils;

public class SafeFileProvider extends ContentProvider {
    private static final String TAG = "SafeFileProvider";
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".browser.SafeFileProvider";
    //private static final String PROVIDER_URI = "content://" + AUTHORITY + "/";

    private String[] mAllowedFolder;

    @Override
    public boolean onCreate() {
        try {
            mAllowedFolder = new String[]{
                    getContext().getDir("public_html", Context.MODE_PRIVATE).getCanonicalPath(),
                    Environment.getExternalStorageDirectory().getCanonicalPath(),
                "/storage/",
                "/mnt/"
            };
        } catch (IOException e) {
            ErrorReportServer.printAndWriteLog(e);
        }
        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        if (mAllowedFolder == null)
            return super.openFile(uri, mode);

        URI normalUri = URI.create(uri.buildUpon().scheme("file").authority("").build().toString()).normalize();
        File file = new File(normalUri);

        try {
            if (file.exists() && file.isFile() && file.canRead()) {
                String path = file.getCanonicalPath();

                for (String allowed : mAllowedFolder) {
                    if (path.regionMatches(true, 0, allowed, 0, allowed.length())) {
                        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                    }
                }

                Logger.w(TAG, "file exists but access not allowed");
                Logger.w(TAG, "path:" + path);
            }
        } catch (IOException e) {
            ErrorReportServer.printAndWriteLog(e);
        }
        return super.openFile(uri, mode);
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(@NonNull Uri uri) {
        URI normalUri = URI.create(uri.buildUpon().scheme("file").authority("").build().toString()).normalize();
        return FileUtils.getMineType(new File(normalUri));
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public static String convertToSaferUrl(String url) {
        return Uri.parse(url).buildUpon().scheme("content").authority(AUTHORITY).build().toString();
    }

}
