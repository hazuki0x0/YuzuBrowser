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

package jp.hazuki.yuzubrowser.legacy.backup;

import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.loader.content.AsyncTaskLoader;
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils;

public class RestoreTask extends AsyncTaskLoader<Boolean> {

    private File zip;

    public RestoreTask(Context context, File item) {
        super(context);
        zip = item;
    }

    @Override
    public Boolean loadInBackground() {
        BackupManager manager = new BackupManager(getContext());
        File root = manager.getRoot();
        manager.cleanFiles();
        OutputStream os = null;
        ZipInputStream zipStream = null;
        ZipEntry entry;

        try {
            String rootPath = root.getCanonicalPath();
            zipStream = new ZipInputStream(new FileInputStream(zip));

            byte[] buffer = new byte[8192];
            int len;
            File file;

            while ((entry = zipStream.getNextEntry()) != null) {
                file = new File(root, entry.getName());
                if (!file.getCanonicalPath().startsWith(rootPath)) {
                    throw new IOException("This file is not put in tmp folder. to:" + file.getCanonicalPath());
                }
                if ("main_preference.xml".equalsIgnoreCase(file.getName())) {
                    PrefXmlParser parser = new PrefXmlParser(getContext(), "main_preference");
                    try {
                        parser.load(zipStream);
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (entry.isDirectory()) {
                        if (file.exists()) {
                            FileUtils.deleteFile(file);
                        }
                        file.mkdir();
                    } else {
                        if (file.exists()) {
                            FileUtils.deleteFile(file);
                        } else if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }

                        os = new FileOutputStream(file);

                        while ((len = zipStream.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                        os.close();
                        os = null;
                    }
                }

            }
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
