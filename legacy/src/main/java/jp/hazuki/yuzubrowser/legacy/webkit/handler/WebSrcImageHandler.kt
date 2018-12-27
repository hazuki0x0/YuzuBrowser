package jp.hazuki.yuzubrowser.legacy.webkit.handler

import android.os.Handler
import android.os.Message

abstract class WebSrcImageHandler : Handler() {
    override fun handleMessage(msg: Message) {
        val url = msg.data.getString("url")
        if (url.isNullOrEmpty()) return
        handleUrl(url)
    }

    abstract fun handleUrl(url: String)
}
