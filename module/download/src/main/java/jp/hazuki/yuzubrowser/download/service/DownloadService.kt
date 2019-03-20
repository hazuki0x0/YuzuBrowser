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

package jp.hazuki.yuzubrowser.download.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.*
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import dagger.android.DaggerService
import jp.hazuki.yuzubrowser.core.utility.extensions.resolvePath
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.download.*
import jp.hazuki.yuzubrowser.download.core.data.DownloadFile
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.data.DownloadRequest
import jp.hazuki.yuzubrowser.download.core.data.MetaData
import jp.hazuki.yuzubrowser.download.core.downloader.Downloader
import jp.hazuki.yuzubrowser.download.core.utils.getNotificationString
import jp.hazuki.yuzubrowser.download.core.utils.registerMediaScanner
import jp.hazuki.yuzubrowser.download.core.utils.toDocumentFile
import jp.hazuki.yuzubrowser.download.service.connection.ServiceClient
import jp.hazuki.yuzubrowser.download.service.connection.ServiceCommand
import jp.hazuki.yuzubrowser.download.service.connection.ServiceSocket
import jp.hazuki.yuzubrowser.download.ui.DownloadListActivity
import okhttp3.OkHttpClient
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import javax.inject.Inject

class DownloadService : DaggerService(), ServiceClient.ServiceClientListener {

    private lateinit var handler: Handler
    private lateinit var powerManager: PowerManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var database: DownloadDatabase
    private lateinit var messenger: Messenger

    private val threadList = mutableListOf<DownloadThread>()
    private val observers = mutableListOf<Messenger>()

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()
        handler = Handler()
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        database = DownloadDatabase.getInstance(this)
        messenger = Messenger(ServiceClient(this))

