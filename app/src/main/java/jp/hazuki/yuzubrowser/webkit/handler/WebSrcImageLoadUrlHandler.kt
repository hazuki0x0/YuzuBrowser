package jp.hazuki.yuzubrowser.webkit.handler

import jp.hazuki.yuzubrowser.webkit.CustomWebView
import java.lang.ref.WeakReference

class WebSrcImageLoadUrlHandler(web: CustomWebView) : WebSrcImageHandler() {
    private val mReference: WeakReference<CustomWebView> = WeakReference(web)

    override fun handleUrl(url: String) {
        mReference.get()?.loadUrl(url)
    }
}
