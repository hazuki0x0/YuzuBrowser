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

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import jp.hazuki.yuzubrowser.core.utility.extensions.resolveDirectoryPath
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.storage.getStorageList
import jp.hazuki.yuzubrowser.legacy.databinding.EnvironmentActivityBinding
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import java.io.IOException

class EnvironmentActivity : ThemeActivity() {

    private lateinit var binding: EnvironmentActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EnvironmentActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Environment"

        try {
            val externalStorageDir = Environment.getExternalStorageDirectory()
            binding.externalStorageDirTextView.text = externalStorageDir?.canonicalPath ?: "null"
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }

        val externalStorageState = Environment.getExternalStorageState()
        binding.externalStorageStateTextView.text = externalStorageState ?: "null"

        try {
            val externalFilesDir = getExternalFilesDir(null)
            binding.externalFilesDirTextView.text = externalFilesDir?.canonicalPath ?: "null"
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }

        getStorageList().forEach {
            binding.estimatedExternalFilesDirTextView.append(it.path)
            binding.estimatedExternalFilesDirTextView.append("\n")
        }

        val dlUri = Uri.parse(AppPrefs.download_folder.get())
        binding.downloadUriTextView.text = dlUri.toString()

        binding.downloadPathTextView.text = dlUri.resolveDirectoryPath(this)
    }

}
