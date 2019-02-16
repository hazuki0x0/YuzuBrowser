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

package jp.hazuki.yuzubrowser.legacy.download.ui

import android.os.Bundle
import android.view.WindowManager
import jp.hazuki.yuzubrowser.legacy.Constants
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.legacy.download.service.connection.ActivityClient
import jp.hazuki.yuzubrowser.legacy.download.service.connection.ServiceSocket
import jp.hazuki.yuzubrowser.legacy.download.ui.fragment.DownloadListFragment
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class DownloadListActivity : ThemeActivity(), ActivityClient.ActivityClientListener, DownloadCommandController {

    private lateinit var downloadService: ServiceSocket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var fullscreen = AppData.fullscreen.get()
        var orientation = AppData.oritentation.get()
        if (intent != null) {
            fullscreen = intent.getBooleanExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, fullscreen)
            orientation = intent.getIntExtra(Constants.intent.EXTRA_MODE_ORIENTATION, orientation)
        }
        if (fullscreen)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = orientation

        downloadService = ServiceSocket(this, this)


        setContentView(R.layout.fragment_base)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, DownloadListFragment(), TAG)
                    .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        downloadService.bindService()
    }

    override fun onPause() {
        super.onPause()
        downloadService.unbindService()
    }

    override fun update(info: DownloadFileInfo) {
        val fragment = supportFragmentManager.findFragmentByTag(TAG)
        if (fragment is ActivityClient.ActivityClientListener) {
            fragment.update(info)
        }
    }

    override fun getDownloadInfo(list: List<DownloadFileInfo>) {
        val fragment = supportFragmentManager.findFragmentByTag(TAG)
        if (fragment is ActivityClient.ActivityClientListener) {
            fragment.getDownloadInfo(list)
        }
    }

    override fun cancelDownload(id: Long) {
        downloadService.cancelDownload(id)
    }

    override fun pauseDownload(id: Long) {
        downloadService.pauseDownload(id)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    companion object {
        private const val TAG = "main"
    }
}