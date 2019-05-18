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

package jp.hazuki.yuzubrowser.legacy.speeddial.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import jp.hazuki.yuzubrowser.bookmark.view.BookmarkActivity
import jp.hazuki.yuzubrowser.core.utility.extensions.getBitmap
import jp.hazuki.yuzubrowser.history.presenter.BrowserHistoryActivity
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDial
import jp.hazuki.yuzubrowser.legacy.speeddial.WebIcon
import jp.hazuki.yuzubrowser.legacy.utils.appinfo.AppInfo
import jp.hazuki.yuzubrowser.legacy.utils.appinfo.ApplicationListFragment
import jp.hazuki.yuzubrowser.legacy.utils.appinfo.ShortCutListFragment
import jp.hazuki.yuzubrowser.legacy.utils.stack.SingleStack
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class SpeedDialSettingActivity : ThemeActivity(), SpeedDialEditCallBack, androidx.fragment.app.FragmentManager.OnBackStackChangedListener, SpeedDialSettingActivityFragment.OnSpeedDialAddListener, SpeedDialSettingActivityEditFragment.GoBackController, ApplicationListFragment.OnAppSelectListener, ShortCutListFragment.OnShortCutSelectListener {

    private val speedDialStack = object : SingleStack<SpeedDial>() {
        override fun processItem(item: SpeedDial) {
            goEdit(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        supportFragmentManager.addOnBackStackChangedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, SpeedDialSettingActivityFragment(), "main")
                    .commit()

            if (intent != null && ACTION_ADD_SPEED_DIAL == intent.action) {
                val title = intent.getStringExtra(Intent.EXTRA_TITLE)
                val url = intent.getStringExtra(Intent.EXTRA_TEXT)
                val icon = intent.getParcelableExtra<Bitmap>(EXTRA_ICON)
                speedDialStack.addItem(SpeedDial(url, title, WebIcon.createIconOrNull(icon), true))
            }
        }

        shouldDisplayHomeUp()
    }

    override fun goBack(): Boolean = supportFragmentManager.popBackStackImmediate()

    override fun goEdit(speedDial: SpeedDial) {
        supportFragmentManager.beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, SpeedDialSettingActivityEditFragment.newInstance(speedDial))
                .commit()
    }

    override fun addFromBookmark() {
        val intent = Intent(this, BookmarkActivity::class.java)
                .apply { action = Intent.ACTION_PICK }
        startActivityForResult(intent, RESULT_REQUEST_BOOKMARK)
    }

    override fun addFromHistory() {
        val intent = Intent(this, BrowserHistoryActivity::class.java)
                .apply { action = Intent.ACTION_PICK }
        startActivityForResult(intent, RESULT_REQUEST_HISTORY)
    }

    override fun addFromAppList() {
        val target = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }

        supportFragmentManager.beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, ApplicationListFragment.newInstance(target))
                .commit()
    }

    override fun addFromShortCutList() {
        supportFragmentManager.beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, ShortCutListFragment())
                .commit()
    }

    @Suppress("DEPRECATION")
    override fun onShortCutSelected(data: Intent) {
        goBack()
        val intent = data.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
        val name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
        var icon: Bitmap? = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)
        if (icon == null) {
            val iconRes = data.getParcelableExtra<Intent.ShortcutIconResource>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
            if (iconRes != null) {
                try {
                    val foreignResources = packageManager.getResourcesForApplication(iconRes.packageName)
                    val id = foreignResources.getIdentifier(iconRes.resourceName, null, null)
                    icon = BitmapFactory.decodeResource(foreignResources, id)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

            }

            if (icon == null) {
                try {
                    val component = intent.component
                    if (component != null) {
                        val drawable = packageManager.getApplicationIcon(component.packageName)
                        icon = drawable.getBitmap()
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

            }
        }

        val webIcon = WebIcon.createIconOrNull(icon)

        val speedDial = SpeedDial(intent.toUri(Intent.URI_INTENT_SCHEME), name, webIcon, false)
        speedDialStack.addItem(speedDial)
    }

    override fun onAppSelected(type: Int, appInfo: AppInfo) {
        goBack()
        val intent = Intent()
        intent.setClassName(appInfo.packageName, appInfo.className)
        val webIcon = WebIcon.createIcon(appInfo.icon.getBitmap())
        val speedDial = SpeedDial(intent.toUri(Intent.URI_INTENT_SCHEME), appInfo.appName, webIcon, false)
        speedDialStack.addItem(speedDial)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_REQUEST_BOOKMARK, RESULT_REQUEST_HISTORY -> {
                if (resultCode != Activity.RESULT_OK || data == null) return
                val title = data.getStringExtra(Intent.EXTRA_TITLE)
                val url = data.getStringExtra(Intent.EXTRA_TEXT)
                val icon = data.getByteArrayExtra(Intent.EXTRA_STREAM)
                val speedDial = if (icon == null) {
                    SpeedDial(url, title)
                } else {
                    SpeedDial(url, title, WebIcon(icon), true)
                }
                speedDialStack.addItem(speedDial)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onEdited(speedDial: SpeedDial) {
        goBack()
        val fragment = supportFragmentManager.findFragmentByTag("main")
        if (fragment is SpeedDialEditCallBack) {
            fragment.onEdited(speedDial)
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

    private fun shouldDisplayHomeUp() {
        val canBack = supportFragmentManager.backStackEntryCount == 0
        supportActionBar?.setDisplayHomeAsUpEnabled(canBack)
    }

    override fun onPause() {
        super.onPause()
        speedDialStack.onPause()
    }

    override fun onResume() {
        super.onResume()
        speedDialStack.onResume()
    }

    override fun onBackStackChanged() {
        shouldDisplayHomeUp()
    }

    companion object {
        const val ACTION_ADD_SPEED_DIAL = "jp.hazuki.yuzubrowser.legacy.speeddial.view.SpeedDialSettingActivity.add_speed_dial"
        const val EXTRA_ICON = ACTION_ADD_SPEED_DIAL + ".icon"

        private const val RESULT_REQUEST_BOOKMARK = 100
        private const val RESULT_REQUEST_HISTORY = 101
    }
}
