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

package jp.hazuki.yuzubrowser.favicon

import android.graphics.Bitmap
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import java.util.concurrent.LinkedBlockingQueue

class FaviconAsyncManager(private val manager: FaviconManager) {
    private val faviconThread: FaviconThread

    init {
        faviconThread = FaviconThread()
        faviconThread.start()
    }

    fun destroy() {
        faviconThread.interrupt()
    }

    fun updateAsync(url: String?, icon: Bitmap?) {
        if (url != null && icon != null)
            faviconThread.addQueue(FaviconItem(url, icon))
    }

    operator fun get(url: String): Bitmap? {
        return manager[url]
    }

    private class FaviconItem constructor(val url: String, val icon: Bitmap)

    private inner class FaviconThread : Thread() {

        private val queue = LinkedBlockingQueue<FaviconItem>()

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

        private fun update(item: FaviconItem) {
            manager[item.url] = item.icon
        }

        internal fun addQueue(item: FaviconItem) {
            queue.add(item)
        }

    }
}
