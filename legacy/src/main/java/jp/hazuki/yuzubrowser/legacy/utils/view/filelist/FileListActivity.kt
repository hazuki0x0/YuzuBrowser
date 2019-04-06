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

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.widget.EditText
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import java.io.File

open class FileListActivity : ThemeActivity(), FileListFragment.OnFileSelectedListener {

    private val currentAbsolutePath: String?
        get() {
            val fragment = supportFragmentManager.findFragmentById(R.id.container) as FileListFragment?
            return if (fragment != null) {
                fragment.currentFolder!!.absolutePath
            } else null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        val file = intent?.getSerializableExtra(EXTRA_FILE) as? File
                ?: File(applicationInfo.dataDir)

        val fragment = FileListFragment.newInstance(file, false, false)

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()

    }

    override fun onFileSelected(file: File) {
        val intent = Intent()
        intent.putExtra(EXTRA_FILE, file)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.set_folder_path).setOnMenuItemClickListener {
            val editText = EditText(this@FileListActivity)
            editText.setSingleLine(true)
            editText.setText(currentAbsolutePath)

            AlertDialog.Builder(this@FileListActivity)
                    .setTitle(R.string.set_folder_path)
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok) { _, _ -> setFilePath(File(editText.text.toString())) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            true
        }
        menu.add(R.string.go_data_folder).setOnMenuItemClickListener {
            setFilePath(File(applicationInfo.dataDir))
            true
        }
        menu.add(R.string.go_sd_folder).setOnMenuItemClickListener {
            setFilePath(Environment.getExternalStorageDirectory())
            true
        }
        menu.add(R.string.quit).setOnMenuItemClickListener {
            finish()
            true
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun setFilePath(filePath: File) {
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? FileListFragment
        fragment?.setFilePath(filePath)
    }

    companion object {
        const val EXTRA_FILE = "FileListActivity.extra.EXTRA_TARGET_DIRECTORY"
    }
}
