/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.history

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import jp.hazuki.yuzubrowser.Constants
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity
import jp.hazuki.yuzubrowser.utils.extensions.convertDpToFloatPx

class BrowserHistoryActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)
        supportActionBar?.run {
            elevation = convertDpToFloatPx(1)
            setDisplayHomeAsUpEnabled(true)
        }

        var pickMode = false
        var fullscreen = AppData.fullscreen.get()
        var orientation = AppData.oritentation.get()
        intent?.run {
            if (Intent.ACTION_PICK == action)
                pickMode = true

            fullscreen = getBooleanExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, fullscreen)
            orientation = getIntExtra(Constants.intent.EXTRA_MODE_ORIENTATION, orientation)
        }

        if (fullscreen)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        requestedOrientation = orientation

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, BrowserHistoryFragment(pickMode))
                .commit()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment !is BrowserHistoryFragment || !fragment.onBackPressed()) {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
