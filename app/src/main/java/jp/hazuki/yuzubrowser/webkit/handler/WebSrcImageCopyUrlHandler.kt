package jp.hazuki.yuzubrowser.webkit.handler

import android.content.Context
import jp.hazuki.yuzubrowser.utils.extensions.setClipboardWithToast
import java.lang.ref.WeakReference

class WebSrcImageCopyUrlHandler(context: Context) : WebSrcImageHandler() {
    private val mReference: WeakReference<Context> = WeakReference(context)

    override fun handleUrl(url: String) {
        mReference.get()?.run {
            setClipboardWithToast(url)
        }
    }
}
