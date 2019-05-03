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

package jp.hazuki.yuzubrowser.bookmark.netscape;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import androidx.loader.content.AsyncTaskLoader;
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkFolder;

public class BookmarkHtmlExportTask extends AsyncTaskLoader<Boolean> {

    private final File dest;
    private BookmarkFolder folder;

    public BookmarkHtmlExportTask(Context context, File dest, BookmarkFolder folder) {
        super(context);
        this.dest = dest;
        this.folder = folder;
    }

    @Override
    public Boolean loadInBackground() {
        if (!dest.getParentFile().exists()) {
            if (!dest.getParentFile().mkdirs()) {
                return Boolean.FALSE;
            }
        }

        try (FileWriter fileWriter = new FileWriter(dest);
             BufferedWriter writer = new BufferedWriter(fileWriter)) {

            NetscapeBookmarkCreator creator = new NetscapeBookmarkCreator(folder);
            creator.create(writer);
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
