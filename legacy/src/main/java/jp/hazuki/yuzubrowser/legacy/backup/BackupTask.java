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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import androidx.loader.content.AsyncTaskLoader;
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;

public class BackupTask extends AsyncTaskLoader<Boolean> {

    private final File dest;

    public BackupTask(Context context, File dest) {
        super(context);
        this.dest = dest;
    }

    @Override
    public Boolean loadInBackground() {
        BackupManager manager = new BackupManager(getContext());
        try {
            if (!dest.getParentFile().exists()) {
                if (!dest.getParentFile().mkdirs()) {
                    return Boolean.FALSE;
                }
            }
            ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(dest));
            URI root = manager.getRoot().toURI();
            byte[] buffer = new byte[8192];
            int len;
            InputStream in;
            String name;

            for (File item : manager.getBackUpFiles()) {
                in = new FileInputStream(item);
                name = root.relativize(item.toURI()).toString();
                zipStream.putNextEntry(new ZipEntry(name));
                while ((len = in.read(buffer)) > 0) {
                    zipStream.write(buffer, 0, len);
                }
                in.close();
            }
            zipStream.close();
            return Boolean.TRUE;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return Boolean.FALSE;
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
