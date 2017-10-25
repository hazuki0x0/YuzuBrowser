package jp.hazuki.yuzubrowser.webkit.handler

import android.app.Activity
import jp.hazuki.yuzubrowser.utils.WebUtils
import java.lang.ref.WeakReference

class WebSrcImageShareWebHandler(activity: Activity) : WebSrcImageHandler() {
    private val mReference: WeakReference<Activity> = WeakReference(activity)

    override fun handleUrl(url: String) {
        mReference.get()?.let {
            WebUtils.shareWeb(it, url, null)
        }
    }
}
