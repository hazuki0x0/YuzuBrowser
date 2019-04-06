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

import android.os.Handler
import android.os.Message
import android.os.Messenger
import java.lang.ref.WeakReference

class ServiceClient(listener: ServiceClientListener) : Handler() {
    private val ref = WeakReference<ServiceClientListener>(listener)

    override fun handleMessage(msg: Message) {
        val listener = ref.get() ?: return

        when (msg.what) {
            ServiceSocket.REGISTER_OBSERVER -> listener.registerObserver(msg.replyTo)
            ServiceSocket.UNREGISTER_OBSERVER -> listener.unregisterObserver(msg.replyTo)
            ServiceSocket.UPDATE -> listener.update(msg)
            ServiceSocket.GET_DOWNLOAD_INFO -> listener.getDownloadInfo(msg.replyTo)
            ServiceSocket.CANCEL_DOWNLOAD -> listener.cancelDownload(msg.obj as Long)
            ServiceSocket.PAUSE_DOWNLOAD -> listener.pauseDownload(msg.obj as Long)
        }
    }

    interface ServiceClientListener {
        fun registerObserver(messenger: Messenger)
        fun unregisterObserver(messenger: Messenger)
        fun update(msg: Message)
        fun getDownloadInfo(messenger: Messenger)
        fun cancelDownload(id: Long)
        fun pauseDownload(id: Long)
    }
}