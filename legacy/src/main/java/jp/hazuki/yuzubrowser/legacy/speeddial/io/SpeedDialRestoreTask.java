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

package jp.hazuki.yuzubrowser.legacy.speeddial.io;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.loader.content.AsyncTaskLoader;
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDialHtml;
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDialManager;

public class SpeedDialRestoreTask extends AsyncTaskLoader<Boolean> {

    private final File from;

    public SpeedDialRestoreTask(Context context, File from) {
        super(context);
        this.from = from;
    }

    @Override
    public Boolean loadInBackground() {
        if (!from.exists() || !from.canRead()) return Boolean.FALSE;
        File db = getContext().getDatabasePath(SpeedDialManager.DB_NAME);

        getContext().deleteDatabase(SpeedDialManager.DB_NAME);
        try (FileInputStream fis = new FileInputStream(from);
             ZipInputStream zis = new ZipInputStream(fis);
             FileOutputStream fos = new FileOutputStream(db)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (SpeedDialManager.DB_NAME.equals(entry.getName())) {
                    byte[] buffer = new byte[8192];
                    int len;

                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            SpeedDialHtml.clearCache(getContext());
            SpeedDialManager.Companion.closeAll();
            SpeedDialManager.Companion.closeAll();
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
