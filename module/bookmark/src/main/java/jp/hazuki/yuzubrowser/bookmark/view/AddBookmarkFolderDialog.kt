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

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import jp.hazuki.bookmark.R
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkFolder
import jp.hazuki.yuzubrowser.bookmark.repository.BookmarkManager
import jp.hazuki.yuzubrowser.bookmark.util.BookmarkIdGenerator

class AddBookmarkFolderDialog @JvmOverloads constructor(context: Context, private var mManager: BookmarkManager?, title: String?, private var mParent: BookmarkFolder?, private val item: BookmarkFolder? = null) {
    private val mDialog: AlertDialog
    private val titleEditText: EditText
    private val addToTopCheckBox: CheckBox
    private var mOnClickListener: DialogInterface.OnClickListener? = null

    constructor(context: Context, manager: BookmarkManager, item: BookmarkFolder) : this(context, manager, item.title, item.parent, item)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.add_bookmark_folder_dialog, null)
        titleEditText = view.findViewById(R.id.titleEditText)
        addToTopCheckBox = view.findViewById(R.id.addToTopCheckBox)

        if (item != null) {
            addToTopCheckBox.visibility = View.GONE
        }

        titleEditText.setText(title)

        mDialog = AlertDialog.Builder(context)
                .setTitle(if (item == null) R.string.add_folder else R.string.edit_bookmark)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    fun show() {
        mDialog.show()

        mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener { _ ->
            val title = titleEditText.text
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(mDialog.context, R.string.title_empty_mes, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mManager == null)
                mManager = BookmarkManager.getInstance(mDialog.context)

            if (mParent == null)
                mParent = mManager!!.root

            if (item == null) {
                val item = BookmarkFolder(title.toString(), mParent, BookmarkIdGenerator.getNewId())
                if (addToTopCheckBox.isChecked)
                    mParent!!.addFirst(item)
                else
                    mParent!!.add(item)
            } else {
                if (item.parent == null) {
                    item.parent = mParent
                    mParent!!.add(item)
                }
                item.title = title.toString()
            }

            if (mManager!!.save()) {
                Toast.makeText(mDialog.context, R.string.succeed, Toast.LENGTH_SHORT).show()
                if (mOnClickListener != null)
                    mOnClickListener!!.onClick(mDialog, DialogInterface.BUTTON_POSITIVE)
                mDialog.dismiss()
            } else {
                Toast.makeText(mDialog.context, R.string.failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setOnClickListener(l: DialogInterface.OnClickListener): AddBookmarkFolderDialog {
        mOnClickListener = l
        return this
    }
}
