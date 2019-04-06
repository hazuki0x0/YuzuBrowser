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

package jp.hazuki.yuzubrowser.download.service.connection

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import jp.hazuki.yuzubrowser.core.service.ServiceBindHelper
import jp.hazuki.yuzubrowser.core.service.ServiceConnectionHelper
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.download.service.DownloadService

class ServiceSocket(private val context: Context, listener: ActivityClient.ActivityClientListener) : ServiceConnectionHelper<Messenger> {
    private val messenger = Messenger(ActivityClient(listener))
    private val serviceHelper = ServiceBindHelper(context, this)

    override fun onBind(service: IBinder): Messenger {
        service.messenger.run {
            safeSend(createMessage(REGISTER_OBSERVER))
            safeSend(createMessage(GET_DOWNLOAD_INFO))
            return this
        }
    }

    override fun onUnbind(service: Messenger?) {
        serviceHelper.binder?.safeSend(createMessage(UNREGISTER_OBSERVER))
    }

    fun bindService() {
        serviceHelper.bindService(Intent(context, DownloadService::class.java))
    }

    fun unbindService() {
        serviceHelper.unbindService()
    }

    fun cancelDownload(id: Long) {
        serviceHelper.binder?.safeSend(createMessage(CANCEL_DOWNLOAD, id))
    }

    fun pauseDownload(id: Long) {
        serviceHelper.binder?.safeSend(createMessage(PAUSE_DOWNLOAD, id))
    }

    private fun Messenger.safeSend(message: Message) {
        try {
            send(message)
        } catch (e: RemoteException) {
            ErrorReport.printAndWriteLog(e)
        }
    }

    private fun createMessage(@ServiceCommand command: Int, obj: Any? = null) =
            Message.obtain(null, command, obj).apply { replyTo = messenger }

    private val IBinder.messenger
        get() = Messenger(this)

    companion object {
        const val REGISTER_OBSERVER = 0
        const val UNREGISTER_OBSERVER = 1
        const val UPDATE = 2
        const val GET_DOWNLOAD_INFO = 3
        const val CANCEL_DOWNLOAD = 4
        const val PAUSE_DOWNLOAD = 5
    }
}