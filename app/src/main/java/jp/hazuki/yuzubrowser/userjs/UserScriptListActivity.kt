package jp.hazuki.yuzubrowser.userjs

import android.os.Bundle

import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity

class UserScriptListActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, UserScriptListFragment())
                .commit()
    }
}
