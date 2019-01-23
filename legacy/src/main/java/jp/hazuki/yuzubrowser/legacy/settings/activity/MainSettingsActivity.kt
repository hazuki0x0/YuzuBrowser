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

package jp.hazuki.yuzubrowser.legacy.settings.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.debug.DebugActivity
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class MainSettingsActivity : ThemeActivity(), PreferenceFragmentCompat.OnPreferenceStartScreenCallback, ReplaceFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Intent.ACTION_CREATE_SHORTCUT == intent?.action) {
            @Suppress("DEPRECATION")
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent(this@MainSettingsActivity, MainSettingsActivity::class.java))
                putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(applicationContext, R.mipmap.ic_launcher))
                putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.browser_settings))
            })
            finish()
            return
        }

        setContentView(R.layout.activity_settings)
        setupActionBar()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainSettingsFragment())
                    .commit()
        }
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Debug mode").intent = Intent(this, DebugActivity::class.java)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat?, pref: PreferenceScreen): Boolean {
        return if (caller is YuzuPreferenceFragment) {
            if (!caller.onPreferenceStartScreen(pref)) {
                replaceFragment(PreferenceScreenFragment.newInstance(caller.preferenceResId, pref.key), pref.key)
            }
            true
        } else {
            false
        }
    }

    override fun replaceFragment(fragment: androidx.fragment.app.Fragment, key: String) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(key)
                .commit()
    }
}
