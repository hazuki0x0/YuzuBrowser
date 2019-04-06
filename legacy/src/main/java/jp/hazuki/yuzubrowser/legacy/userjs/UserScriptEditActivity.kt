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

package jp.hazuki.yuzubrowser.legacy.userjs

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu

import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import kotlinx.android.synthetic.main.scroll_edittext.*

class UserScriptEditActivity : ThemeActivity() {

    private lateinit var mUserScript: UserScriptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scroll_edittext)

        val intent = intent ?: throw NullPointerException("intent is null")
        val id = intent.getLongExtra(EXTRA_USERSCRIPT, -1)
        mUserScript = UserScriptDatabase.getInstance(applicationContext)[id] ?: UserScriptInfo()

        title = intent.getStringExtra(Intent.EXTRA_TITLE) ?: ""
        editText.setText(mUserScript.data)
    }

    override fun onBackPressed() {
        showSaveDialog(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.userjs_save).setOnMenuItemClickListener {
            showSaveDialog(false)
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun showSaveDialog(orFinish: Boolean) {
        val builder = AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.userjs_save_confirm)
                .setPositiveButton(R.string.save) { _, _ ->
                    mUserScript.data = editText.text.toString()
                    UserScriptDatabase.getInstance(applicationContext).set(mUserScript)
                    setResult(RESULT_OK)
                    finish()
                }
                .setNeutralButton(android.R.string.cancel, null)
        if (orFinish)
            builder.setNegativeButton(R.string.not_save) { _, _ -> finish() }
        builder.show()
    }

    companion object {
        const val EXTRA_USERSCRIPT = "UserScriptEditActivity.extra.userscript"
    }
}
