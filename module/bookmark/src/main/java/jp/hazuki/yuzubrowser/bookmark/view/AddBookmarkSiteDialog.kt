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

package jp.hazuki.yuzubrowser.bookmark.view

import android.content.Context
import android.view.View
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkSite
import jp.hazuki.yuzubrowser.bookmark.repository.BookmarkManager
import jp.hazuki.yuzubrowser.bookmark.util.BookmarkIdGenerator
import jp.hazuki.yuzubrowser.ui.extensions.decodePunyCodeUrl

class AddBookmarkSiteDialog : AddBookmarkDialog<BookmarkSite, String> {
    constructor(context: Context, manager: BookmarkManager, item: BookmarkSite) : super(context, manager, item, item.title, item.url)

    constructor(context: Context, title: String, url: String) : super(context, null, null, title, url)

    override fun initView(view: View, title: String?, url: String) {
        super.initView(view, title, url)
        titleEditText.setText(title ?: url)
        urlEditText.setText(url.decodePunyCodeUrl())
    }

    override fun makeItem(item: BookmarkSite?, title: String, url: String): BookmarkSite? {
        return if (item == null) {
            BookmarkSite(title, url.trim { it <= ' ' }, BookmarkIdGenerator.getNewId())
        } else {
            item.title = title
            item.url = url.trim { it <= ' ' }
            null
        }
    }
}
