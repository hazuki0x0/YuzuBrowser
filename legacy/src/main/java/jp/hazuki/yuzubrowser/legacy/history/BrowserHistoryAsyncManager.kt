package jp.hazuki.yuzubrowser.legacy.history

import android.content.Context
import jp.hazuki.yuzubrowser.legacy.utils.Logger
import java.util.concurrent.LinkedBlockingQueue

class BrowserHistoryAsyncManager(context: Context) {
    private val historyManager = BrowserHistoryManager.getInstance(context)
    private val thread = MyThread(historyManager)

    init {
        thread.start()
    }

    fun destroy() {
        thread.interrupt()
    }

    fun add(url: String) {
        thread.sendMessage(MyMessage(ADD_URL, url))
    }

    fun update(url: String?, title: String?) {
        thread.sendMessage(MyMessage(UPDATE_TITLE, url, title))
    }

    fun getHistoryArray(limit: Int): Array<String> {
        return historyManager.getHistoryArray(limit)
    }

    private class MyMessage(val what: Int, val url: String?, val obj: Any? = null)

    private class MyThread internal constructor(private val mHistoryManager: BrowserHistoryManager) : Thread() {
        private val mMessageQueue = LinkedBlockingQueue<MyMessage>()

        override fun run() {
            priority = Thread.MIN_PRIORITY
            try {
                while (true) {
                    handleMessage(mMessageQueue.take())
                }
            } catch (e: InterruptedException) {
                Logger.i("history", "thread stop")
            }

        }

        private fun handleMessage(msg: MyMessage) {
            when (msg.what) {
                ADD_URL -> mHistoryManager.add(msg.url)
                UPDATE_TITLE -> mHistoryManager.update(msg.url, msg.obj as String?)
            }
        }

        fun sendMessage(msg: MyMessage) {
            mMessageQueue.add(msg)
        }
    }

    companion object {
        private const val ADD_URL = 1
        private const val UPDATE_TITLE = 2
    }
}