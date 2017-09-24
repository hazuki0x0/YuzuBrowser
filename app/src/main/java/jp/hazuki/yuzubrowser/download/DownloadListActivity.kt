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

package jp.hazuki.yuzubrowser.download

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.text.TextUtils
import android.view.Menu
import android.view.WindowManager
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import jp.hazuki.yuzubrowser.BrowserActivity
import jp.hazuki.yuzubrowser.Constants
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.FileUtils
import jp.hazuki.yuzubrowser.utils.PackageUtils
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity
import jp.hazuki.yuzubrowser.utils.database.ImplementedCursorLoader
import jp.hazuki.yuzubrowser.utils.service.ServiceBindHelper
import jp.hazuki.yuzubrowser.utils.service.ServiceConnectionHelper
import kotlinx.android.synthetic.main.download_activity.*
import java.io.File

class DownloadListActivity : ThemeActivity(), LoaderCallbacks<Cursor>, ServiceConnectionHelper<Messenger> {
    //private static final String TAG = "DownloadListActivity";
    private lateinit var mServiceBindHelper: ServiceBindHelper<Messenger>
    private lateinit var mActivityMessenger: Messenger

    private var mListAdapter: DownloadListAdapter? = null
    private lateinit var mDb: DownloadInfoDatabase

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

        setContentView(R.layout.download_activity)

        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val cursor = mListAdapter!!.getItem(position) as Cursor
            when (cursor.getInt(DownloadInfoDatabase.COLUMN_STATE_INDEX)) {
                DownloadInfo.STATE_DOWNLOADED -> try {
                    startActivity(PackageUtils.createFileOpenIntent(applicationContext, cursor.getString(DownloadInfoDatabase.COLUMN_FILEPATH_INDEX)))
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, R.string.app_notfound, Toast.LENGTH_SHORT).show()
                }

            }
        }

        listView!!.setOnCreateContextMenuListener { menu, _, menuInfo ->
            val info = menuInfo as AdapterContextMenuInfo
            val position = info.position
            val cursor = mListAdapter!!.getItem(position) as Cursor
            val id = cursor.getLong(DownloadInfoDatabase.COLUMN_ID_INDEX)
            val url = cursor.getString(DownloadInfoDatabase.COLUMN_URL_INDEX)
            val filePath = cursor.getString(DownloadInfoDatabase.COLUMN_FILEPATH_INDEX)

            when (cursor.getInt(DownloadInfoDatabase.COLUMN_STATE_INDEX)) {
                DownloadInfo.STATE_DOWNLOADED -> menu.add(R.string.open_file).setOnMenuItemClickListener {
                    try {
                        startActivity(PackageUtils.createFileOpenIntent(applicationContext, filePath))
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                        Toast.makeText(applicationContext, R.string.app_notfound, Toast.LENGTH_SHORT).show()
                    }

                    false
                }
                DownloadInfo.STATE_DOWNLOADING -> menu.add(R.string.cancel_download).setOnMenuItemClickListener {
                    DownloadService.cancelDownload(mServiceBindHelper.binder, mActivityMessenger, id)
                    false
                }
            }

            menu.add(R.string.open_url).setOnMenuItemClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setClass(applicationContext, BrowserActivity::class.java)
                intent.putExtra(Intent.EXTRA_TEXT, url)
                startActivity(intent)
                finish()
                false
            }

            if (cursor.getInt(DownloadInfoDatabase.COLUMN_STATE_INDEX) != DownloadInfo.STATE_DOWNLOADING) {
                menu.add(R.string.clear_download).setOnMenuItemClickListener {
                    mDb.delete(id)
                    supportLoaderManager.restartLoader(0, null, this@DownloadListActivity)
                    false
                }
            }

            if (!TextUtils.isEmpty(filePath) && cursor.getInt(DownloadInfoDatabase.COLUMN_STATE_INDEX) == DownloadInfo.STATE_DOWNLOADED) {
                val file = File(filePath)
                if (file.exists()) {
                    menu.add(R.string.delete_download).setOnMenuItemClickListener {
                        FileUtils.deleteFile(file)
                        mDb.delete(id)
                        supportLoaderManager.restartLoader(0, null, this@DownloadListActivity)
                        false
                    }
                }
            }
        }

        mDb = DownloadInfoDatabase.getInstance(applicationContext)
        mActivityMessenger = Messenger(DownloadListActivityHandler(this))

        mServiceBindHelper = ServiceBindHelper(applicationContext, this)

        mListAdapter = DownloadListAdapter(this, null)
        listView.adapter = mListAdapter

        supportLoaderManager.initLoader(0, null, this)
    }

    fun updateProgress(info: DownloadRequestInfo) {
        if (mListAdapter != null) {
            mListAdapter!!.pushDownloadList(info)
            mListAdapter!!.notifyDataSetChanged()
        }
    }

    fun updateState() {
        if (mListAdapter != null) {
            supportLoaderManager.restartLoader(0, null, this)
        }
    }

    fun pushDownloadList(info: DownloadRequestInfo) {
        mListAdapter?.pushDownloadList(info)
    }

    fun notifyDataSetChanged() {
        mListAdapter?.notifyDataSetChanged()
    }

    override fun onCreateLoader(arg0: Int, arg1: Bundle): Loader<Cursor> {
        return ImplementedCursorLoader(applicationContext, mDb)
    }

    override fun onLoadFinished(arg0: Loader<Cursor>, cursor: Cursor) {
        mListAdapter!!.swapCursor(cursor)
    }

    override fun onLoaderReset(arg0: Loader<Cursor>) {
        mListAdapter!!.swapCursor(null)
    }

    override fun onPause() {
        super.onPause()
        mServiceBindHelper.unbindService()
    }

    override fun onResume() {
        super.onResume()
        mServiceBindHelper.bindService(Intent(applicationContext, DownloadService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        listView!!.adapter = null
    }

    override fun onBind(service: IBinder): Messenger {
        val messenger = DownloadService.registerObserver(service, mActivityMessenger)
        DownloadService.getDownloadInfo(messenger, mActivityMessenger)
        return messenger
    }

    override fun onUnbind(service: Messenger) {
        DownloadService.unregisterObserver(mServiceBindHelper.binder, mActivityMessenger)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.delete_all_history).setOnMenuItemClickListener {
            AlertDialog.Builder(this@DownloadListActivity)
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.confirm_delete_all_history)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        mDb.deleteAllHistory()

                        supportLoaderManager.restartLoader(0, null, this@DownloadListActivity)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }
}
