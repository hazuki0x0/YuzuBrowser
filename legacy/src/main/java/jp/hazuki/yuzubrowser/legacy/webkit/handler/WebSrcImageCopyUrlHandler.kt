package jp.hazuki.yuzubrowser.legacy.webkit.handler

import android.content.Context
import jp.hazuki.yuzubrowser.legacy.utils.extensions.setClipboardWithToast
import java.lang.ref.WeakReference

class WebSrcImageCopyUrlHandler(context: Context) : WebSrcImageHandler() {
    private val mReference: WeakReference<Context> = WeakReference(context)

    override fun handleUrl(url: String) {
        mReference.get()?.run {
            setClipboardWithToast(url)
        }
    }
}
