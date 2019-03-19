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
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import java.lang.ref.WeakReference

class ActivityClient(listener: ActivityClientListener) : Handler() {
    private val ref = WeakReference<ActivityClientListener>(listener)

    override fun handleMessage(msg: Message) {
        val listener = ref.get() ?: return

        when (msg.what) {
            ServiceSocket.UPDATE -> listener.update(msg.obj as DownloadFileInfo)
            ServiceSocket.GET_DOWNLOAD_INFO -> {
                val obj = msg.obj
                if (obj is List<*>) {
                    listener.getDownloadInfo(obj.filterIsInstance<DownloadFileInfo>())
                }
            }
        }
    }

    interface ActivityClientListener {
        fun update(info: DownloadFileInfo)
        fun getDownloadInfo(list: List<DownloadFileInfo>)
    }
}