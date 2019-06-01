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

package jp.hazuki.yuzubrowser.legacy.reader

import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import jp.hazuki.yuzubrowser.legacy.Constants
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs

class ReaderActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val themeMode = AppPrefs.reader_theme.get()
        if (themeMode >= 0) {
            delegate.localNightMode = if (themeMode == 0) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        setSupportActionBar(findViewById(R.id.toolbar))

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)


        val intent = intent
        if (intent != null) {
            var fullscreen = AppPrefs.fullscreen.get()
            var orientation = AppPrefs.oritentation.get()

            val url = intent.getStringExtra(Constants.intent.EXTRA_URL)
            val ua = intent.getStringExtra(Constants.intent.EXTRA_USER_AGENT)
            fullscreen = intent.getBooleanExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, fullscreen)
            orientation = intent.getIntExtra(Constants.intent.EXTRA_MODE_ORIENTATION, orientation)

            if (fullscreen)
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            requestedOrientation = orientation

            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, ReaderFragment(url, ua))
                        .commit()
            }
        } else {
            finish()
        }
    }


    override fun setTitle(title: CharSequence?) {
        val actionBar = supportActionBar
        if (actionBar != null)
            actionBar.title = title
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
