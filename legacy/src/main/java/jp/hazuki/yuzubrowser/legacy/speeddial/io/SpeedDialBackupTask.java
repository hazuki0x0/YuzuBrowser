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

package jp.hazuki.yuzubrowser.legacy.speeddial.io;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import androidx.loader.content.AsyncTaskLoader;
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDialManager;

public class SpeedDialBackupTask extends AsyncTaskLoader<Boolean> {

    private final File dest;

    public SpeedDialBackupTask(Context context, File file) {
        super(context);
        dest = file;
    }

    @Override
    public Boolean loadInBackground() {

        if (!dest.getParentFile().exists()) {
            if (!dest.getParentFile().mkdirs()) {
                return Boolean.FALSE;
            }
        }


        File db = getContext().getDatabasePath(SpeedDialManager.DB_NAME);

        try (FileOutputStream fos = new FileOutputStream(dest);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(db)) {
            zos.putNextEntry(new ZipEntry(SpeedDialManager.DB_NAME));
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            return Boolean.TRUE;
        } catch (IOException e) {
            e.printStackTrace();
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
