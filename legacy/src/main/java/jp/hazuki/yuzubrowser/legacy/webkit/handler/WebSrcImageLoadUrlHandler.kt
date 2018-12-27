package jp.hazuki.yuzubrowser.legacy.webkit.handler

import jp.hazuki.yuzubrowser.legacy.webkit.CustomWebView
import java.lang.ref.WeakReference

class WebSrcImageLoadUrlHandler(web: CustomWebView) : WebSrcImageHandler() {
    private val mReference: WeakReference<CustomWebView> = WeakReference(web)

    override fun handleUrl(url: String) {
        mReference.get()?.loadUrl(url)
    }
}
