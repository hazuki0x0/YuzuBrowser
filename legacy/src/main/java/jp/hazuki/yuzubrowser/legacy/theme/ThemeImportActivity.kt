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

package jp.hazuki.yuzubrowser.legacy.theme

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.settings.activity.MainSettingsActivity
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import jp.hazuki.yuzubrowser.ui.dialog.ProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThemeImportActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        if (intent != null && Intent.ACTION_VIEW == intent.action && intent.data != null) {
            val uri = intent.data ?: return

            ui {
                val dialog = ProgressDialog("Installing...", false, false)
                dialog.show(supportFragmentManager, "dialog")

                val result = withContext(Dispatchers.Default) { importTheme(applicationContext, uri) }

                dialog.dismiss()
                if (result.isSuccess) {
                    Toast.makeText(applicationContext, getString(R.string.theme_imported, result.message), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, MainSettingsActivity::class.java))
                } else {
                    Toast.makeText(applicationContext, result.message, Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        } else {
            finish()
        }
    }
}
