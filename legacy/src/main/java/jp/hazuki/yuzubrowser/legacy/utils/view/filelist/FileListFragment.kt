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

package jp.hazuki.yuzubrowser.legacy.utils.view.filelist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.ListFragment
import jp.hazuki.yuzubrowser.core.utility.utils.ArrayUtils
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.extensions.addCallback
import java.io.File
import java.util.*

class FileListFragment : ListFragment() {

    var currentFolder: File? = null
        private set
    var currentFileList: Array<File>? = null
        private set
    private var mShowParentMover = false
    private var mShowDirectoryOnly = false
    private val extension: String? = null
    private var fileSelectedListener: OnFileSelectedListener? = null
    private var itemLongClick: OnFileItemLongClickListener? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val arguments = arguments ?: throw IllegalArgumentException()
        mShowParentMover = arguments.getBoolean(EXTRA_PARENT_MOVER)
        mShowDirectoryOnly = arguments.getBoolean(EXTRA_DIR_ONLY)
        val file = arguments.getSerializable(EXTRA_FILE) as File

        setFilePath(file)

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, view, position, id ->
            itemLongClick?.onListFileItemLongClick(listView, view, currentFileList!![position], position, id)
            false
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, this::goBack)
    }

    private fun goBack(): Boolean {
        val nextFile = currentFolder!!.parentFile
        return if (nextFile != null && nextFile.canRead())
            setFilePath(nextFile)
        else
            false
    }

    fun setFilePath(file: File?): Boolean {
        val activity = activity ?: return false
        if (file == null) {
            currentFolder = null
            currentFileList = null
            val adapter = listView.adapter as ArrayAdapter<*>
            adapter.notifyDataSetInvalidated()
            return false
        }
        if (file.isFile) {
            return false
        }

        if (!file.exists() || !file.canRead()) {
            Toast.makeText(activity, R.string.cannot_access_folder, Toast.LENGTH_SHORT).show()
            return false
        }

        var fileList = file.listFiles()
        if (mShowDirectoryOnly) {
            fileList = ArrayUtils.copyIf(fileList) { `object` -> `object`.isDirectory }
        }

        if (extension != null) {
            fileList = ArrayUtils.copyIf(fileList) { `object` -> `object`.isDirectory || `object`.name.endsWith(extension) }
        }

        Arrays.sort(fileList, FileUtils.FILE_COMPARATOR)

        currentFolder = file
        currentFileList = fileList

        val adapter = FileAdapter(activity, fileList)
        listAdapter = adapter
        activity.title = file.name
        return true
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        var pos = position
        if (mShowParentMover) {
            if (pos == 0) {
                goBack()
                return
            }
            --pos
        }
        val file = currentFileList!![pos]
        if (file.isFile) {
            if (fileSelectedListener != null) {
                fileSelectedListener!!.onFileSelected(file)
            }
        } else {
            setFilePath(file)
        }
    }

    fun notifyDataSetChanged() {
        if (currentFolder != null)
            setFilePath(currentFolder)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is OnFileSelectedListener) {
            fileSelectedListener = activity as OnFileSelectedListener?
        } else {
            throw RuntimeException("Not found OnFileSelectedListener in Activity")
        }

        if (activity is OnFileItemLongClickListener) {
            itemLongClick = activity as OnFileItemLongClickListener?
        }
    }

    override fun onDetach() {
        super.onDetach()
        fileSelectedListener = null
        itemLongClick = null
    }

    private inner class FileAdapter constructor(context: Context, files: Array<File>) : ArrayAdapter<File>(context, android.R.layout.simple_expandable_list_item_1, files) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView
                    ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            val file = getItem(position)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            text1.text = if (file == null) "../" else if (file.isDirectory) file.name + File.separatorChar else file.name
            return view
        }

        override fun getItem(position: Int): File? {
            return if (mShowParentMover)
                if (position == 0)
                    null
                else
                    super.getItem(position - 1)
            else
                super.getItem(position)
        }

        override fun getCount(): Int {
            return if (mShowParentMover)
                super.getCount() + 1
            else
                super.getCount()
        }
    }

    interface OnFileItemLongClickListener {
        fun onListFileItemLongClick(l: ListView, v: View, file: File, position: Int, id: Long): Boolean
    }

    interface OnFileSelectedListener {
        fun onFileSelected(file: File)
    }

    companion object {
        private const val EXTRA_FILE = "file"
        private const val EXTRA_PARENT_MOVER = "mover"
        private const val EXTRA_DIR_ONLY = "dir_only"

        fun newInstance(file: File, parentMover: Boolean, directoryOnly: Boolean): FileListFragment {
            val fragment = FileListFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_FILE, file)
            bundle.putBoolean(EXTRA_PARENT_MOVER, parentMover)
            bundle.putBoolean(EXTRA_DIR_ONLY, directoryOnly)
            fragment.arguments = bundle
            return fragment
        }
    }
}
