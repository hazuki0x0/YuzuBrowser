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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import java.io.File

class DebugFileListActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        val file = intent.getSerializableExtra(EXTRA_PATH) as File?

        supportFragmentManager.commit {
            replace(R.id.container, FileBrowserFragment(file))
        }
    }

    companion object {
        private const val EXTRA_PATH = "path_file"

        fun createIntent(context: Context, file: File?) =
            Intent(context, DebugFileListActivity::class.java).apply {
                putExtra(EXTRA_PATH, file)
            }
    }
}
