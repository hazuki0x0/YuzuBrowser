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
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import kotlinx.android.synthetic.main.environment_activity.*
import java.io.IOException

class EnvironmentActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.environment_activity)
        title = "Environment"

        try {
            val externalStorageDir = Environment.getExternalStorageDirectory()
            externalStorageDirTextView.text = externalStorageDir?.canonicalPath ?: "null"
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }

        val externalStorageState = Environment.getExternalStorageState()
        externalStorageStateTextView.text = externalStorageState ?: "null"

        try {
            val externalFilesDir = getExternalFilesDir(null)
            externalFilesDirTextView.text = externalFilesDir?.canonicalPath ?: "null"
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }

        getStorageList().forEach {
            estimatedExternalFilesDirTextView.append(it.path)
            estimatedExternalFilesDirTextView.append("\n")
        }

        val dlUri = Uri.parse(AppPrefs.download_folder.get())
        downloadUriTextView.text = dlUri.toString()

        downloadPathTextView.text = dlUri.resolveDirectoryPath(this)
    }

}
