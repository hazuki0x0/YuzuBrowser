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

package jp.hazuki.yuzubrowser.bookmark.netscape

import android.content.Context
import android.os.Handler
import android.widget.Toast
import androidx.loader.content.AsyncTaskLoader
import jp.hazuki.bookmark.R
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkFolder
import jp.hazuki.yuzubrowser.bookmark.repository.BookmarkManager
import jp.hazuki.yuzubrowser.favicon.FaviconManager
import java.io.File
import java.io.IOException

class BookmarkHtmlImportTask(
    context: Context,
    private val html: File,
    private val manager: BookmarkManager,
    private val faviconManager: FaviconManager,
    private val folder: BookmarkFolder,
    private val handler: Handler
) : AsyncTaskLoader<Boolean>(context) {

    override fun loadInBackground(): Boolean? {
        try {
            val parser = NetscapeBookmarkParser(folder, faviconManager)
            parser.parse(html)
            manager.save()
            return java.lang.Boolean.TRUE
        } catch (e: NetscapeBookmarkException) {
            handler.post { Toast.makeText(context, R.string.not_bookmark_file, Toast.LENGTH_SHORT).show() }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return java.lang.Boolean.FALSE
    }

    override fun onStartLoading() {
        forceLoad()
    }

    override fun onStopLoading() {
        cancelLoad()
    }
}
