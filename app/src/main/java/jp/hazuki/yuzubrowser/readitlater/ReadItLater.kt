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

package jp.hazuki.yuzubrowser.readitlater

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import jp.hazuki.yuzubrowser.webkit.CustomWebView

fun save(context: Context, resolver: ContentResolver, webView: CustomWebView) {
    val uri = resolver.insert(ReadItLaterProvider.EDIT_URI, ContentValues().apply {
        put(ReadItLaterProvider.URL, webView.originalUrl)
        put(ReadItLaterProvider.TITLE, webView.title)
    })
    val cursor = resolver.query(uri, null, null, null, null)
    if (cursor != null) {
        cursor.moveToFirst()
        val path = cursor.getString(0)
        cursor.close()
        if (webView.saveWebArchiveMethod(path)) {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            return
        }
    }
    resolver.delete(ReadItLaterProvider.convertToEdit(uri), null, null)
}