        val notify = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_DOWNLOAD_SERVICE)
                .setContentTitle(getText(R.string.download_service))
                .setSmallIcon(R.drawable.ic_yuzubrowser_white)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()

        database.cleanUp()

        val filter = IntentFilter(INTENT_ACTION_CANCEL_DOWNLOAD)
        filter.addAction(INTENT_ACTION_PAUSE_DOWNLOAD)

        registerReceiver(notificationControl, filter)

        startForeground(Int.MIN_VALUE, notify)
    }

    override fun onDestroy() {
        synchronized(threadList) {
            threadList.forEach {
                it.abort()
            }
        }
        unregisterReceiver(notificationControl)
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder = messenger.binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            var thread: DownloadThread? = null
            when (intent.action) {
                INTENT_ACTION_START_DOWNLOAD -> {
                    val root = intent.getParcelableExtra<Uri>(INTENT_EXTRA_DOWNLOAD_ROOT_URI).toDocumentFile(this)
                    val file = intent.getParcelableExtra<DownloadFile>(INTENT_EXTRA_DOWNLOAD_REQUEST)
                    val metadata = intent.getParcelableExtra<MetaData?>(INTENT_EXTRA_DOWNLOAD_METADATA)
                    if (file != null) {
                        thread = FirstDownloadThread(root, file, metadata)
                    }
                }
                INTENT_ACTION_RESTART_DOWNLOAD -> {
                    val id = intent.getLongExtra(INTENT_EXTRA_DOWNLOAD_ID, -1)
                    val info = database[id]
                    if (info != null) {
                        thread = ReDownloadThread(info, DownloadRequest(null, null, null))
                    }
                }
            }

            if (thread != null) {
                synchronized(threadList) {
                    threadList.add(thread)
                }
                thread.start()
            }
        }
        return START_NOT_STICKY
    }

    override fun registerObserver(messenger: Messenger) {
        observers.add(messenger)
    }

    override fun unregisterObserver(messenger: Messenger) {
        observers.remove(messenger)
    }

    override fun update(msg: Message) {
        val it = observers.iterator()
        while (it.hasNext()) {
            try {
                it.next().send(Message.obtain(msg))
            } catch (e: RemoteException) {
                ErrorReport.printAndWriteLog(e)
                it.remove()
            }
        }
    }

    override fun getDownloadInfo(messenger: Messenger) {
        val list = synchronized(threadList) { threadList.map { it.info } }

        val it = observers.iterator()
        while (it.hasNext()) {
            try {
                it.next().send(Message.obtain(null, ServiceSocket.GET_DOWNLOAD_INFO, list))
            } catch (e: RemoteException) {
                ErrorReport.printAndWriteLog(e)
                it.remove()
            }
        }
    }

    override fun cancelDownload(id: Long) {
        synchronized(threadList) {
            threadList.firstOrNull { id == it.info.id }?.cancel()
        }
    }

    override fun pauseDownload(id: Long) {
        synchronized(threadList) {
            threadList.firstOrNull { id == it.info.id }?.pause()
        }
    }

    private val notificationControl = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null) return

            val id = intent.getLongExtra(INTENT_EXTRA_DOWNLOAD_ID, -1)
            if (id >= 0) {
                when (intent.action) {
                    INTENT_ACTION_CANCEL_DOWNLOAD -> cancelDownload(id)
                    INTENT_ACTION_PAUSE_DOWNLOAD -> pauseDownload(id)
                }
            }
        }
    }

    private inner class FirstDownloadThread(private val root: DocumentFile, private val file: DownloadFile, private val metadata: MetaData?) : DownloadThread(file.request) {
        override val info: DownloadFileInfo by lazy {
            DownloadFileInfo(root, file, metadata
                    ?: MetaData(this@DownloadService, okHttpClient, root, file.url, file.request, file.name))
        }
    }

    private inner class ReDownloadThread(override val info: DownloadFileInfo, request: DownloadRequest) : DownloadThread(request)

    private abstract inner class DownloadThread(private val request: DownloadRequest) : Thread(), Downloader.DownloadListener {
        abstract val info: DownloadFileInfo

        private val notification = NotificationCompat.Builder(this@DownloadService, NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY)
        private val bigTextStyle = NotificationCompat.BigTextStyle()

        private var downloader: Downloader? = null

        private var isActionAdded = false
        private var isAbort = false

        @SuppressLint("WakelockTimeout")
        override fun run() {
            if (isAbort) return

            val wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DownloadThread:wakelock")
            prepareThread(wakelock)

            if (info.id < 0) {
                database.insert(info)
            } else {
                info.state = DownloadFileInfo.STATE_DOWNLOADING
                database.update(info)
            }

            if (!info.root.exists()) {
                failedCheckFolder(info, R.string.download_failed_root_not_exists)
                endThreaded(wakelock)
                return
            } else if (!info.root.canWrite()) {
                failedCheckFolder(info, R.string.download_failed_root_not_writable)
                endThreaded(wakelock)
                return
            }

            val downloader = Downloader.getDownloader(this@DownloadService, okHttpClient, info, request)
            this.downloader = downloader

            downloader.downloadListener = this

            downloader.download()

            endThreaded(wakelock)
        }

        fun cancel() = downloader?.cancel()

        fun pause() = downloader?.pause()

        fun abort() {
            downloader?.abort()
            isAbort = true
        }

        override fun onStartDownload(info: DownloadFileInfo) {
            database.update(info)
            notification.run {
                setSmallIcon(android.R.drawable.stat_sys_download)
                setOngoing(false)
                setContentTitle(info.name)
                setWhen(info.startTime)
                setProgress(0, 0, true)
                setContentIntent(PendingIntent.getActivity(applicationContext, 0, intentFor<DownloadListActivity>(), 0))
                notificationManager.notify(info.id.toInt(), build())
            }
            updateInfo(ServiceSocket.UPDATE, info)
        }

        override fun onFileDownloaded(info: DownloadFileInfo, downloadedFile: DocumentFile) {
            if (info.size < 0) {
                info.size = info.currentSize
            }
            database.update(info)
            NotificationCompat.Builder(this@DownloadService, NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY).run {
                setOngoing(false)
                setContentTitle(info.name)
                setWhen(System.currentTimeMillis())
                setProgress(0, 0, false)
                setAutoCancel(true)
                setContentText(getText(R.string.download_success))
                setSmallIcon(android.R.drawable.stat_sys_download_done)
                setContentIntent(PendingIntent.getActivity(applicationContext, 0, info.createFileOpenIntent(this@DownloadService, downloadedFile), 0))
                notificationManager.notify(info.id.toInt(), build())
            }
            updateInfo(ServiceSocket.UPDATE, info)
            info.root.findFile(info.name)?.uri?.resolvePath(this@DownloadService)
                    ?.let { registerMediaScanner(it) }
        }

        override fun onFileDownloadFailed(info: DownloadFileInfo, cause: String?) {
            database.update(info)
            if (cause != null) {
                handler.post { longToast(cause) }
                Logger.d("download error", cause)
            }
            NotificationCompat.Builder(this@DownloadService, NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY).run {
                setOngoing(false)
                setContentTitle(info.name)
                setWhen(System.currentTimeMillis())
                setProgress(0, 0, false)
                setAutoCancel(true)
                setContentText(getText(R.string.download_fail))
                setSmallIcon(android.R.drawable.stat_sys_warning)
                setContentIntent(PendingIntent.getActivity(applicationContext, 0, intentFor<DownloadListActivity>(), 0))
                notificationManager.notify(info.id.toInt(), build())
            }
            updateInfo(ServiceSocket.UPDATE, info)
        }

        override fun onFileDownloadAbort(info: DownloadFileInfo) {
            database.update(info)
            if (info.state == DownloadFileInfo.STATE_PAUSED) {
                NotificationCompat.Builder(this@DownloadService, NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY).run {
                    setOngoing(false)
                    setContentTitle(info.name)
                    setWhen(System.currentTimeMillis())
                    setAutoCancel(true)
                    setContentText(getText(R.string.download_paused))
                    setSmallIcon(R.drawable.ic_pause_white_24dp)
                    setContentIntent(PendingIntent.getActivity(applicationContext, 0, intentFor<DownloadListActivity>(), 0))

                    val resume = Intent(this@DownloadService, DownloadService::class.java).apply {
                        action = INTENT_ACTION_RESTART_DOWNLOAD
                        putExtra(INTENT_EXTRA_DOWNLOAD_ID, info.id)
                    }
                    addAction(R.drawable.ic_start_white_24dp, getText(R.string.resume_download),
                            PendingIntent.getService(this@DownloadService, info.id.toInt(), resume, 0))
                    notificationManager.notify(info.id.toInt(), build())
                }
            } else {
                notificationManager.cancel(info.id.toInt())
            }
            updateInfo(ServiceSocket.UPDATE, info)
        }

        override fun onFileDownloading(info: DownloadFileInfo, progress: Long) {
            notification.run {
                setAction(info)
                if (info.size <= 0) {
                    setProgress(0, 0, true)
                } else {
                    setProgress(1000, (progress * 1000 / info.size).toInt(), false)
                }
                setStyle(bigTextStyle.bigText(info.getNotificationString(applicationContext)))
                notificationManager.notify(info.id.toInt(), build())
            }
            updateInfo(ServiceSocket.UPDATE, info)
        }

        private fun setAction(info: DownloadFileInfo) {
            if (!isActionAdded) {
                isActionAdded = true
                notification.run {
                    if (info.resumable) {
                        val pause = Intent(INTENT_ACTION_PAUSE_DOWNLOAD).apply { putExtra(INTENT_EXTRA_DOWNLOAD_ID, info.id) }
                        addAction(R.drawable.ic_pause_white_24dp, getText(R.string.pause_download),
                                PendingIntent.getBroadcast(this@DownloadService, info.id.toInt(), pause, PendingIntent.FLAG_UPDATE_CURRENT))
                    }

                    val cancel = Intent(INTENT_ACTION_CANCEL_DOWNLOAD).apply { putExtra(INTENT_EXTRA_DOWNLOAD_ID, info.id) }
                    addAction(R.drawable.ic_cancel_white_24dp, getText(android.R.string.cancel),
                            PendingIntent.getBroadcast(this@DownloadService, info.id.toInt(), cancel, PendingIntent.FLAG_UPDATE_CURRENT))
                }
            }
        }

        private fun updateInfo(@ServiceCommand command: Int, info: DownloadFileInfo) {
            try {
                messenger.send(Message.obtain(null, command, info))
            } catch (e: RemoteException) {
                ErrorReport.printAndWriteLog(e)
            }
        }

        @SuppressLint("WakelockTimeout")
        private fun prepareThread(wakeLock: PowerManager.WakeLock) {
            wakeLock.acquire()
        }

        private fun endThreaded(wakeLock: PowerManager.WakeLock) {
            wakeLock.release()

            synchronized(threadList) {
                threadList.remove(this)
                if (threadList.isEmpty()) {
                    stopSelf()
                }
            }
        }

        private fun failedCheckFolder(info: DownloadFileInfo, @StringRes message: Int) {
            info.state = DownloadFileInfo.STATE_UNKNOWN_ERROR
            database.updateWithEmptyRoot(info)
            handler.post { toast(message) }
            NotificationCompat.Builder(this@DownloadService, NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY).run {
                setOngoing(false)
                setContentTitle(info.name)
                setWhen(System.currentTimeMillis())
                setProgress(0, 0, false)
                setAutoCancel(true)
                setContentText(getText(message))
                setSmallIcon(android.R.drawable.stat_sys_warning)
                setContentIntent(PendingIntent.getActivity(applicationContext, 0, intentFor<DownloadListActivity>(), 0))
                notificationManager.notify(info.id.toInt(), build())
            }
            updateInfo(ServiceSocket.UPDATE, info)
        }
    }
}