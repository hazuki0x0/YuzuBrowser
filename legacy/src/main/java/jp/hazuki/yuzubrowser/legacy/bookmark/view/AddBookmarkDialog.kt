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

package jp.hazuki.yuzubrowser.legacy.bookmark.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkFolder
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkItem
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkManager
import jp.hazuki.yuzubrowser.legacy.bookmark.view.BookmarkFoldersDialog.OnFolderSelectedListener
import jp.hazuki.yuzubrowser.legacy.browser.BrowserController
import jp.hazuki.yuzubrowser.ui.extensions.toPunyCodeUrl
import jp.hazuki.yuzubrowser.ui.widget.SpinnerButton

abstract class AddBookmarkDialog<S : BookmarkItem, T>(protected val context: Context, manager: BookmarkManager?, protected val mItem: S?, title: String?, url: T) : OnFolderSelectedListener {
    protected val mDialog: AlertDialog
    protected val titleEditText: EditText
    protected val urlEditText: EditText
    protected val folderTextView: TextView
    protected val folderButton: SpinnerButton
    protected val addToTopCheckBox: CheckBox
    protected var mOnClickListener: DialogInterface.OnClickListener? = null
    protected val mManager: BookmarkManager = manager ?: BookmarkManager.getInstance(context)
    protected lateinit var mParent: BookmarkFolder

    init {
        val view = inflateView()
        titleEditText = view.findViewById(R.id.titleEditText)
        urlEditText = view.findViewById(R.id.urlEditText)
        folderTextView = view.findViewById(R.id.folderTextView)
        folderButton = view.findViewById(R.id.folderButton)
        addToTopCheckBox = view.findViewById(R.id.addToTopCheckBox)

        initView(view, title, url)

        //titleEditText.setText((title == null)?url:title);

        mDialog = AlertDialog.Builder(context)
                .setTitle(if (mItem == null) R.string.add_bookmark else R.string.edit_bookmark)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    protected fun inflateView(): View {
        return LayoutInflater.from(context).inflate(R.layout.add_bookmark_dialog, null)
    }

    protected open fun initView(view: View, title: String?, url: T) {
        if (mItem == null) {
            val root = mManager.root
            mParent = root
            folderButton.text = root.title
            folderButton.setOnClickListener { v ->
                BookmarkFoldersDialog(context, mManager)
                        .setTitle(R.string.folder)
                        .setCurrentFolder(root)
                        .setOnFolderSelectedListener(this@AddBookmarkDialog)
                        .show()
            }
        } else {
            folderTextView.visibility = View.GONE
            folderButton.visibility = View.GONE
            addToTopCheckBox.visibility = View.GONE
        }
    }

    fun show() {
        mDialog.show()

        mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener { v ->
            val title = titleEditText.text
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(mDialog.context, R.string.title_empty_mes, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val url = urlEditText.text
            if (TextUtils.isEmpty(url)) {
                Toast.makeText(mDialog.context, R.string.url_empty_mes, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = makeItem(mItem, title.toString(), url.toString().toPunyCodeUrl())
            if (item != null) {
                if (addToTopCheckBox.isChecked)
                    mManager.addFirst(mParent, item)
                else
                    mManager.add(mParent, item)
            }
            if (mItem != null && addToTopCheckBox.isChecked) {
                mManager.moveToFirst(mParent, mItem)
            }

            if (mManager.save()) {
                Toast.makeText(mDialog.context, R.string.succeed, Toast.LENGTH_SHORT).show()
                mOnClickListener?.onClick(mDialog, DialogInterface.BUTTON_POSITIVE)
                if (context is BrowserController) {
                    (context as BrowserController).requestIconChange()
                }
                mDialog.dismiss()
            } else {
                Toast.makeText(mDialog.context, R.string.failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    protected abstract fun makeItem(item: S?, title: String, url: String): S?

    fun setOnClickListener(l: DialogInterface.OnClickListener): AddBookmarkDialog<S, T> {
        mOnClickListener = l
        return this
    }

    inline fun setOnClickListener(crossinline listener: (DialogInterface, Int) -> Unit): AddBookmarkDialog<S, T> {
        setOnClickListener(DialogInterface.OnClickListener { dialog, which -> listener(dialog, which) })
        return this
    }

    override fun onFolderSelected(dialog: DialogInterface, folder: BookmarkFolder): Boolean {
        folderButton.text = folder.title
        mParent = folder
        return false
    }
}
