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
import android.view.View
import android.view.ViewGroup
import android.widget.*
import jp.hazuki.bookmark.R
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkFolder
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkItem
import jp.hazuki.yuzubrowser.bookmark.repository.BookmarkManager
import java.util.*

class BookmarkFoldersDialog(private val context: Context, private val manager: BookmarkManager) {
    private lateinit var mDialog: AlertDialog
    private val mListView: ListView = ListView(context)
    private lateinit var mCurrentFolder: BookmarkFolder
    private val mFolderList = ArrayList<BookmarkFolder?>()
    private var mExcludeList: Collection<BookmarkItem>? = null
    private var mOnFolderSelectedListener: OnFolderSelectedListener? = null

    private val titleText: TextView

    interface OnFolderSelectedListener {
        fun onFolderSelected(dialog: DialogInterface, folder: BookmarkFolder): Boolean
    }

    init {
        val top = View.inflate(context, R.layout.dialog_title, null)
        titleText = top.findViewById(R.id.titleText)
        val button = top.findViewById(R.id.addButton) as ImageButton

        button.setOnClickListener {
            AddBookmarkFolderDialog(context, manager, context.getString(R.string.new_folder_name), mCurrentFolder)
                .setOnClickListener(DialogInterface.OnClickListener { _, _ -> setFolder(mCurrentFolder) })
                .show()
        }
        button.setOnLongClickListener {
            Toast.makeText(context, R.string.new_folder_name, Toast.LENGTH_SHORT).show()
            true
        }

        mDialog = AlertDialog.Builder(context)
            .setView(mListView)
            .setCustomTitle(top)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (mOnFolderSelectedListener != null)
                    mOnFolderSelectedListener!!.onFolderSelected(mDialog, mCurrentFolder)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()


        mListView.adapter = object : ArrayAdapter<BookmarkFolder>(context.applicationContext, 0, mFolderList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView
                    ?: View.inflate(context, android.R.layout.simple_list_item_1, null)
                val item = getItem(position)
                view.findViewById<TextView>(android.R.id.text1).text = if (item != null) item.title else ".."
                return view
            }
        }

        mListView.setOnItemClickListener { _, _, position, _ ->
            val folder = mFolderList[position] ?: mCurrentFolder.parent!!
            setFolder(folder)
        }

        mListView.setOnItemLongClickListener { _, _, position, _ ->
            val folder = mFolderList[position] ?: mCurrentFolder.parent!!
            mOnFolderSelectedListener?.onFolderSelected(mDialog, folder) ?: false
        }
    }

    fun setTitle(title: CharSequence): BookmarkFoldersDialog {
        titleText.text = title
        return this
    }

    fun setTitle(title: Int): BookmarkFoldersDialog {
        titleText.setText(title)
        return this
    }

    fun setCurrentFolder(folder: BookmarkFolder): BookmarkFoldersDialog {
        mExcludeList = null
        setFolder(folder)
        return this
    }

    fun setCurrentFolder(folder: BookmarkFolder, excludeList: List<BookmarkItem>): BookmarkFoldersDialog {
        mExcludeList = excludeList
        setFolder(folder)
        return this
    }

    fun setCurrentFolder(folder: BookmarkFolder, excludeItem: BookmarkItem?): BookmarkFoldersDialog {
        mExcludeList = hashSetOf<BookmarkItem>().apply { if (excludeItem != null) add(excludeItem) }
        setFolder(folder)
        return this
    }

    fun setOnFolderSelectedListener(l: OnFolderSelectedListener): BookmarkFoldersDialog {
        mOnFolderSelectedListener = l
        return this
    }

    inline fun setOnFolderSelectedListener(crossinline l: (BookmarkFolder) -> Boolean): BookmarkFoldersDialog {
        return setOnFolderSelectedListener(object : OnFolderSelectedListener {
            override fun onFolderSelected(dialog: DialogInterface, folder: BookmarkFolder): Boolean {
                return l(folder)
            }
        })
    }

    private fun setFolder(folder: BookmarkFolder) {
        mFolderList.clear()
        mCurrentFolder = folder
        if (folder.parent != null)
            mFolderList.add(null)//for move to prev folder
        for (i in folder.itemList)
            if (i is BookmarkFolder && (mExcludeList == null || !mExcludeList!!.contains(i)))
                mFolderList.add(i)
        (mListView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
    }

    fun show() {
        mDialog.show()
    }
}
