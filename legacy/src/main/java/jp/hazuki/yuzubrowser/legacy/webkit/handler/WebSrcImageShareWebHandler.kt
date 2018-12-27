package jp.hazuki.yuzubrowser.legacy.webkit.handler

import android.app.Activity
import jp.hazuki.yuzubrowser.legacy.utils.WebUtils
import java.lang.ref.WeakReference

class WebSrcImageShareWebHandler(activity: Activity) : WebSrcImageHandler() {
    private val mReference: WeakReference<Activity> = WeakReference(activity)

    override fun handleUrl(url: String) {
        mReference.get()?.let {
            WebUtils.shareWeb(it, url, null)
        }
    }
}
