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

package jp.hazuki.yuzubrowser.download.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import jp.hazuki.yuzubrowser.download.R
import java.io.File

class FallbackFolderSelectActivity : AppCompatActivity() {
    private val rootPath = Environment.getExternalStorageDirectory().path

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        if (savedInstanceState == null)
            FolderSelectFragment(rootPath, rootPath).show(supportFragmentManager, TAG)
    }

    class FolderSelectFragment : androidx.fragment.app.DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity ?: throw IllegalStateException()
            val arguments = arguments ?: throw IllegalArgumentException()

            val root = arguments.getString(ROOT) ?: throw IllegalArgumentException()
            val path = arguments.getString(PATH)
            val parent = File(path)
            val directories = parent.listFiles()
                    .asSequence()
                    .filter { it.isDirectory }
                    .map { it.name }
                    .sorted()
                    .toMutableList()

            var canGoUp = false

            if (path != root) {
                directories.add(0, "../")
                canGoUp = true
            }

            isCancelable = false

            return AlertDialog.Builder(activity)
                    .setTitle(parent.name)
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        activity.finish()
                    }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val data = Intent().apply { data = Uri.parse("file://$path") }
                        activity.apply {
                            setResult(Activity.RESULT_OK, data)
                            finish()
                        }
                    }
                    .setItems(directories.toTypedArray()) { _, which ->
                        if (canGoUp && which == 0) {
                            moveTo(root, parent.parent)
                        } else {
                            moveTo(root, "$path/${directories[which]}")
                        }
                        dismiss()
                    }
                    .setOnKeyListener { _, keyCode, event ->
                        if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                            if (canGoUp) {
                                moveTo(root, parent.parent)
                                dismiss()
                            } else {
                                activity.finish()
                            }
                            true
                        } else {
                            false
                        }
                    }
                    .create()
        }

        private fun moveTo(root: String, path: String) {
            fragmentManager?.let { FolderSelectFragment(root, path).show(it, TAG) }
        }

        companion object {
            private const val ROOT = "root"
            private const val PATH = "path"

            operator fun invoke(root: String, path: String): FolderSelectFragment {
                return FolderSelectFragment().apply {
                    arguments = Bundle().apply {
                        putString(ROOT, root)
                        putString(PATH, path)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "dialog"
    }
}