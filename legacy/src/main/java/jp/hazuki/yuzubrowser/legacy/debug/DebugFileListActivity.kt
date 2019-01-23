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

package jp.hazuki.yuzubrowser.legacy.debug

import android.app.AlertDialog
import android.content.Intent
import android.view.Menu
import android.view.MenuItem.OnMenuItemClickListener
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.utils.view.filelist.FileListActivity
import jp.hazuki.yuzubrowser.legacy.utils.view.filelist.FileListFragment
import java.io.File

class DebugFileListActivity : FileListActivity(), FileListFragment.OnFileItemLongClickListener {
    private var mCopiedFile: File? = null

    private val currentFolder: File?
        get() {
            val fragment = supportFragmentManager.findFragmentById(R.id.container) as? FileListFragment
            return fragment?.currentFolder
        }

    override fun onFileSelected(file: File) {
        startActivity(Intent(applicationContext, TextEditActivity::class.java).apply {
            putExtra(Intent.EXTRA_STREAM, file)
        })
    }

    override fun onListFileItemLongClick(l: ListView, v: View, file: File, position: Int, id: Long): Boolean {
        AlertDialog.Builder(this)
                .setTitle(file.name)
                .setItems(
                        arrayOf("Copy", "Delete")
                ) { _, which ->
                    when (which) {
                        0 -> {
                            if (file.isFile) {
                                Toast.makeText(applicationContext, "file copied", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(applicationContext, "folder copied", Toast.LENGTH_SHORT).show()
                            }
                            mCopiedFile = file
                        }
                        1 -> AlertDialog.Builder(this@DebugFileListActivity)
                                .setTitle("Confirm")
                                .setMessage("Delete?")
                                .setPositiveButton(android.R.string.yes) { _, _ ->
                                    if (!FileUtils.deleteFile(file)) {
                                        Toast.makeText(applicationContext, "Delete failed", Toast.LENGTH_SHORT).show()
                                    }
                                    notifyDataSetChanged()
                                }
                                .setNegativeButton(android.R.string.no, null)
                                .show()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add("Paste").setOnMenuItemClickListener(OnMenuItemClickListener {
            if (mCopiedFile == null) {
                Toast.makeText(applicationContext, "File not available", Toast.LENGTH_SHORT).show()
                return@OnMenuItemClickListener true
            }

            AlertDialog.Builder(this@DebugFileListActivity)
                    .setTitle("Confirm")
                    .setMessage("Paste?")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        if (!FileUtils.copyFile(mCopiedFile, currentFolder)) {
                            Toast.makeText(applicationContext, "failed", Toast.LENGTH_SHORT).show()
                        }
                        notifyDataSetChanged()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            true
        })
        menu.add("Make folder").setOnMenuItemClickListener {
            val editText = EditText(this@DebugFileListActivity)
            editText.maxLines = 1
            editText.setText("new folder")

            AlertDialog.Builder(this@DebugFileListActivity)
                    .setTitle("Make folder")
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        File(currentFolder, editText.text.toString()).mkdir()
                        notifyDataSetChanged()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            true
        }
        return true
    }

    private fun notifyDataSetChanged() {
        (supportFragmentManager.findFragmentById(R.id.container) as? FileListFragment)?.notifyDataSetChanged()
    }
}
