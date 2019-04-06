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

package jp.hazuki.yuzubrowser.legacy.action.item.startactivity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.EditText
import android.widget.Toast
import jp.hazuki.yuzubrowser.core.utility.extensions.getBitmap
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.utils.WebUtils
import jp.hazuki.yuzubrowser.legacy.utils.appinfo.AppInfo
import jp.hazuki.yuzubrowser.legacy.utils.appinfo.ApplicationListFragment
import jp.hazuki.yuzubrowser.legacy.utils.appinfo.ShortCutListFragment
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import java.net.URISyntaxException

class StartActivityPreferenceActivity : ThemeActivity(), StartActivityPreferenceFragment.OnActionListener, ApplicationListFragment.OnAppSelectListener, ShortCutListFragment.OnShortCutSelectListener {
    private var mCurrentIntent: Intent? = null
    private var mUrl = StartActivitySingleAction.REPLACE_URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        val intent = intent
        if (intent != null)
            mCurrentIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT)

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, StartActivityPreferenceFragment())
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.action_start_activity_edit_url).setOnMenuItemClickListener {
            val editText = EditText(this@StartActivityPreferenceActivity)
            editText.setSingleLine(true)
            editText.setText(mUrl)

            AlertDialog.Builder(this@StartActivityPreferenceActivity)
                    .setTitle(R.string.action_start_activity_edit_url)
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok) { _, _ -> mUrl = editText.text.toString() }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            false
        }
        menu.add(R.string.action_start_activity_edit_intent).setOnMenuItemClickListener {
            val editText = EditText(this@StartActivityPreferenceActivity)
            editText.setSingleLine(true)
            if (mCurrentIntent != null)
                editText.setText(mCurrentIntent!!.toUri(0))

            AlertDialog.Builder(this@StartActivityPreferenceActivity)
                    .setTitle(R.string.action_start_activity_edit_intent)
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        try {
                            val str = editText.text.toString()
                            val intent = Intent.parseUri(str, 0)
                            setResult(RESULT_OK, intent)
                            finish()
                        } catch (e: URISyntaxException) {
                            ErrorReport.printAndWriteLog(e)
                            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun startAppListFragment(type: Int, intent: Intent) {
        supportFragmentManager.beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, ApplicationListFragment.newInstance(type, intent))
                .commit()
    }

    override fun openApplicationList() {
        val target = Intent(Intent.ACTION_MAIN)
        target.addCategory(Intent.CATEGORY_LAUNCHER)
        startAppListFragment(RESULT_REQUEST_APP, target)
    }

    override fun openShortCutList() {
        supportFragmentManager.beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, ShortCutListFragment())
                .commit()
    }

    override fun openSharePage() {
        val queryIntent = WebUtils.createShareWebIntent(mUrl, StartActivitySingleAction.REPLACE_TITLE)
        startAppListFragment(RESULT_REQUEST_SHARE, queryIntent)
    }

    override fun openOther() {
        val queryIntent = WebUtils.createOpenInOtherAppIntent(mUrl)
        startAppListFragment(RESULT_REQUEST_OPEN_OTHER, queryIntent)
    }

    override fun onShortCutSelected(intent: Intent) {
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onAppSelected(type: Int, info: AppInfo) {
        val intent = Intent()
        intent.setClassName(info.packageName, info.className)

        when (type) {
            RESULT_REQUEST_SHARE -> {
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, StartActivitySingleAction.REPLACE_URI)
                intent.putExtra(Intent.EXTRA_SUBJECT, StartActivitySingleAction.REPLACE_TITLE)
            }
            RESULT_REQUEST_OPEN_OTHER -> WebUtils.createOpenInOtherAppIntent(intent, StartActivitySingleAction.REPLACE_URI)
        }

        @Suppress("DEPRECATION")
        val result = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)
            putExtra(Intent.EXTRA_SHORTCUT_ICON, info.icon.getBitmap())
        }
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        private const val RESULT_REQUEST_APP = 0
        private const val RESULT_REQUEST_SHARE = 1
        private const val RESULT_REQUEST_OPEN_OTHER = 2
    }
}
