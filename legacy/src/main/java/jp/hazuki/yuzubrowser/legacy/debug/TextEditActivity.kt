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
import android.os.Bundle
import android.view.Menu
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import kotlinx.android.synthetic.main.scroll_edittext.*
import java.io.*

class TextEditActivity : ThemeActivity() {
    private lateinit var mFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scroll_edittext)

        val file = intent.getSerializableExtra(Intent.EXTRA_STREAM) as? File
        if (file == null) {
            finish()
            return
        }
        mFile = file

        title = file.name

        val builder = StringBuilder()
        try {
            BufferedReader(FileReader(file)).use {

                val buf = CharArray(1024)
                var n = 0
                while (it.read(buf).let { n = it; it > 0 }) {
                    builder.append(buf, 0, n)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        editText.setText(builder)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Save").setOnMenuItemClickListener {
            AlertDialog.Builder(this@TextEditActivity)
                    .setTitle("Confirm")
                    .setMessage("Save?")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        try {
                            BufferedWriter(FileWriter(mFile)).use { os -> os.write(editText.text.toString()) }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }
}
