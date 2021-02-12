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
import androidx.activity.viewModels
import jp.hazuki.yuzubrowser.legacy.databinding.ScrollEditTextModel
import jp.hazuki.yuzubrowser.legacy.databinding.ScrollEdittextBinding
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import java.io.*

class TextEditActivity : ThemeActivity() {
    private lateinit var mFile: File

    private lateinit var binding: ScrollEdittextBinding

    private val viewModel by viewModels<ScrollEditTextModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScrollEdittextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.model = viewModel

        val file = intent.getSerializableExtra(Intent.EXTRA_STREAM) as? File
        if (file == null) {
            finish()
            return
        }
        mFile = file

        title = file.name

        val builder = StringBuilder()
        try {
            BufferedReader(FileReader(file)).use { reader ->

                val buf = CharArray(1024)
                var n: Int
                while (reader.read(buf).let { n = it; it > 0 }) {
                    builder.append(buf, 0, n)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        viewModel.text.value = builder.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Save").setOnMenuItemClickListener {
            AlertDialog.Builder(this@TextEditActivity)
                    .setTitle("Confirm")
                    .setMessage("Save?")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        try {
                            BufferedWriter(FileWriter(mFile)).use { os -> os.write(viewModel.text.value) }
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
