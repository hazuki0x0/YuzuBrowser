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

package jp.hazuki.yuzubrowser.legacy.speeddial

import android.content.Context
import android.graphics.Bitmap
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class SpeedDialAsyncManager(context: Context) {

    private val manager = SpeedDialManager(context)
    private val sdThread = SDThread(manager)

    val all: ArrayList<SpeedDial>
        get() = manager.all

    init {
        sdThread.start()
    }

    fun destroy() {
        sdThread.interrupt()
    }

    fun updateAsync(url: String?, icon: Bitmap?) {
        if (url != null && icon != null)
            sdThread.addQueue(SDItem(url, icon))
    }

    fun isNeedUpdate(url: String?): Boolean {
        return if (url != null) manager.isNeedUpdate(url) else false
    }

    private class SDItem internal constructor(val url: String, val icon: Bitmap)

    private class SDThread internal constructor(private val manager: SpeedDialManager) : Thread() {
        private val queue = LinkedBlockingQueue<SDItem>()

        override fun run() {
            priority = Thread.MIN_PRIORITY
            try {
                while (true) {
                    update(queue.take())
                }
            } catch (e: InterruptedException) {
                Logger.i("Speed dial", "thread stop")
            }
        }

        private fun update(item: SDItem) {
            manager.update(item.url, item.icon)
        }

        internal fun addQueue(item: SDItem) {
            queue.add(item)
        }
    }
}
