package jp.hazuki.yuzubrowser.settings.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceScreen
import android.view.Menu
import android.view.MenuItem
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.debug.DebugActivity
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity

class MainSettingsActivity : ThemeActivity(), PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

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
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container,
                            PreferenceScreenFragment.newInstance(caller.preferenceResId, pref.key))
                    .addToBackStack(pref.key)
                    .commit()
            true
        } else {
            false
        }
    }

    override fun lightThemeResource(): Int {
        return R.style.CustomThemeLight_Pref
    }
}
