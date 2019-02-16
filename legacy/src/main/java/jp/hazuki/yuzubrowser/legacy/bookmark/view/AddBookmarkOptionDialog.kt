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

package jp.hazuki.yuzubrowser.legacy.bookmark.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkManager
import jp.hazuki.yuzubrowser.legacy.browser.BrowserController

class AddBookmarkOptionDialog : androidx.fragment.app.DialogFragment() {

    companion object {
        private const val ARG_URL = "url"
        private const val ARG_TITLE = "title"

        @JvmStatic
        fun newInstance(title: String, url: String): AddBookmarkOptionDialog {
            return AddBookmarkOptionDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_URL, url)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        val arguments = arguments ?: throw IllegalArgumentException()
        return AlertDialog.Builder(activity).apply {
            setTitle(R.string.bookmark)
            setItems(R.array.add_bookmark_option) { _, i ->
                when (i) {
                    0 -> AddBookmarkSiteDialog(activity, arguments.getString(ARG_TITLE), arguments.getString(ARG_URL)).show()
                    1 -> {
                        BookmarkManager.getInstance(activity).run {
                            removeAll(arguments.getString(ARG_URL))
                            save()
                        }
                        if (activity is BrowserController) {
                            activity.requestIconChange()
                        }
                    }
                }
            }
            setNegativeButton(android.R.string.cancel, null)
        }.create()
    }
}