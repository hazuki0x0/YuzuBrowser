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

package jp.hazuki.yuzubrowser.legacy.bookmark.netscape;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import androidx.loader.content.AsyncTaskLoader;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkManager;

public class BookmarkHtmlImportTask extends AsyncTaskLoader<Boolean> {

    private File html;
    private BookmarkManager manager;
    private BookmarkFolder folder;
    private Handler handler;

    public BookmarkHtmlImportTask(Context context, File html, BookmarkManager manager, BookmarkFolder folder, Handler handler) {
        super(context);
        this.html = html;
        this.manager = manager;
        this.folder = folder;
        this.handler = handler;
    }

    @Override
    public Boolean loadInBackground() {
        try {
            NetscapeBookmarkParser parser = new NetscapeBookmarkParser(getContext(), folder);
            parser.parse(html);
            manager.save();
            return Boolean.TRUE;
        } catch (NetscapeBookmarkException e) {
            handler.post(() -> Toast.makeText(getContext(), R.string.not_bookmark_file, Toast.LENGTH_SHORT).show());
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
